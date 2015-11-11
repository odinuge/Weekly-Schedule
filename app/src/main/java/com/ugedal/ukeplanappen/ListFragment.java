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

package com.ugedal.ukeplanappen;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.List;


public class ListFragment extends Fragment {
    ArrayList<Week> MyList = new ArrayList<Week>();

    RecyclerView rv;

    static ArrayList<Integer> classes = new ArrayList<Integer>();
    static {
        classes.add(R.id.trinn_1);
        classes.add(R.id.trinn_2);
        classes.add(R.id.trinn_3);
        classes.add(R.id.trinn_4);
        classes.add(R.id.trinn_5);
        classes.add(R.id.trinn_6);
        classes.add(R.id.trinn_7);
        classes.add(R.id.trinn_8);
        classes.add(R.id.trinn_9);
        classes.add(R.id.trinn_10);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        rv = (RecyclerView) view.findViewById(R.id.cardList);
        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        new SetupAsync(this).execute();
        return view;

    }
    public void refreshContent(){
        new SetupAsync(this).execute();
    }

    public void openPDF(Week currentWeek){

        String pdf = new String();
        if (!currentWeek.getTitle().contains(".pdf"))
            pdf = ".pdf";
        SharedPreferences sharedPref = getActivity()
				.getPreferences(Context.MODE_PRIVATE);
        int trinn = sharedPref.getInt(getString(R.string.current_trinn), R.id.trinn_1);

        int className = classes.lastIndexOf(trinn)+1;

        File pdfFile = new File(getActivity().getExternalFilesDir(null),
                currentWeek.getWeekNumber() + currentWeek.getTitle() + className + pdf);

        if (pdfFile.isFile() && pdfFile.length() == 0)
            pdfFile.delete();

        if (pdfFile.isFile() && pdfFile.exists()) {
            StartPDFIntentMethod(pdfFile);
            return;
        }
        DownloadFileAsync dlAsync = new DownloadFileAsync(getActivity(),pdfFile, currentWeek);
        dlAsync.execute(currentWeek.getDlUrl().replaceAll(" ", "%20"));
    }

    public class SetupAsync extends AsyncTask<String, String, String> {
        ListFragment Fragment = null;
        int className;
        public SetupAsync(ListFragment Fragment) {
            attach(Fragment);
        }

        void attach(ListFragment Fragment) {
            this.Fragment = Fragment;
        }

        void detach() {
            this.Fragment = null;
        }

        @Override
        protected void onPreExecute() {
            rv.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... aurl) {

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            int trinn = sharedPref.getInt(getString(R.string.current_trinn), R.id.trinn_1);

            className = classes.lastIndexOf(trinn)+1;
            String h = "http://aset.no/klassetrinn/"+className+"-trinn/?vis=ukeplaner";

            try {
                MyList = new HTMLWeekExtractor(getActivity()).extractWeeks(h, className);
            } catch (IOException e1) {
                e1.printStackTrace();
                return e1.getMessage();
            }

            return null;

        }

        protected void onPostExecute(String result) {
            ((MainActivity) getActivity()).swipeContainer.setRefreshing(false);
            if(result==null) {
                try {
                    WeekAdapter adapter = new WeekAdapter(MyList, getActivity());
                    rv.setAdapter(adapter);
                    rv.setVisibility(View.VISIBLE);


                } catch (Exception e) {
                    e.printStackTrace();
                    rv.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                MyList = new HTMLWeekExtractor(getActivity()).getList(null,className);
                WeekAdapter adapter = new WeekAdapter(MyList, getActivity());
                rv.setAdapter(adapter);
                rv.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            }

        }
    }
    public class DownloadFileAsync extends AsyncTask<String, String, String>
            implements OnCancelListener {

        AppCompatActivity activity = null;
        InputStream input = null;
        OutputStream output = null;
        File pdfFile;
        Week currentWeek;

        DownloadFileAsync(FragmentActivity act, File pdfFile, Week currentWeek) {
            this.pdfFile = pdfFile;
            this.currentWeek = currentWeek;
        }


        @Override
        protected void onPreExecute() {
            ((MainActivity) getActivity()).swipeContainer.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... aurl) {
            try {
                int count;
                URL url1 = new URL(aurl[0]);
                URLConnection conexion = url1.openConnection();
                conexion.setConnectTimeout(1000 * 3);

                conexion.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conexion.getContentLength();

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
            ((MainActivity) getActivity()).swipeContainer.setRefreshing(false);
            if(result==null) {
                StartPDFIntentMethod(pdfFile);
            } else {
                pdfFile.delete();
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void StartPDFIntentMethod(File pdfFile) {
        if (pdfFile.exists() && pdfFile.length() == 0) {
            pdfFile.delete();
			return;
        }
		Uri PATH = Uri.fromFile(pdfFile);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(PATH, "application/pdf");
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		try {
			startActivity(intent);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		builder.setTitle("PDF");
		builder.setMessage("Kan ikke åpne PDF. Åpne Google Play for å finne en PDF-leser.");
		builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent marketIntent = new Intent(Intent.ACTION_VIEW);
				marketIntent.setData(Uri
						.parse("https://play.google.com/store/search?q=pdf&c=apps"));
				try {
					startActivity(marketIntent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton("Nei", null);
		builder.create().show();
    }
}
