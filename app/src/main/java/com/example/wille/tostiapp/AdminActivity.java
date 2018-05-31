package com.example.wille.tostiapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminActivity extends AppCompatActivity {

    private DatabaseReference database;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        date = formatter.format(new Date());
        database = FirebaseDatabase.getInstance().getReference("orders");
    }

    public void setupTostis (View v) {
        int num;
        try {
            num = Integer.parseInt(((EditText) findViewById(R.id.editText)).getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a maximum amount of tostis", Toast.LENGTH_LONG).show();
            return;
        }
        database.child(date).child("max").setValue(num);
        database.child(date).child("tostis_ordered").setValue(0);
    }
}
