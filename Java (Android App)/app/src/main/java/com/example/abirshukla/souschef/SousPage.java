package com.example.abirshukla.souschef;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static com.example.abirshukla.souschef.DataForUser.hm;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class SousPage extends AppCompatActivity implements RecognitionListener {
    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "sous chef"; //adjust this keyphrase!
    ProgressDialog pd;
    private SpeechRecognizer recognizer;

    private final int REQ_CODE_SPEECH_INPUT = 100;
    //TextToSpeech t1;
    String subject = "";

    //private SpeechRecognizerManager mSpeechRecognizerManager;
    String nameOfDish = DataForUser.dishName;
    Session session = null;
    Context context = null;
    String dishUrl = "";

    String rec, subjectEmail, textMessage;
    boolean v = false;
    private TextToSpeech myTTSA;
    private int MY_DATA_CHECK_CODEA = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sous_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle b = getIntent().getExtras();
        pd = new ProgressDialog(this);
        pd.setMessage("Acquiring data for your dish...");
        pd.show();
        dishUrl = b.getString("dishUrl");
        TextView dishNameView = (TextView) findViewById(R.id.textView2);
        dishNameView.setText(DataForUser.dishName);
        String result = b.getString("result");
        System.out.println("Abir: Res: "+result);
        String[] ingr = result.substring(2,result.indexOf("],")).split(";a,");
        DataForUser.ingredients.clear();
        DataForUser.directions.clear();
        DataForUser.times.clear();
        DataForUser.step = -1;
        DataForUser.hm.clear();
        for (int i = 0; i < ingr.length;i++) {
            System.out.println("Abir: Ingr: "+ingr[i].trim());
            if (ingr[i].length() > 1) {
                if (ingr[i].contains(";a\",")) {
                    String[] ar = ingr[i].split(";a\",");
                    for (String sar: ar) {
                        DataForUser.ingredients.add(sar.trim());

                    }

                }
                else {
                    DataForUser.ingredients.add(ingr[i].trim().substring(1));
                }
            }
        }
        String[] time = result.substring(result.indexOf("]") + 1, result.lastIndexOf("],")).replace("Prep","").replace("Cook","").replace("Ready In","").split(",");

        for (int i = 0; i < time.length; i++) {
            System.out.println("Abir: Time: " + time[i]);
            DataForUser.times.add(time[i].trim());

        }


        String[] dir = result.substring(result.lastIndexOf("[") + 1, result.length() - 2).split(";a,");

        for (int i = 0; i < dir.length; i++) {
            if (dir[i].contains(";a\",")) {
                String[] ar = dir[i].split(";a\",");
                for (String sar: ar) {
                    System.out.println("Abir: Extra: "+sar);
                    DataForUser.directions.add(sar.trim());
                }

            } else {
                System.out.println("Abir: Dir: Step " + i + ": " + dir[i]);
                DataForUser.directions.add(dir[i]);
            }
        }

        //Put Data on screen
        String timeText = "";
        if (DataForUser.times.size() == 4) {
            timeText = "Time: "+DataForUser.times.get(3) + " (" + DataForUser.times.get(1).substring(1) + " Prep, " + DataForUser.times.get(2) + " Cook)";

        }
        else if(DataForUser.times.size() == 3){
            timeText = "Time: "+DataForUser.times.get(2) + " (" + DataForUser.times.get(1).substring(1) + " Prep)";


        }
        else if (DataForUser.times.size() == 2) {
            timeText = "Time: "+DataForUser.times.get(1).substring(1);
        }
        TextView timeView = (TextView) findViewById(R.id.textView3);
        int index = result.indexOf("Ready In");
        index = result.indexOf("u",index)+1;
        System.out.println("Abir: Index: "+index);
        System.out.println("Abir: Test: "+result.substring(index,index+60));
        DataForUser.serve = result.substring(index,result.indexOf(",",index)).trim();
        int index2 = result.indexOf("u",index);
        DataForUser.cals = result.substring(index2+1,result.indexOf(",",index2)).trim();
        String hmTime = "";
        if (timeText.contains("1 h")) {
            hmTime = timeText.replace(" h","hour");
        }
        else {
            hmTime = timeText.replace(" h","hours");
        }
        if (hmTime.contains("1 m")) {
            hmTime = hmTime.replace(" m","minute");

        }
        else {
            hmTime = hmTime.replace(" m","minutes");
        }
        hm.put("time",hmTime);
        if (DataForUser.serve.equals("???")) {
            hm.put("servings","unknown");
        }
        else {
            hm.put("servings",DataForUser.serve);
        }
        if (DataForUser.cals.equals("???")) {
            hm.put("cal","unknown");
        }
        else {
            hm.put("cal",DataForUser.cals);
        }


        //timeView.setText();

        //TextView ingredients = (TextView) findViewById(R.id.textView4);
        String ing = "Ingredients\n\n";
        for (int i = 0; i < DataForUser.ingredients.size();i++) {
            ing = ing + DataForUser.ingredients.get(i)+"\n";
        }
        hm.put("ing",ing);
        ing = ing + "\nDirections\n" + "\n";
        String dire = "";
        String[] insArr = new String[DataForUser.directions.size()];
        for (int i = 0; i < DataForUser.directions.size();i++) {
            String d = DataForUser.directions.get(i);
            dire = dire + d;
            ing = ing + "Step "+(i+1)+": "+d+"\n\n";
            insArr[i] = "Step "+(i+1)+": "+d;
        }
        hm.put("insArr",insArr);
        hm.put("insWhole",dire);
        DataForUser.everything = timeText+"\n"+"Cal: "+DataForUser.cals+", Serves: "+DataForUser.serve+"\n\n"+ing+"\n";
        DataForUser.everything = DataForUser.everything.replace(";a,","").replace("\\xae","").replace(".;a","");
        timeView.setText(DataForUser.everything);



        //mSpeechRecognizerManager=new SpeechRecognizerManager(this);


    Intent checkTTSIntentA = new Intent();
        checkTTSIntentA.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntentA, MY_DATA_CHECK_CODEA);
        myTTSA = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    myTTSA.setLanguage(Locale.UK);
                    pd.hide();
                    pd.dismiss();



                }
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTSA.stop();
                try {
                    recognizer.cancel();
                } catch (Exception e) {

                }
                v = true;
                promptSpeechInput();
                //Toast.makeText(SousPage.this, "Voice", Toast.LENGTH_SHORT).show();
            }
        });
        try {
            new AsyncTask<Void, Void, Exception>() {
                @Override
                protected Exception doInBackground(Void... params) {
                    try {
                        Assets assets = new Assets(SousPage.this);
                        File assetDir = assets.syncAssets();
                        setupRecognizer(assetDir);
                        //Toast.makeText(, "Touch Button or say 'Sous Chef' for voice commands! ", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        return e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Exception result) {
                    if (result != null) {
                        //Toast.makeText(SousPage.this, "Failed to init pocketSphinxRecognizer ", Toast.LENGTH_SHORT).show();
                    } else {
                        recognizer.startListening(KWS_SEARCH);
                    }
                }
            }.execute();
            Toast.makeText(SousPage.this, "Touch Button for voice commands! ", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(SousPage.this, "Failed to init pocketSphinxRecognizer ", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        myTTSA.stop();
        recognizer.cancel();
        recognizer.shutdown();
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        System.out.println("Abir: Poc: "+text);
        if (text.equals(KEYPHRASE)) {
            recognizer.cancel();
            myTTSA.stop();
            v = true;
            promptSpeechInput();    // <- You have to implement this

        }
    }



    @Override
    public void onResult(Hypothesis hypothesis) {}

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onTimeout() {}

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
    }

    @Override
    public void onError(Exception error) {
    }








    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sous, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent s = new Intent(SousPage.this,Settings.class);
            startActivity(s);
            return true;
        }
        else if (id == R.id.action_favorite) {
            Intent f = new Intent(SousPage.this,Favorites.class);
            startActivity(f);

        }
        else if (id == R.id.addRemoveFav) {
            Toast.makeText(getApplicationContext(), DataForUser.editFav(dishUrl),
                    Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }


    //public void chefSpeak(final String speech) {


    //}


    private void promptSpeechInput() {


            String speech_prompt = "What do you want to know?";
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    speech_prompt);
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(), "Speech Not Supported",
                        Toast.LENGTH_SHORT).show();

                return;
            }



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (v) {
            String res = "";
            super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode) {
                case REQ_CODE_SPEECH_INPUT: {
                    if (resultCode == RESULT_OK && null != data) {

                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        res = result.get(0);
                    }
                    break;
                }

            }
            v = false;
            res = res.toLowerCase();
            System.out.println("Abir: Voice: " + res);
            respond(res);

        }

    }


    public void respond(String res) {
        if (res.equals("help")) {
            Intent sample = new Intent(this, com.example.abirshukla.souschef.Sample.class);
            startActivity(sample);
            return;
        }
        if (res.contains("timer")) {
            Intent openClockIntent = new Intent(AlarmClock.ACTION_SET_TIMER);
            openClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(openClockIntent);
            return;
        }
        else if (res.contains("alarm")) {
            Intent openClockIntent = new Intent(AlarmClock.ACTION_SET_TIMER);
            openClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(openClockIntent);
            return;
        }
        //Intent speak = new Intent(this, Speaker.class);
        String say = "";
        if (res.contains("email") || res.contains("send")) {
            String name = DataForUser.getEmail();
            String sub = "";
            String mess = "";
            if (res.contains("everything")) {
                sub = "Sous Chef Data for "+nameOfDish;
                mess = DataForUser.everything;
                say = "Email Sent with Sous Chef Data";
                rec = name;
                subjectEmail = sub;
                textMessage = mess;

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                session = Session.getDefaultInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("yoursouschef1@gmail.com", "aadi2247");
                    }
                });


                RetreiveFeedTask task = new RetreiveFeedTask();
                task.execute();

            }
            else if (res.contains("long") || res.contains("time")) {
                sub = "Time for "+nameOfDish;
                mess = hm.get("time").toString();
                say = "Email Sent with time";
                subject = "time";


                rec = name;
                subjectEmail = sub;
                textMessage = mess;

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                session = Session.getDefaultInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("yoursouschef1@gmail.com", "aadi2247");
                    }
                });


                RetreiveFeedTask task = new RetreiveFeedTask();
                task.execute();








            }
            else if (res.contains("yield") || res.contains("feed") || res.contains("serv")) {
                sub = "Servings for "+ nameOfDish;
                mess = say = hm.get("servings").toString();
                say = "Email Sent with servings";

                subject = "servings";



                rec = name;
                subjectEmail = sub;
                textMessage = mess;

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                session = Session.getDefaultInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("yoursouschef1@gmail.com", "aadi2247");
                    }
                });


                RetreiveFeedTask task = new RetreiveFeedTask();
                task.execute();






            }
            else if (res.contains("calori") || res.contains("nutrients")) {
                sub = "Servings for "+ nameOfDish;
                mess = say = "This dish has "+hm.get("cal").toString()+" calories";
                say = "Email Sent with calories";

                subject = "calories";



                rec = name;
                subjectEmail = sub;
                textMessage = mess;

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                session = Session.getDefaultInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("yoursouschef1@gmail.com", "aadi2247");
                    }
                });


                RetreiveFeedTask task = new RetreiveFeedTask();
                task.execute();

            }
            else if (res.contains("ingredients")) {
                sub = "Ingredients for "+ nameOfDish;
                say = "Email Sent with Ingredients";
                mess = hm.get("ing").toString();
                subject = "ing";
                rec = name;
                subjectEmail = sub;
                textMessage = mess;

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                session = Session.getDefaultInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("yoursouschef1@gmail.com", "aadi2247");
                    }
                });


                RetreiveFeedTask task = new RetreiveFeedTask();
                task.execute();
            }
            else if(res.contains("instructions") || res.contains("step")) {
                sub = "Instructions for "+ nameOfDish;
                say = "Email Sent with Instructions";
                mess= hm.get("insWhole").toString();
                subject = "insWhole";

                rec = name;
                subjectEmail = sub;
                textMessage = mess;

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                session = Session.getDefaultInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("yoursouschef1@gmail.com", "aadi2247");
                    }
                });


                RetreiveFeedTask task = new RetreiveFeedTask();
                task.execute();
            }
            else if (res.contains("that") || res.contains("those") || res.contains("them")) {
                if (!subject.equals("")) {
                    if (subject.equals("time")) {
                        sub = "Time for "+nameOfDish;
                        mess = hm.get("time").toString();
                        say = "Email Sent with time";


                        rec = name;
                        subjectEmail = sub;
                        textMessage = mess;

                        Properties props = new Properties();
                        props.put("mail.smtp.host", "smtp.gmail.com");
                        props.put("mail.smtp.socketFactory.port", "465");
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.port", "465");

                        session = Session.getDefaultInstance(props, new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication("yoursouschef1@gmail.com", "aadi2247");
                            }
                        });


                        RetreiveFeedTask task = new RetreiveFeedTask();
                        task.execute();
                    }
                    else if (subject.equals("servings")) {
                        sub = "Servings for "+ nameOfDish;
                        mess = say = hm.get("servings").toString();
                        say = "Email Sent with servings";


                        rec = name;
                        subjectEmail = sub;
                        textMessage = mess;

                        Properties props = new Properties();
                        props.put("mail.smtp.host", "smtp.gmail.com");
                        props.put("mail.smtp.socketFactory.port", "465");
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.port", "465");

                        session = Session.getDefaultInstance(props, new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication("yoursouschef1@gmail.com", "aadi2247");
                            }
                        });


                        RetreiveFeedTask task = new RetreiveFeedTask();
                        task.execute();
                    }
                    else if (subject.equals("ing")) {
                        sub = "Ingredients for "+ nameOfDish;
                        say = "Email Sent with Ingredients";
                        mess = hm.get("ing").toString();



                        rec = name;
                        subjectEmail = sub;
                        textMessage = mess;

                        Properties props = new Properties();
                        props.put("mail.smtp.host", "smtp.gmail.com");
                        props.put("mail.smtp.socketFactory.port", "465");
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.port", "465");

                        session = Session.getDefaultInstance(props, new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication("yoursouschef1@gmail.com", "aadi2247");
                            }
                        });


                        RetreiveFeedTask task = new RetreiveFeedTask();
                        task.execute();
                    }
                    else if(subject.equals("insWhole") || subject.equals("insArr")) {
                        sub = "Instructions for "+ nameOfDish;
                        say = "Email Sent with Instructions";
                        mess = hm.get("insWhole").toString();

                        rec = name;
                        subjectEmail = sub;
                        textMessage = mess;

                        Properties props = new Properties();
                        props.put("mail.smtp.host", "smtp.gmail.com");
                        props.put("mail.smtp.socketFactory.port", "465");
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.port", "465");

                        session = Session.getDefaultInstance(props, new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication("yoursouschef1@gmail.com", "aadi2247");
                            }
                        });


                        RetreiveFeedTask task = new RetreiveFeedTask();
                        task.execute();

                    }
                    else {
                        say = "Can not send email, Be sure to include what you want me to send in the email during your voice command";

                    }


                }
                else {
                    say = "Can not send email, Be sure to include what you want me to send in the email during your voice command";

                }
            }
            else {
                say = "Can not send email, Be sure to include what you want me to send in the email during your voice command";
            }
        }//Not Email
        else if (res.contains("long") || res.contains("time")) {
            subject = "time";
            say = hm.get("time").toString();
        }
        else if (res.contains("shop") || res.contains("store")) {
            subject = "shop";
            View v = new View(this);
            goToMap(v);
            return;
        }
        else if (res.contains("yield") || res.contains("feed") || res.contains("serv")) {
            subject = "servings";
            say = hm.get("servings").toString();
        }
        else if (res.contains("calori") || res.contains("nutrients")) {
            subject = "calories";
            say = "This dish has "+hm.get("cal").toString()+" calories";
        }
        else if (res.contains("ingredients")) {
            subject = "ing";
            say = hm.get("ing").toString();
        }
        else if (res.contains("next")) {
            subject = "insWhole";
            String[] steps = (String[]) hm.get("insArr");
            try {
                say = steps[++DataForUser.step];
            }catch (IndexOutOfBoundsException e) {
                say = "No Further Steps";
                DataForUser.step--;
            }
            subject = "step" + Integer.toString(DataForUser.step);
        }
        else if (res.contains("previous")) {
            subject = "insWhole";
            String[] steps = (String[]) hm.get("insArr");
            try {
                say = steps[--DataForUser.step];
            } catch (IndexOutOfBoundsException e) {
                say = "No Previous Steps";
                DataForUser.step++;
            }
            subject = "step" + Integer.toString(DataForUser.step);
        }
        else if(res.contains("instructions")) {
            if(res.contains("step")) {
                System.out.println("Abir: Voice: Step: Exec");
                subject = "insWhole";
                String[] steps = (String[]) hm.get("insArr");
                int checkIns = 0;
                for (int i = 1; i < steps.length + 1; i++) {
                    if (res.contains(Integer.toString(i))) {
                        say = steps[i - 1];
                        DataForUser.step = i-1;
                        checkIns = 1;
                        subject = "step" + Integer.toString(i - 1);

                    }

                }
                if (checkIns == 0) {
                    try {
                        if (res.contains("one")) {
                            say = steps[0];
                            DataForUser.step = 0;
                            subject = "step" + Integer.toString(0);
                        } else if (res.contains("two")) {
                            say = steps[1];
                            DataForUser.step = 1;
                            subject = "step" + Integer.toString(1);
                        } else if (res.contains("three")) {
                            say = steps[2];
                            DataForUser.step = 2;
                            subject = "step" + Integer.toString(2);
                        } else if (res.contains("four")) {
                            say = steps[3];
                            DataForUser.step = 3;
                            subject = "step" + Integer.toString(3);
                        } else if (res.contains("five")) {
                            DataForUser.step = 3;
                            say = steps[4];
                            subject = "step" + Integer.toString(4);
                        } else if (res.contains("six")) {
                            DataForUser.step = 5;
                            say = steps[5];
                            subject = "step" + Integer.toString(5);
                        } else if (res.contains("seven")) {
                            DataForUser.step = 6;
                            say = steps[6];
                            subject = "step" + Integer.toString(6);
                        } else if (res.contains("eight")) {
                            say = steps[7];
                            DataForUser.step = 7;
                            subject = "step" + Integer.toString(7);
                        } else if (res.contains("nine")) {
                            say = steps[8];
                            DataForUser.step = 8;
                            subject = "step" + Integer.toString(8);
                        } else if (res.contains("ten")) {
                            say = steps[9];
                            DataForUser.step = 9;
                            subject = "step" + Integer.toString(9);
                        } else {
                            subject = "insWhole";
                            say = hm.get("insWhole").toString();
                        }
                    }catch (Exception e) {
                        subject = "insWhole";
                        say = hm.get("insWhole").toString();
                    }
                }
            }
            else {
                subject = "insWhole";
                say = hm.get("insWhole").toString();
            }
        }
        else if(res.contains("step")) {
            System.out.println("Abir: Voice: Step: Exec");
            subject = "step";
            String[] steps = (String[]) hm.get("insArr");
            int checkIns = 0;
            for (int i = 1; i < steps.length + 1; i++) {
                if (res.contains(Integer.toString(i))) {
                    say = steps[i - 1];
                    checkIns = 1;
                    DataForUser.step = i-1;
                    subject = "step" + Integer.toString(i - 1);

                }

            }
            if (checkIns == 0) {
                try {
                    if (res.contains("one")) {
                        say = steps[0];
                        DataForUser.step = 0;
                        subject = "step" + Integer.toString(0);
                    } else if (res.contains("two")) {
                        say = steps[1];
                        DataForUser.step = 1;
                        subject = "step" + Integer.toString(1);
                    } else if (res.contains("three")) {
                        say = steps[2];
                        DataForUser.step = 2;
                        subject = "step" + Integer.toString(2);
                    } else if (res.contains("four")) {
                        say = steps[3];
                        DataForUser.step = 3;
                        subject = "step" + Integer.toString(3);
                    } else if (res.contains("five")) {
                        DataForUser.step = 3;
                        say = steps[4];
                        subject = "step" + Integer.toString(4);
                    } else if (res.contains("six")) {
                        DataForUser.step = 5;
                        say = steps[5];
                        subject = "step" + Integer.toString(5);
                    } else if (res.contains("seven")) {
                        DataForUser.step = 6;
                        say = steps[6];
                        subject = "step" + Integer.toString(6);
                    } else if (res.contains("eight")) {
                        say = steps[7];
                        DataForUser.step = 7;
                        subject = "step" + Integer.toString(7);
                    } else if (res.contains("nine")) {
                        say = steps[8];
                        DataForUser.step = 8;
                        subject = "step" + Integer.toString(8);
                    } else if (res.contains("ten")) {
                        say = steps[9];
                        DataForUser.step = 9;
                        subject = "step" + Integer.toString(9);
                    } else {
                        subject = "insWhole";
                        say = hm.get("insWhole").toString();
                    }
                }catch (Exception e) {
                    subject = "insWhole";
                    say = hm.get("insWhole").toString();
                }
            }
        }
        else if (res.contains("take") || res.contains("direct ")) {
            subject = "shop";
            View v = new View(this);
            goToMap(v);
            return;
        }
        else if (res.contains("video") || res.contains("tutorial")) {
            subject = "video";
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setPackage("com.google.android.youtube");
            intent.putExtra("query", "how to make " + nameOfDish);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
        else if (res.contains("fav") && res.contains("add")) {
            DataForUser.favorites.add(dishUrl);
            say = "Dish Added to Favorites";
        }
        else if(res.contains("fav") && res.contains("remove")) {
            DataForUser.favorites.remove(dishUrl);
            say = "Dish Removed Favorites";
        }
        else if (res.contains("that") || res.contains("those") || res.contains("them") || res.contains("repeat")) {
            if (!subject.equals("")) {
                if (subject.equals("video")) {
                    Intent intent = new Intent(Intent.ACTION_SEARCH);
                    intent.setPackage("com.google.android.youtube");
                    intent.putExtra("query", "how to make " + nameOfDish);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return;
                }
                else if(subject.equals("shop")) {
                    View v = new View(this);
                    goToMap(v);
                    return;
                } else if (subject.equals("calories")) {

                    say = "This dish has "+hm.get("cal").toString()+" calories";
                }
                else if(subject.contains("step")) {
                    String r = subject.substring(4);
                    String arr[] = (String[]) hm.get("insArr");
                    int indexA = Integer.parseInt(r);
                    say = arr[indexA];
                }
                else {
                    say = hm.get(subject).toString();
                }
            }
            else {
                System.out.println("Abir: Voice: Subject: "+subject);
                if (subject.contains("step")) {
                    subject = "insWhole";
                    String[] steps = (String[]) hm.get("insArr");
                    int checkIns = 0;
                    for (int i = 1; i < steps.length + 1; i++) {
                        if (res.contains(Integer.toString(i))) {
                            say = steps[i - 1];
                            checkIns = 1;
                            DataForUser.step = i-1;
                            subject = "step" + Integer.toString(i - 1);

                        }

                    }
                    if (checkIns == 0) {
                        try {
                            if (res.contains("one")) {
                                say = steps[0];
                                DataForUser.step = 0;
                                subject = "step" + Integer.toString(0);
                            } else if (res.contains("two")) {
                                say = steps[1];
                                DataForUser.step = 1;
                                subject = "step" + Integer.toString(1);
                            } else if (res.contains("three")) {
                                say = steps[2];
                                DataForUser.step = 2;
                                subject = "step" + Integer.toString(2);
                            } else if (res.contains("four")) {
                                say = steps[3];
                                DataForUser.step = 3;
                                subject = "step" + Integer.toString(3);
                            } else if (res.contains("five")) {
                                DataForUser.step = 3;
                                say = steps[4];
                                subject = "step" + Integer.toString(4);
                            } else if (res.contains("six")) {
                                DataForUser.step = 5;
                                say = steps[5];
                                subject = "step" + Integer.toString(5);
                            } else if (res.contains("seven")) {
                                DataForUser.step = 6;
                                say = steps[6];
                                subject = "step" + Integer.toString(6);
                            } else if (res.contains("eight")) {
                                say = steps[7];
                                DataForUser.step = 7;
                                subject = "step" + Integer.toString(7);
                            } else if (res.contains("nine")) {
                                say = steps[8];
                                DataForUser.step = 8;
                                subject = "step" + Integer.toString(8);
                            } else if (res.contains("ten")) {
                                say = steps[9];
                                DataForUser.step = 9;
                                subject = "step" + Integer.toString(9);
                            } else {
                                subject = "insWhole";
                                say = hm.get("insWhole").toString();
                            }
                        }catch (Exception e) {
                            subject = "insWhole";
                            say = hm.get("insWhole").toString();
                        }
                    }
                } else {


                    say = "Can not understand Voice Command, please check voice command page for sample voice commands";
                }
            }
        }
        //speak.putExtra("res",say);
        //speak.putExtra("nameOfDish", nameOfDish);
        //speak.putExtra("subject",subject);
        //startActivity(speak);
        if (say.length() != 0) {
            Toast.makeText(SousPage.this, say, Toast.LENGTH_LONG).show();
            myTTSA.speak(say, TextToSpeech.QUEUE_FLUSH, null);
        }

        System.out.println("Abir: Poc: Not Saying Anything so listen");
        try {
            recognizer.startListening(KWS_SEARCH);
        }
        catch (Exception e) {

        }
        //t1.speak(say, TextToSpeech.QUEUE_FLUSH, null);


    }

    class RetreiveFeedTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try{
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("yoursouschef1@gmail.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(rec));
                message.setSubject(subjectEmail);
                message.setContent(textMessage, "text/html; charset=utf-8");
                Transport.send(message);
            } catch(MessagingException e) {
                e.printStackTrace();
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            Toast.makeText(getApplicationContext(), "Message Successfully Sent!", Toast.LENGTH_LONG).show();
        }
    }

    public void goToMap(View view) {
        // Search for restaurants nearby
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=Grocery");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

}
