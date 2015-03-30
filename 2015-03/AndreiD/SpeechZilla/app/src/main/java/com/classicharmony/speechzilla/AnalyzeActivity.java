package com.classicharmony.speechzilla;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.classicharmony.speechzilla.adapters.TheListAdapter;
import com.classicharmony.speechzilla.models.TheNote;
import com.classicharmony.speechzilla.utils.CroutonStyles;
import com.classicharmony.speechzilla.utils.DatabaseHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class AnalyzeActivity extends ActionBarActivity {


    private String text_to_analyze;
    private AsyncHttpClient http_client;
    private Context mContext;
    public ArrayList<String> mArraylist = new ArrayList<>();

    protected BlockingQueue queue = null;
    protected String API_KEY = "s4snhnsun6uu";
    protected String API_KEY_SECRET = "2j3l8pgnq8d7936";
    protected String endpointUrl = "https://text.s4.ontotext.com/v1/news";

    private ListView listView_analyze;
    private Button button_delete_all;
    private TheListAdapter mAdapter;
    private ArrayList<Object> locations;
    private ArrayList<Object> organizations;
    private ArrayList<Object> keywords;
    private ProgressBar progress_bar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_analyze);

        mContext = AnalyzeActivity.this;

        listView_analyze = (ListView) findViewById(R.id.listView_analyze);
        button_delete_all = (Button) findViewById(R.id.button_delete_all);
        progress_bar = (ProgressBar) findViewById(R.id.progressBar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            text_to_analyze = extras.getString("text_to_analyze");
        }


        get_all_notes();


        text_to_analyze = text_to_analyze.trim();
        text_to_analyze = text_to_analyze.replaceAll("\n", ". ");

        final JSONObject jsonParams = new JSONObject();
        final StringEntity entity;
        try {
            jsonParams.put("document", text_to_analyze);
            jsonParams.put("documentType", "text/plain");
            entity = new StringEntity(jsonParams.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }




        http_client = new AsyncHttpClient();
        http_client.setMaxRetriesAndTimeout(0, 5000);
        http_client.setBasicAuth(API_KEY, API_KEY_SECRET);
        http_client.addHeader("Content-Type", "application/json");
        http_client.addHeader("Accept", "application/json");
        http_client.addHeader("Accept-Encoding", "gzip");


        http_client.post(mContext, endpointUrl, entity, "application/json", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i("STARTING>>>", "<><><><><><><><><>");
                try {
                    String responseText = EntityUtils.toString(entity);
                    Log.i(">>> SENDING >>", String.valueOf(responseText));
                } catch (IOException e) {
                    e.printStackTrace();
                }


                progress_bar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    String server_output = new String(response, "utf-8");
                    process_output(server_output);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                progress_bar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e("FAILED", String.valueOf(statusCode));
                try {
                    if (errorResponse != null) {
                        String server_output = new String(errorResponse, "utf-8");
                        Log.d("FAILED", server_output);
                        Toast.makeText(mContext,"S4 processing failed with reason "+server_output,Toast.LENGTH_LONG).show();
                    }
                } catch (UnsupportedEncodingException ex) {
                    e.printStackTrace();
                }

                progress_bar.setVisibility(View.GONE);
            }
        });


        button_delete_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper mDatabase = new DatabaseHelper(mContext);
                mDatabase.delete_ALL_Notes();
                mAdapter.clear_all();
                mAdapter.notifyDataSetChanged();

            }
        });

        listView_analyze.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final TheNote mNote = mAdapter.getItem(position);

                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                alert.setTitle(mNote.getCreated_at());
                final EditText input = new EditText(mContext);
                alert.setView(input);

                input.setText(mNote.getFull_text());

                alert.setNegativeButton("Copy to clipboard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("SpeechZilla", mNote.getFull_text());
                        clipboard.setPrimaryClip(clip);
                        Crouton.makeText((android.app.Activity) mContext, "Copied to clipboard", CroutonStyles.GREEN).show();
                    }
                });

                alert.setNeutralButton("Map", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent i_tomap = new Intent(mContext,ToMapActivity.class);
                        i_tomap.putExtra("locations_str",mNote.getLocation_list());
                        startActivity(i_tomap);
                    }
                });

                alert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alert.show();
            }
        });

    }


    private void process_output(String server_output) {

        Log.i(">>>> >>> >>>", server_output);
        organizations = new ArrayList<>();
        locations = new ArrayList<>();
        keywords = new ArrayList<>();

        try {
            JSONObject jObj = new JSONObject(server_output);

            if (jObj.has("entities")) {
                JSONObject jEntities = jObj.getJSONObject("entities");

                if (jEntities.has("Location")) {
                    try {
                        JSONArray jArr = jEntities.getJSONArray("Location");
                        for (int i = 0; i < jArr.length(); i++) {
                            JSONObject jAuxx = jArr.getJSONObject(i);
                            locations.add(jAuxx.getString("string"));
                        }
                    } catch (Exception ex) {
                        Log.e("here", ex.getLocalizedMessage());
                    }
                }
                if (jEntities.has("Organization")) {

                    try {
                        JSONArray jArr = jEntities.getJSONArray("Location");
                        for (int i = 0; i < jArr.length(); i++) {
                            JSONObject jAuxx = jArr.getJSONObject(i);
                            organizations.add(jAuxx.getString("string"));
                        }
                    } catch (Exception ex) {
                        Log.e("here", ex.getLocalizedMessage());
                    }
                }

            }

            if (jObj.has("keyphrasesTfIdf")) {
                JSONArray jArr = jObj.getJSONArray("keyphrasesTfIdf");
                for (int i = 0; i < jArr.length(); i++) {
                    keywords.add(String.valueOf(jArr.get(i)));
                }

            } else {
                Log.e("no categories found", "----------");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }



        store_it(text_to_analyze, String.valueOf(locations), String.valueOf(organizations),String.valueOf(keywords));

    }

    private void store_it(String text_to_analyze, String locations, String organizations, String keywords) {

        DatabaseHelper mDatabase = new DatabaseHelper(mContext);

        TheNote mNote = new TheNote();
        mNote.setFull_text(text_to_analyze);
        mNote.setOrganization_list(organizations);
        mNote.setLocation_list(locations);
        mNote.setKeywords(keywords);
        mDatabase.createNote(mNote);
        mDatabase.closeDB();

        get_all_notes();

    }

    private void get_all_notes() {

        DatabaseHelper mDatabase = new DatabaseHelper(mContext);

        List<TheNote> list_notes = mDatabase.getAllNotes();
        mAdapter = new TheListAdapter(mContext, list_notes);
        listView_analyze.setAdapter(mAdapter);


    }
}























