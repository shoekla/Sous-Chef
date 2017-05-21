package com.example.abirshukla.souschef;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 123;
    private static final int SELECT_FILE = 1234;
    SharedPreferences sharedPref;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    ProgressDialog pd;
    private StorageReference mStorageRef;
    boolean cameraUse = false;
    boolean gal = false;
    boolean scrapePic = false;
    String userIn = "";
    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain", "Bruce","Brian","Brain","Brehh","Bro","banana"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        pd = new ProgressDialog(this);
        pd.setMessage("Acquiring data for your dish...");
        if (sharedPref != null) {
            String email = sharedPref.getString("email", "");
            if (!email.equals("") && DataForUser.getEmail().equals("")) {
                System.out.println(email);
                DataForUser.setEmail(email);
                Toast.makeText(MainActivity.this, email+" signed in", Toast.LENGTH_SHORT).show();
            }

        }
        if (DataForUser.getEmail().length() == 0) {
            Intent s = new Intent(MainActivity.this,Settings.class);
            startActivity(s);
        }
        mStorageRef = FirebaseStorage.getInstance().getReference();
        /*
        final EditText edit_txt = (EditText) findViewById(R.id.editText);

        edit_txt.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    pd.show();
                    userIn = edit_txt.getText().toString();

                    String res = userIn.replace(" ","%20");
                    scrapePic = false;
                    getHTML("https://sous-chef2-0.herokuapp.com/search/"+res+"/");
                    return true;
                }
                return false;
            }
        });
        */
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, COUNTRIES);
        final AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.editText);
        textView.setThreshold(1);
        textView.setAdapter(adapter);
        textView.setDropDownHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        textView.setDropDownWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setOnScrollChangeListener(new AdapterView.OnScrollChangeListener() {

                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                }
            });
        }
        textView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                    userIn = textView.getText().toString();
                    String res = userIn.replace(" ","%20");
                    scrapePic = false;
                    pd.show();
                    getHTML("https://sous-chef2-0.herokuapp.com/search/"+res+"/");
                    //add to firbase
                    handled = true;
                }
                return handled;
            }
        });
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
                userIn = textView.getText().toString();
                String res = userIn.replace(" ","%20");
                scrapePic = false;
                pd.show();
                getHTML("https://sous-chef2-0.herokuapp.com/search/"+res+"/");
                //Toast.makeText(MainActivity.this,"Back Pressed !!!", Toast.LENGTH_SHORT).show();

            }

        });

    }
    public void prom (View view) {
        cameraUse = false;
        promptSpeechInput();
    }
    public void cam(View view) {
        selectImage();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    cameraUse = true;
                    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(i , 0);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    gal = true;
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent s = new Intent(MainActivity.this,Settings.class);
            startActivity(s);
            return true;
        }
        else if (id == R.id.action_favorite) {
            Toast.makeText(MainActivity.this, "Favorites Coming Soon", Toast.LENGTH_SHORT).show();

        }

        return super.onOptionsItemSelected(item);
    }
    private void promptSpeechInput() {
        String speech_prompt = "What is the name of the Dish";
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
    public String getPath(Uri uri, Activity activity) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (gal) {
            Uri selectedImageUri = data.getData();

            String imagePath = getPath(selectedImageUri, MainActivity.this);
            //Bitmap bitmapImage = BitmapFactory.decodeFile(imagePath );
            //imageView.setImageBitmap(bitmapImage );
            System.out.println("Abir Image Path: "+imagePath);
            String imageName = imagePath.substring(imagePath.lastIndexOf("/"));
            Uri file = Uri.fromFile(new File(imagePath));
            final String pathPython = DataForUser.getFirbaseName()+imageName;
            StorageReference riversRef = mStorageRef.child(pathPython);

            riversRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            scrapePic = true;
                            getHTML("http://abirshukla.pythonanywhere.com/sousC/"+pathPython+"/");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });
            pd.show();

            gal = false;
            return;
        }
        if (cameraUse) {
            if (data != null) {

                String[] projection = { MediaStore.Images.Media.DATA };
                Cursor cursor = null;
                cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

                int column_index_data = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToLast();

                String imagePath = cursor.getString(column_index_data);
                //Bitmap bitmapImage = BitmapFactory.decodeFile(imagePath );
                //imageView.setImageBitmap(bitmapImage );
                System.out.println("Abir Image Path: "+imagePath);
                String imageName = imagePath.substring(imagePath.lastIndexOf("/"));
                Uri file = Uri.fromFile(new File(imagePath));
                final String pathPython = DataForUser.getFirbaseName()+imageName;
                StorageReference riversRef = mStorageRef.child(pathPython);

                riversRef.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                scrapePic = true;
                                getHTML("http://abirshukla.pythonanywhere.com/sousC/"+pathPython+"/");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                            }
                        });
                pd.show();
                cameraUse = false;





            }

        }
        else {
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


            //Intent speak = new Intent(MainActivity.this, speaker.class);
            //speak.putExtra("res",res);
            //startActivity(speak);
        /*pd.show();
        a.putExtra("nameOfDish", res);
        res = res.replace(" ","%20");
        String url = "http://abirshukla.pythonanywhere.com/searchCook/"+res+"/";
        getHTML(url);
        */
            if (res.length() < 1) {
                return;
            }

            Toast.makeText(MainActivity.this, "You Said: " + res, Toast.LENGTH_SHORT).show();
            pd.show();
            userIn = res;
            res = res.replace(" ","%20");
            scrapePic = false;
            getHTML("https://sous-chef2-0.herokuapp.com/search/"+res+"/");
        }

    }
    public void getHTML(final String url) {
        if (scrapePic) {
            System.out.println("Begin HTML");
            System.out.println("Abir Final Url: " + url);
            final String[] d = new String[1];
            Ion.with(getApplicationContext())
                    .load(url)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            Intent r = new Intent(MainActivity.this, Results.class);
                            //System.out.println("Abir: " + result);
                            r.putExtra("query", "picture input");
                            r.putExtra("result", result);
                            scrapePic = false;
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
        else {
            final String[] d = new String[1];
            Ion.with(getApplicationContext())
                    .load(url)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            Intent r = new Intent(MainActivity.this, SearchDish.class);
                            //System.out.println("Abir: " + result);
                            r.putExtra("query", userIn);
                            r.putExtra("result", result);
                            scrapePic = false;
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
    }
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("email",DataForUser.getEmail());
        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor editor = sharedPref.edit();
        String email = DataForUser.getEmail();
        editor.putString("email", email);
        editor.commit();
        super.onDestroy();
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String email = savedInstanceState.getString("email");
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
}
