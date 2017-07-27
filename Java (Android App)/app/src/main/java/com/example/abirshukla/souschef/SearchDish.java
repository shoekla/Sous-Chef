package com.example.abirshukla.souschef;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

public class SearchDish extends AppCompatActivity {
    private static String video_title;
    String query = "";

    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_dish);
        pd = new ProgressDialog(this);
        pd.setMessage("Formatting Data...");
         pd.show();
        Bundle b = getIntent().getExtras();
        query = b.getString("query");
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("Data Found on: " + query);
        String result = b.getString("result");

        String[] urls = result.substring(2, result.indexOf("],")).split(",");
        DataForUser.urls.clear();
        for (int i = 0; i < urls.length; i++) {
            //System.out.println("Abir: Link: " + urls[i]);
            DataForUser.urls.add(urls[i]);
        }
        String[] vids = result.substring(result.indexOf("]") + 1, result.lastIndexOf("],")).split(",");
        DataForUser.vids.clear();
        DataForUser.vidTitles.clear();
        DataForUser.vids.add("Search On Youtube");
        DataForUser.vidTitles.add("Search on Youtube");
        for (int i = 0; i < vids.length; i++) {
            //System.out.println("Abir: Vid: " + vids[i]);
            if (!vids[i].contains("[") && vids[i].length() > 1) {
                DataForUser.vids.add(vids[i].trim());
            }
        }
        new LongOperation().execute("");

        String[] words = result.substring(result.lastIndexOf("[") + 1, result.length() - 2).split(",");
        DataForUser.sousPages.clear();
        DataForUser.sousTitles.clear();
        DataForUser.indexMore = 2;

        for (int i = 0; i < words.length; i++) {
            //System.out.println("Abir: Sous: " + words[i]);
            DataForUser.sousPages.add(words[i]);
            String st = words[i].substring(0,words[i].length()-1);
            DataForUser.sousTitles.add(toTitleCase(st.substring(st.lastIndexOf("/")+1).replace("-"," ")));
        }


        final ArrayList<String> menuList = new ArrayList<>();
        menuList.add(urls.length + " Helpful Links Found");
        menuList.add(vids.length + " Helpful Videos Found");
        if (words.length > 19) {
            DataForUser.sousTitles.add("Load More Recipes");
            menuList.add(words.length + "+ Sous Chef Pages Found");
        }
        else {
            menuList.add(words.length + " Sous Chef Pages Found");

        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, menuList);
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (position == 0) {
                    //Go To Food Items
                    Intent l = new Intent(SearchDish.this, Links.class);
                    l.putExtra("query", query);
                    l.putExtra("what", "link");
                    startActivity(l);
                } else if (position == 1) {
                    Intent l = new Intent(SearchDish.this, Links.class);
                    l.putExtra("what", "vid");
                    l.putExtra("query", query);
                    startActivity(l);
                } else {
                    Intent l = new Intent(SearchDish.this, Links.class);
                    l.putExtra("what", "sous");
                    l.putExtra("query", query);
                    startActivity(l);
                }

            }
        });
        //Toast.makeText(SearchDish.this, "Finished Computing Results", Toast.LENGTH_SHORT).show();
    }


    public class LongOperation extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            for (int i = 1; i < DataForUser.vids.size();i++) {
                DataForUser.vidTitles.add(getTitleQuietly("https://www.youtube.com/watch?v=" + DataForUser.vids.get(i).trim()));
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            for (int i = 0; i < DataForUser.vidTitles.size();i++) {
                if (DataForUser.vidTitles.get(i) == null) {
                    //System.out.println("Abir Index "+i+" is null, "+DataForUser.vids.get(i));
                    DataForUser.vidTitles.set(i,query+" video");
                }
            }

            pd.hide();
            Toast.makeText(SearchDish.this, "Finished Computing Results", Toast.LENGTH_SHORT).show();
        }
        public String getTitleQuietly(String youtubeUrl) {
            //System.out.println("Abir: TitleParam: " + youtubeUrl);

            try {
                if (youtubeUrl != null) {
                    URL embededURL = new URL("http://www.youtube.com/oembed?url=" +
                            youtubeUrl + "&format=json");

                    video_title = new JSONObject(IOUtils.toString(embededURL)).getString("title");
                    return video_title;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }
    }
    public static String toTitleCase(String str) {

        if (str == null) {
            return null;
        }

        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }
}

