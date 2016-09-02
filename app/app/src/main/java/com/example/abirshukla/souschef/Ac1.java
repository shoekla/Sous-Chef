package com.example.abirshukla.souschef;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class Ac1 extends AppCompatActivity {
    Intent f;
    String nameOfDish;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ac1);
        Bundle c = getIntent().getExtras();
        f = new Intent(this,Voice.class);
        String result = c.getString("code");
        nameOfDish = c.getString("nameOfDish");
        f.putExtra("nameOfDish",nameOfDish);
        String subject = c.getString("subject");
        f.putExtra("subject",subject);
        int index = result.indexOf("<p>");
        int index2 = result.indexOf("</p>",index);
        String urA = result.substring(index + 3, index2);
        try {
            getHTML(urA);
            System.out.print("Good");
        }
        catch(Exception e) {
            if (nameOfDish.length() < 2) {
                System.out.println("Try Error: "+e.toString());
                Intent back = new Intent(this,MainActivity.class);
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                startActivity(back);
            }
            else {

                System.out.println("Try Error: "+e.toString());
            }
        }
        System.out.println("Link: "+result.substring(index+3,index2));

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    public void getHTML(final String url) {
        System.out.println("Begin HTML");
        f.putExtra("link", url);
        final String[] d = new String[1];
        Ion.with(getApplicationContext())
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        System.out.println("Result: " + result);
                        if (result.contains("No Error") || result.contains("http://www.foodnetwork.com/</p>")) {
                            Intent er = new Intent(Ac1.this,Error.class);
                            er.putExtra("name",nameOfDish);
                            startActivity(er);
                            return;
                        }
                        f.putExtra("code", result);
                        startActivity(f);
                    }
                });
    }

}
