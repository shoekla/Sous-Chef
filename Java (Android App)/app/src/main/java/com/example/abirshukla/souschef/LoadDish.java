package com.example.abirshukla.souschef;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoadDish extends AppCompatActivity {
    SharedPreferences sharedPref;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_dish);

        pd = new ProgressDialog(LoadDish.this);
        pd.setMessage("Loading Data...");
        pd.show();
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myR = database.getReference("food");
        myR.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (DataForUser.foodDishes.length() == 0) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    DataForUser.foodDishes = dataSnapshot.getValue(String.class);
                    System.out.println("Abir: foodDish: " + DataForUser.foodDishes);
                    Intent m = new Intent(LoadDish.this, MainActivity.class);
                    pd.hide();
                    pd.dismiss();
                    startActivity(m);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

}
