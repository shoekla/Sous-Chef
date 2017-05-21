package com.example.abirshukla.souschef;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class Links extends AppCompatActivity {
    String userIn = "";
    ProgressDialog pd;
    String what = "";
    String query = "";
    String dishName = "";
    boolean sous = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_links);
        Bundle b = getIntent().getExtras();
        query = b.getString("query");
        what = b.getString("what");
        ArrayAdapter adapter = null;
        pd = new ProgressDialog(this);
        pd.setMessage("Acquiring data for your dish...");
        if (what.equals("link")) {
            adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, DataForUser.urls);
        }
        else if (what.equals("vid")) {
            adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, DataForUser.vidTitles);
        }
        else if (what.equals("sous")) {
            adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, DataForUser.sousTitles);
        }
        else {
            adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, DataForUser.search);
        }
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                if (what.equals("link")) {
                    clickMenu(DataForUser.urls.get(position));
                }
                else if (what.equals("vid")) {
                    if (position == 0) {
                        Intent intent = new Intent(Intent.ACTION_SEARCH);
                        intent.setPackage("com.google.android.youtube");
                        intent.putExtra("query", query);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else {
                        clickMenu(DataForUser.vids.get(position));
                    }
                }
                else if (what.equals("sous")) {
                    pd.show();
                    sous = true;
                    dishName = DataForUser.sousTitles.get(position);
                    System.out.println("Abir Sous Enterd");
                    getHTML("https://sous-chef2-0.herokuapp.com/sous"+DataForUser.sousPages.get(position).substring(DataForUser.sousPages.get(position).indexOf("recipe/")+6));
                    //Toast.makeText(Links.this, "Sous Link: "+, Toast.LENGTH_SHORT).show();
                }
                else {

                    pd.show();
                    System.out.println("Abir Not Sous Enterd");
                    userIn = DataForUser.search.get(position);
                    String r = DataForUser.search.get(position).replace(" ","%20");
                    getHTML("https://sous-chef2-0.herokuapp.com/search/"+r+"/");

                }

            }
        });
    }
    public void getHTML(final String url) {
        if (sous) {
            DataForUser.dishName = dishName;
            System.out.println("Abir Url:Sous " + url);
            final String[] d = new String[1];
            Ion.with(getApplicationContext())
                    .load(url)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            Intent r = new Intent(Links.this, SousPage.class);
                            //System.out.println("Abir: " + result);
                            System.out.println("Abir Res: " + result);
                            r.putExtra("result", result);

                            pd.hide();
                            //pd.dismiss();
                            startActivity(r);
                        }
                    });


        }
        else {
            System.out.println("Abir Url: " + url);
            final String[] d = new String[1];
            Ion.with(getApplicationContext())
                    .load(url)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            Intent r = new Intent(Links.this, SearchDish.class);
                            //System.out.println("Abir: " + result);
                            r.putExtra("query", userIn);
                            System.out.println("Abir Res: " + result);
                            r.putExtra("result", result);

                            pd.hide();
                            //pd.dismiss();
                            startActivity(r);
                            //System.out.println("First Result: " + result);
                            //a.putExtra("code", result);
                            //a.putExtra("subject", "");
                            //pd.dismiss();
                            //startActivity(a);
                        }
                    });
        }
        sous = false;

    }
    public void searchYoutube(View view) {
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        intent.putExtra("query", query);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void clickMenu(final String url) {
        final CharSequence[] items = { "Open Link", "Share",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(Links.this);
        builder.setTitle("Options For Link");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Open Link")) {
                    if (what.equals("vid")) {
                        try {

                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + url));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        } catch (ActivityNotFoundException e) {

                            // youtube is not installed.Will be opened in other available apps

                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/watch?v=" + url));
                            startActivity(i);
                        }
                    }
                    else {
                        try {
                            Uri uri = Uri.parse("googlechrome://navigate?url=" + url);
                            Intent i = new Intent(Intent.ACTION_VIEW, uri);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        } catch (ActivityNotFoundException e) {
                            // Chrome is probably not installed
                        }
                        dialog.dismiss();
                    }

                } else if (items[item].equals("Share")) {
                    /*
                    String share = "Check out "+url;
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, share);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    Toast.makeText(Links.this, "Shared", Toast.LENGTH_SHORT).show();
                    */
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    dialog.dismiss();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
}
