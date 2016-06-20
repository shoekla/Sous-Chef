package com.example.abirshukla.souschef;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class ScrollVoice extends AppCompatActivity {
    TextView name;
    ProgressDialog pd;
    TextView youSaid;
    TextView info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_voice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Sous Chef");
        setSupportActionBar(toolbar);
        Bundle dish = getIntent().getExtras();
        String code = dish.getString("code");
        name = (TextView) findViewById(R.id.title);
        pd = new ProgressDialog(this);
        pd.setMessage("Acquiring data for your dish...");
        pd.show();
        name.setText("Code:"+ code);
        String nameOfDish = dish.getString("nameOfDish");
        String creator = getCreator(code);
        name.setText(nameOfDish+" by "+creator);
        pd.dismiss();
        info = (TextView) findViewById(R.id.info);
        info.setText(code);

    }
    public String getTime(String code) {
        return "";
    }

    public String getServing(String code) {
        return "";
    }

    public String getIngredients(String code) {
        return "";
    }

    public String getInstructions(String code) {
        return "";
    }

    public String getCreator(String code) {
        int index = code.indexOf("Recipe courtesy of");
        int indexA =code.indexOf("\"name\">",index);
        int index2 = code.indexOf("</span>", index);
        if (index == -1 || index2 == -2) {
            return "";
        }
        return code.substring(indexA+7,index2);
    }
}
