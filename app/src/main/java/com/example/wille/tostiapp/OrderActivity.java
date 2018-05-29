package com.example.wille.tostiapp;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.RadioButton;
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
    private DatabaseReference database;
    private DatabaseReference today;
    private String date;

    private CheckBox ham;
    private CheckBox cheese;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        // is there a list for today?

        database = FirebaseDatabase.getInstance().getReference("orders");
        user = (User) getIntent().getExtras().get("user");
        assert(user != null);

        ham     = (CheckBox) findViewById(R.id.ham);
        cheese  = (CheckBox) findViewById(R.id.cheese);


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        date = formatter.format(new Date());

        database.addListenerForSingleValueEvent(new ValueEventListener() {
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
            user_tostis.setText(String.format(getString(R.string.tostis_remaining_1),2-user_ordered));
            tostis_left.setText(String.format(getString(R.string.tostis_remaining_2),remaining));
            if(user_ordered < 2) {

            } else {
                // error: user ordered 2
            }
        } else {
            // error message: max tostis
        }
    }

    private int getOrderedUser(DataSnapshot list) {
        int total_ordered = 0;

        for(DataSnapshot order : list.getChildren())
            if(order.hasChild("uid"))
                if (order.child("uid").getValue().toString().equals(user.getUID()))
                    total_ordered += (int) order.child("amount").getValue();

        return total_ordered;
    }

    public void makeOrder (View v) {
        Order order = new Order(user.getName(), user.getUID(),
                ((RadioButton) findViewById(R.id.amount_1)).isChecked() ? 1 : 2,
                ham.isChecked(), cheese.isChecked());
        database.child(date).child("1337").setValue(order);
        Snackbar.make(findViewById(R.id.root), "Order placed", Snackbar.LENGTH_LONG).show();
    }
}
