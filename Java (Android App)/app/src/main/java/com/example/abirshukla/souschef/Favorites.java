package com.example.abirshukla.souschef;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

public class Favorites extends AppCompatActivity {
    String dishName = "";
    String dishUrl = "";
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        pd = new ProgressDialog(this);
        pd.setMessage("Acquiring data for your dish...");
        final ArrayList<String> titles = new ArrayList<>();
        for (int i = 0; i < DataForUser.favorites.size();i++) {
            String st = DataForUser.favorites.get(i);
            st = st.substring(0,st.length()-1);
            titles.add(toTitleCase(st.substring(st.lastIndexOf("/")+1).replace("-"," ")));
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, titles);
        ListView listView = (ListView) findViewById(R.id.favList);
        listView.setAdapter(adapter);
        if (DataForUser.favorites.size() == 0) {
            Toast.makeText(Favorites.this,"No Favorites Added", Toast.LENGTH_SHORT).show();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                    pd.show();
                    dishUrl = DataForUser.favorites.get(position);
                dishName = titles.get(position);
                    getHTML("https://sous-chef2-0.herokuapp.com/sous"+dishUrl);


            }
        });
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
    public void getHTML(final String url) {

            DataForUser.dishName = dishName;
            System.out.println("Abir Url:Sous " + url);
            final String[] d = new String[1];
            Ion.with(getApplicationContext())
                    .load(url)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            Intent r = new Intent(Favorites.this, SousPage.class);
                            //System.out.println("Abir: " + result);
                            System.out.println("Abir Res: " + result);
                            r.putExtra("result", result);
                            r.putExtra("dishUrl", dishUrl);

                            pd.hide();
                            pd.dismiss();
                            //pd.dismiss();
                            startActivity(r);
                        }
                    });


        }
}
