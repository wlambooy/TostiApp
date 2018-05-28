package com.example.wille.tostiapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class OrderActivity extends AppCompatActivity {
    private User user;
    private FirebaseDatabase database;
    private DatabaseReference today;
    private String date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        // is there a list for today?

        database = FirebaseDatabase.getInstance();
        user = (User) getIntent().getExtras().get("user");
        assert(user != null);


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        date = formatter.format(new Date());

        database.getReference("orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(date)) {
                    getMaxOrder(snapshot.child(date));
                } else {
                    // error message: no list for today
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getMaxOrder(DataSnapshot list) {
        // textviews with tostis_ordered and user_ordered
        TextView user_tostis = findViewById(R.id.user_tostis);
        TextView tostis_left = findViewById(R.id.tostis_left);

        int max = Integer.parseInt(list.child("max").getValue().toString());
        int tostis_ordered =  Integer.parseInt(list.child("tostis_ordered").getValue().toString());
        int remaining = max - tostis_ordered;

        if(remaining > 0) {
            int user_ordered = getOrderedUser(list);
            user_tostis.setText(String.format("You have %d of your 2 tostis per day remaining!",2-user_ordered));
            tostis_left.setText(String.format("There are %d tostis remaining",remaining));
            if(user_ordered < 2) {
                setSpinner(Math.min(2-user_ordered,remaining));

            } else {
                // error: user ordered 2
            }
        } else {
            // error message: max tostis
        }
    }

    private void setSpinner(int nr) {
        Spinner spinner = findViewById(R.id.amount);
        String[] items = new String[nr];
        for(int i=0; i < nr; i++)
            items[i] = Integer.toString(i+1);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items));
    }

    private int getOrderedUser(DataSnapshot list) {
        int total_ordered = 0;

        for(DataSnapshot order : list.getChildren())
            if(order.hasChild("uid"))
                if (order.child("uid").getValue().toString().equals(user.getUID()))
                    total_ordered += (int) order.child("amount").getValue();

        return total_ordered;
    }
}
