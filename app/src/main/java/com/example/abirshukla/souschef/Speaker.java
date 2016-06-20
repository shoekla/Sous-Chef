package com.example.abirshukla.souschef;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.Locale;

public class Speaker extends Activity implements DialogInterface.OnClickListener, TextToSpeech.OnInitListener, View.OnClickListener {
    //TTS object
    private TextToSpeech myTTS;
    private TextToSpeech myTTSA;
    //status check code
    private int MY_DATA_CHECK_CODE = 0;
    TextView said;
    Intent a;
    String url;
    //create the Activity
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker);
        //listen for clicks
		said = (TextView) findViewById(R.id.enter);

        //check for TTS data
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        Bundle wor = getIntent().getExtras();
        String nameOfDish = wor.getString("nameOfDish");
        String res = wor.getString("res");
        System.out.println("Res: " + res);
        said.setText(res);
        chefSpeak(res);
        a = new Intent(this, Ac1.class);
        a.putExtra("nameOfDish", nameOfDish);
        String subject = wor.getString("subject");
        a.putExtra("subject",subject);
        nameOfDish = nameOfDish.replace(" ","%20");
         url = "http://abirshukla.pythonanywhere.com/searchCook/"+nameOfDish+"/";
        getHTML(url);
    }
    public void chefSpeak(final String speech) {
        Intent checkTTSIntentA = new Intent();
        checkTTSIntentA.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntentA, MY_DATA_CHECK_CODE);
        myTTSA = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    myTTSA.setLanguage(Locale.UK);
                        myTTSA.speak(speech, TextToSpeech.QUEUE_FLUSH, null);

                }
            }
        });
    }
    //respond to button clicks
    public void onClick(View v) {

        //get the text entered
        EditText enteredText = (EditText)findViewById(R.id.enter);
        String words = enteredText.getText().toString();
        speakWords(words);
    }

    //speak the user text
    private void speakWords(String speech) {

        //speak straight away
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    //act on result of TTS data check
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
                myTTS = new TextToSpeech(this, this);
            }
            else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    //setup TTS
    public void onInit(int initStatus) {

        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }
    public void getHTML(final String url) {
        System.out.println("Begin HTML");
        System.out.println("Final Url: "+url);
        final String[] d = new String[1];
        Ion.with(getApplicationContext())
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        System.out.println("First Result: " + result);
                        a.putExtra("code", result);
                        while (myTTSA.isSpeaking()) {}
                        startActivity(a);
                    }
                });
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
        myTTSA.stop();
        myTTSA.shutdown();
        getHTML(url);
    }

    @Override
    protected void onDestroy() {
        myTTSA.stop();
        myTTSA.shutdown();
        super.onDestroy();
    }
}
