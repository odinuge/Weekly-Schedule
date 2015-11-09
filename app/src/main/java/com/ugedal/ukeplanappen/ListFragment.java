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



/**
 * Created by odin on 11/8/15.
 */
public class ListFragment extends Fragment {
    ArrayList<Week> MyList = new ArrayList<Week>();

    SetupAsync Setup = new SetupAsync(this);
    // SetupAsync setup;// = new SetupAsync();
    boolean setupDone = false;
    String ClassName;
    //DownloadFileAsync DownloadFileAsync = new DownloadFileAsync(getActivity());
    RecyclerView rv;
    DownloadManager downloadManager;
    private long downloadReference;

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
        //WeekAdapter adapter = new WeekAdapter(list);
        //rv.setAdapter(adapter);
        return view;

    }
    public void refreshContent(){
        new SetupAsync(this).execute();
    }

    public void openPDF(Week currentWeek){

        String pdf = "";
        if (!currentWeek.getTitle().contains(".pdf"))
            pdf = ".pdf";

        File pdfFile = new File(getActivity().getExternalFilesDir(null),
                currentWeek.getWeekNumber()+currentWeek.getTitle()+pdf);


        if (pdfFile.isFile() && pdfFile.length() == 0) {

            pdfFile.delete();
        }
        if (pdfFile.isFile() && pdfFile.exists()) {
            StartPDFIntentMethod(pdfFile);
        } else {

            DownloadFileAsync dlAsync = new DownloadFileAsync(getActivity(),pdfFile, currentWeek);
            dlAsync.execute(currentWeek.getDlUrl().replaceAll(" ", "%20"));
        }



    }
    public class SetupAsync extends AsyncTask<String, String, String> {
        ListFragment Fragment = null;
        int className;
        public SetupAsync(ListFragment Fragment) {
            // TODO Auto-generated constructor stub
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
            rv.setAdapter(null);
        }

        @Override
        protected String doInBackground(String... aurl) {

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            int trinn = sharedPref.getInt(getString(R.string.current_trinn), R.id.trinn_1);

            className =(classes.lastIndexOf(trinn)+1);
            String h = "http://aset.no/klassetrinn/"+className+"-trinn/?vis=ukeplaner";


            try {
                MyList = new HTMLWeekExtractor(getActivity()).extractWeeks(h, className);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.getMessage();
            }

            //
            return null;

        }

        protected void onProgressUpdate(String... progress) {

            // dialog.setProgress(Integer.parseInt(progress[0]));
        }

        protected void onPostExecute(String result) {
            ((MainActivity) getActivity()).swipeContainer.setRefreshing(false);
            if(result==null) {
                try {
                    WeekAdapter adapter = new WeekAdapter(MyList, getActivity());
                    rv.setAdapter(adapter);
                    rv.setVisibility(View.VISIBLE);


                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    rv.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                //setListShown(true);
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

        ProgressDialog dialog;
        AppCompatActivity activity = null;
        InputStream input = null;
        OutputStream output = null;
        boolean cancel = false;
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
            if (cancel == true)
                return "ERROR";
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
                    // publishing the progress....
                    publishProgress("" + (int) (total * 100 / lenghtOfFile));
                    output.write(data, 0, count);


                }

                output.flush();
                output.close();
                input.close();
                if(cancel)
                    pdfFile.delete();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    // e1.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
                pdfFile.delete();
                return e.getMessage();

            }

            return null;

        }

        protected void onProgressUpdate(String... progress) {
            if (activity != null)
                dialog.setProgress(Integer.parseInt(progress[0]));
        }

        protected void onPostExecute(String result) {
            ((MainActivity) getActivity()).swipeContainer.setRefreshing(false);
            if(result==null) {
                StartPDFIntentMethod(pdfFile);
            }else {
                pdfFile.delete();
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            }


        }

        @Override
        public void onCancel(DialogInterface dialog) {
            // TODO Auto-generated method stub
            // pdfFile.delete();
            // this.cancel(true);

        }

    }

    private void StartPDFIntentMethod(File pdfFile) {// Must Have
        // TODO Auto-generated method stub
        // pdfFile = new File(Environment.getExternalStorageDirectory()
        // + "/.UkeplanAppen/" + fileName);

        if (pdfFile.exists() && pdfFile.length() == 0) {
            pdfFile.delete();

        } else if (pdfFile.isFile()) {
            Uri PATH = Uri.fromFile(pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(PATH, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                startActivity(intent);
                Log.i("LOG-UGEDAL", "The PATH of the opned File: " + PATH);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                builder.setTitle("PDF Leser");
                builder.setMessage("Ingen PDF leser tilgjengelig. Vill du laste ned Adobe Reader fra Play Market?");
                builder.setPositiveButton("Ja",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent marketIntent = new Intent(
                                        Intent.ACTION_VIEW);
                                marketIntent.setData(Uri
                                        .parse("market://details?id=com.adobe.reader"));
                                try {
                                    startActivity(marketIntent);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch
                                    // block
                                    e.printStackTrace();
                                }
                            }
                        });
                builder.setNegativeButton("Nei", null);
                builder.create().show();
            }

        }
    }


}
