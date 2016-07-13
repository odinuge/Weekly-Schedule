/*
 * Copyright (c) 2015 Odin Ugedal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ugedal.weeklyschedule;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class ListFragment extends Fragment {
    static ArrayList<Integer> classes = new ArrayList<Integer>();

    static {
        classes.add(R.id.grade_1);
        classes.add(R.id.grade_2);
        classes.add(R.id.grade_3);
        classes.add(R.id.grade_4);
        classes.add(R.id.grade_5);
        classes.add(R.id.grade_6);
        classes.add(R.id.grade_7);
        classes.add(R.id.grade_8);
        classes.add(R.id.grade_9);
        classes.add(R.id.grade_10);
    }

    ArrayList<Schedule> myList = new ArrayList<Schedule>();
    RecyclerView rv;
    ScheduleAdapter adapter;
    SetupAsync currentAsync;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        rv = (RecyclerView) view.findViewById(R.id.cardList);
        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);

        adapter = new ScheduleAdapter(myList, this);
        rv.setAdapter(adapter);
        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        refreshContent();
    }

    public void refreshContent() {
        if (currentAsync != null) {
            currentAsync.cancel(true);
        }
        currentAsync = new SetupAsync(this);
        currentAsync.execute();

    }

    public void openPDF(Schedule currentSchedule) {
        String downloadUrl = currentSchedule.getDlUrl();

        SharedPreferences sharedPref = getActivity()
                .getPreferences(Context.MODE_PRIVATE);
        int trinn = sharedPref.getInt(getString(R.string.current_grade_key), R.id.grade_1);

        int className = classes.lastIndexOf(trinn) + 1;

        File pdfFile = new File(getActivity().getExternalFilesDir(null),
                currentSchedule.getFileName());

        if (pdfFile.isFile() && pdfFile.length() == 0)
            pdfFile.delete();

        if (pdfFile.isFile() && pdfFile.exists()) {
            StartPDFIntentMethod(pdfFile);
            return;
        }
        DownloadFileAsync dlAsync = new DownloadFileAsync(getActivity(), pdfFile, currentSchedule);
        dlAsync.execute(downloadUrl.replaceAll(" ", "%20"));
    }
    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void StartPDFIntentMethod(File pdfFile) {
        if (pdfFile.exists() && pdfFile.length() == 0) {
            pdfFile.delete();
            return;
        }
        Uri PATH = Uri.fromFile(pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(PATH, getMimeType(pdfFile.getPath()));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(intent);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setTitle(getContext().getString(R.string.pdf));
        builder.setMessage(getContext().getString(R.string.pdf_intent_message));
        builder.setPositiveButton(getContext().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                marketIntent.setData(Uri
                        .parse(getContext().getString(R.string.google_play_pdf_query)));
                try {
                    startActivity(marketIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(getContext().getString(android.R.string.no), null);
        builder.create().show();
    }

    public class SetupAsync extends AsyncTask<String, String, String> {
        ListFragment mFragment = null;
        ArrayList<Schedule> myList = new ArrayList<Schedule>();

        int className;

        public SetupAsync(ListFragment Fragment) {
            attach(Fragment);
        }

        void attach(ListFragment Fragment) {
            this.mFragment = Fragment;
        }

        void detach() {
            this.mFragment = null;
        }

        @Override
        protected void onPreExecute() {
            // rv.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... aurl) {

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            int trinn = sharedPref.getInt(getString(R.string.current_grade_key), R.id.grade_1);

            className = classes.lastIndexOf(trinn) + 1;

            try {
                myList = ScheduleExtractor.extractSchedules(className, sharedPref, mFragment.getContext());
            } catch (IOException e1) {
                e1.printStackTrace();
                return e1.getMessage();
            }

            return null;

        }

        protected void onPostExecute(String result) {
            if (isCancelled())
                return;

            ((MainActivity) getActivity()).setRefreshing(false);
            if (result == null) {
                try {
                    mFragment.myList.clear();
                    ;
                    mFragment.myList.addAll(myList);
                    rv.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();


                } catch (Exception e) {
                    e.printStackTrace();
                    rv.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                myList = ScheduleExtractor.getList(null, className, sharedPref, mFragment.getContext());
                mFragment.myList.clear();
                ;
                mFragment.myList.addAll(myList);
                adapter.notifyDataSetChanged();
                rv.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class DownloadFileAsync extends AsyncTask<String, String, String> {

        AppCompatActivity activity = null;
        InputStream input = null;
        OutputStream output = null;
        File pdfFile;
        Schedule currentSchedule;

        DownloadFileAsync(FragmentActivity act, File pdfFile, Schedule currentSchedule) {
            this.pdfFile = pdfFile;
            this.currentSchedule = currentSchedule;
        }


        @Override
        protected void onPreExecute() {
            ((MainActivity) getActivity()).setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                int count;
                URL url1 = new URL(urls[0]);
                URLConnection connection = url1.openConnection();
                connection.setConnectTimeout(1000 * 3);

                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                input = new BufferedInputStream(url1.openStream());
                output = new FileOutputStream(pdfFile);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
                pdfFile.delete();
                return e.getMessage();
            }
            return null;

        }

        protected void onPostExecute(String result) {
            if (isCancelled())
                return;

            ((MainActivity) getActivity()).setRefreshing(false);
            if (result == null) {
                StartPDFIntentMethod(pdfFile);
            } else {
                pdfFile.delete();
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
