package com.example.wille.tostiapp;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(new Date());
        DatabaseReference orders = FirebaseDatabase.getInstance().getReference("orders").child(date);
        orders.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.history);
                ll.removeAllViews();
                for (int i = 0; dataSnapshot.hasChild(""+i); i++) {
                    Order order = dataSnapshot.child("" + i).getValue(Order.class);
                    if (!(order.getReady() && order.getReceived()))
                        continue;
                    Button b = new Button(HistoryActivity.this);
                    b.setTypeface(Typeface.MONOSPACE);
                    b.setText(String.format("%s  -  %d  -  %c%c", order.getName(), order.getAmount(),
                            order.getWithHam() ? 'H' : ' ', order.getWithCheese() ? 'C' : ' '));
                    b.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    b.setClickable(false);
                    b.setAlpha(0.5f);
                    ll.addView(b);
                }
                if (ll.getChildCount() <= 0) {
                    ((TextView) findViewById(R.id.nothing)).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.readyreceived)).setVisibility(View.INVISIBLE);
                } else {
                    ((TextView) findViewById(R.id.nothing)).setVisibility(View.INVISIBLE);
                    ((TextView) findViewById(R.id.readyreceived)).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
