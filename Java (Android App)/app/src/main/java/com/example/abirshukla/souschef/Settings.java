package com.example.abirshukla.souschef;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class Settings extends AppCompatActivity {
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        editText = (EditText) findViewById(R.id.editText2);
        String e = DataForUser.getEmail();
        if (e.length() != 0) {
            editText.setText(e);
        }
    }
    public void subEmail(View view) {
        String email = editText.getText().toString();
        DataForUser.setEmail(email);
        Intent m = new Intent(Settings.this,MainActivity.class);
        startActivity(m);
    }
}
