package com.example.abirshukla.souschef;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashMap;
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


public class Voice extends Activity {
    TextView name;
    ProgressDialog pd;
    TextView info;
    HashMap hm;
    Button ask;
    String nameOfDish;
    String namError;
    String subject;
    ProgressDialog pdialog = null;
    Session session = null;
    Context context = null;
    String rec, subjectEmail, textMessage;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Sous Chef");

        hm = new HashMap();
        Bundle dish = getIntent().getExtras();
        String code = dish.getString("code");
        subject = dish.getString("subject");
        name = (TextView) findViewById(R.id.title);
        pd = new ProgressDialog(this);
        pd.setMessage("Acquiring data for your dish...");
        name.setText("Code:" + code);
        nameOfDish = dish.getString("nameOfDish");
        String creator = getCreator(code);
        namError = nameOfDish;
        nameOfDish = getName(code);
        name.setText(nameOfDish + " by " + creator);
        pd.dismiss();
        info = (TextView) findViewById(R.id.info);
        System.out.println("Code is: "+code);
        String time = getTime(code);
        String servings = getServing(code);
        String ing = getIngredients(code);
        String ins = getInstructions(code);
        String link = dish.getString("link");
        String infoText = time+"\n\n"+servings+"\n\n"+ing+"\n\n"+ins;
        if (infoText.contains("<html>")) {
            Intent er = new Intent(this,Error.class);
            er.putExtra("name",namError);
            startActivity(er);
        }
       // myFirebaseRef = new Firebase("https://sousche.firebaseio.com/");
        info.setText(infoText);
        ask = (Button) findViewById(R.id.ask);
        ask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();

            }
        });
    }
    public String getName(String code) {
        int index = code.indexOf("<h1 itemprop=\"name\">");
        int index2 = code.indexOf(">",index);
        int index3 = code.indexOf("</h1>",index);
        if (index3 == -1)
            return "<html>";
        return code.substring(index2+1,index3);
    }
    private void promptSpeechInput() {
        String speech_prompt = "What Do you want to know";
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

        respond(res);

    }
    public void respond(String res) {
        res = res.toLowerCase();
        Intent speak = new Intent(this, Speaker.class);
        String say = "";
        if (res.contains("email") || res.contains("send")) {
            String name = DataForUser.getEmail();
            String sub = "";
            String mess = "";
            if (res.contains("long") || res.contains("time")) {
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
        }
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
        else if (res.contains("ingredients")) {
            subject = "ing";
            say = hm.get("ing").toString();
        }
        else if(res.contains("instructions")) {
            subject = "insWhole";
            String[] steps = (String[]) hm.get("insArr");
            int checkIns = 0;
            for (int i = 1; i < steps.length+1; i++) {
                if (res.contains(Integer.toString(i))) {
                    say = steps[i-1];
                    checkIns = 1;
                    subject = "step"+Integer.toString(i-1);

                }
            }
            if (checkIns == 0) {
                subject = "insWhole";
                say = hm.get("insWhole").toString();
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
        } else if (res.contains("step")) {

            String[] steps = (String[]) hm.get("insArr");
            int checkIns = 0;
            for (int i = 1; i < steps.length+1; i++) {
                if (res.contains(Integer.toString(i))) {
                    say = steps[i-1];
                    checkIns = 1;
                    subject = "step"+Integer.toString(i-1);

                }
            }
            if (checkIns == 0) {
                say = "Can not understand Voice Command, please check voice command page for sample voice commands";
            }
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
                say = "Can not understand Voice Command, please check voice command page for sample voice commands";
            }
        }
        speak.putExtra("res",say);
        speak.putExtra("nameOfDish", nameOfDish);
        speak.putExtra("subject",subject);
        startActivity(speak);
    }
    public void goToMap(View view) {
        // Search for restaurants nearby
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=Grocery");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
    public void getHTML(String mess,String name, String sub) {
        mess = mess.replace(" ","%20");
        sub = sub.replace(" ","%20");
        String url = "http://abirshukla.pythonanywhere.com/email/"+name+"/"+sub+"/"+mess+"/";
        System.out.println("Begin HTML");
        System.out.println("Final Url: " + url);
        final String[] d = new String[1];
        Ion.with(getApplicationContext())
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        System.out.println("First Result: " + result);

                    }
                });
    }
    public String getTime(String code) {
        int index = code.indexOf("Total Time:");
        int index2 = code.indexOf("Prep:");
        int index3 = code.indexOf("Cook:");
        if (index3 == -1)
            return "<html>";
        String res = "Total time: ";
        res = "Total Time "+code.substring(index+34,code.indexOf("</dd>",index+34))+ ", Prep Time "+code.substring(index2+14,code.indexOf("</dd>",index2+14))+", Cook Time "+code.substring(index3+14,code.indexOf("</dd>",index3+14));
        res = res.replace(" hr "," hour ");
        res = res.replace(" hr,"," hour,");
        res = res.replace(" hr"," hour");
        res = res.replace(" min "," minutes ");
        res = res.replace(" min"," minutes");
        res = res.replace(" min,"," minutes,");
        hm.put("time", res);
        return res;
    }
    public String takeOutHtml(String str) {
        int check = 0;
        String res = "";
        for (int i = 0; i < str.length();i++) {
            if (str.charAt(i) == '<') {
                check = 1;
            }
            if (str.charAt(i) == '>') {
                check = 0;
            }
            if (check == 0) {
                res = res + str.charAt(i);
            }
        }
        res = res.replace(">","");
        res = res.replace("  ","");
        String adjusted = res.replaceAll("(?m)^[ \t]*\r?\n", "");

        return adjusted;
    }
    public String getServing(String code) {
        int index = code.indexOf("Yield:");
        if (index == -1) {
            return "1 Serving";
        }
        String res = "Servings: "+code.substring(code.indexOf("<dd>",index+5)+4,code.indexOf("</dd>",index+5));
        hm.put("servings",res);
        return "Servings: "+code.substring(code.indexOf("<dd>",index+5)+4,code.indexOf("</dd>",index+5));
    }

    public String getIngredients(String code) {
        int index = code.indexOf("<h6>Ingredients</h6>");
        if (index == -1) {
            return "No Ingredients";
        }
        int index2 = code.indexOf("</ul>", index);
        String res = takeOutHtml(code.substring(index, index2));
        res = res.replace("Ingredients","Ingredients:");
        hm.put("ing", res);
        return res;
    }

    public String getInstructions(String code) {
        int index = code.indexOf("<h6>Directions</h6>");
        if (index == -1) {
            return "No Instructions";
        }
        int index2 = code.indexOf("</ul>", index);
        String r = code.substring(index, index2);
        String res = takeOutHtml(r);
        res = res.replace("Directions","");
        res = res.replace("Click here to see how it's made.","");
        res = res.replace("Watch how to make this recipe.","");
        String[] arr = res.split(".");
        System.out.println("Arr Length: "+arr.length);
        String cont = res.replace(".","abir");
        String resultString = "Directions: ";
        String[] array = cont.split("abir");
        String[] resultArray = new String[array.length];
        for (int i = 0; i< array.length;i++) {
            resultString = resultString+System.getProperty("line.separator")+System.getProperty("line.separator")+"Step "+(i+1)+": "+array[i];
            resultArray[i] = "Step "+(i+1)+" "+array[i];
        }
        hm.put("insArr",resultArray);
        hm.put("insWhole",res);
        return resultString;
    }

    public String getCreator(String code) {
        int index = code.indexOf("Recipe courtesy of");
        int indexA =code.indexOf("\"name\">",index);
        int index2 = code.indexOf("</span>", index);
        if (index == -1 || index2 == -2) {
            return "";
        }
        String res = code.substring(indexA + 7, index2);
        hm.put("creator", res);
        return code.substring(indexA+7,index2);
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
    public void takeToSample(View view) {
        Intent sample = new Intent(this, com.example.abirshukla.souschef.Sample.class);
        startActivity(sample);
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

            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
        }
    }

}
