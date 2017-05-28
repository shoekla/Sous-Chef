package com.example.abirshukla.souschef;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class Results extends AppCompatActivity {
    SharedPreferences sharedPref;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    ProgressDialog pd;
    private StorageReference mStorageRef;
    boolean cameraUse = false;
    String query = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle b = getIntent().getExtras();
        TextView textView = (TextView) findViewById(R.id.textView);
        query = b.getString("query");
        textView.setText(query);
        String result = b.getString("result");
        System.out.println("Abir: Res: "+result);
        String[] urls = result.substring(2,result.indexOf("],")).split(",");
        DataForUser.urls.clear();
        for (int i = 0; i < urls.length;i++) {
            System.out.println("Abir: Link: "+urls[i]);
            if (urls[i].length() > 1) {
                DataForUser.urls.add(urls[i]);
            }
        }
        String[] words = result.substring(result.lastIndexOf("[")+1,result.length()-2).split(",");
        DataForUser.search.clear();
        for (int i = 0; i < words.length;i++) {
            System.out.println("Abir: Search: "+words[i]);
            DataForUser.search.add(words[i].trim());
        }
        Toast.makeText(Results.this, "Finished Computing Results", Toast.LENGTH_SHORT).show();
        final ArrayList<String> menuList = new ArrayList<>();
        menuList.add(DataForUser.search.size()+" Search Terms Found");
        menuList.add(DataForUser.urls.size()+" Helpful Links Found");
        ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.activity_listview,menuList);
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (position == 0) {
                    //Go To Food Items
                    Intent l = new Intent(Results.this,Links.class);
                    l.putExtra("what","search");
                    l.putExtra("query",query);
                    startActivity(l);
                }
                else {
                    Intent l = new Intent(Results.this,Links.class);
                    l.putExtra("what","link");
                    l.putExtra("query",query);
                    startActivity(l);
                }

            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent s = new Intent(Results.this,Settings.class);
            startActivity(s);
            return true;
        }
        else if (id == R.id.action_favorite) {
            Toast.makeText(Results.this, "Favorites Coming Soon", Toast.LENGTH_SHORT).show();

        }

        return super.onOptionsItemSelected(item);
    }

}
