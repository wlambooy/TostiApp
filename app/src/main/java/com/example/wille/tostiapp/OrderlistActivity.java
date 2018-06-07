package com.example.wille.tostiapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderlistActivity extends AppCompatActivity {

    private DatabaseReference orders;

    private Map<Button, Integer> ranks = new HashMap<>();
    private List<Integer> ready = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderlist);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(new Date());
        orders = FirebaseDatabase.getInstance().getReference("orders").child(date);
        orders.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.orderlist);
                ll.removeAllViews();
                LinearLayout llr = (LinearLayout) findViewById(R.id.strikelist);
                llr.removeAllViews();
                for (int i = 0; dataSnapshot.hasChild(""+i); i++) {
                    Order order = dataSnapshot.child("" + i).getValue(Order.class);
                    Button b = new Button(OrderlistActivity.this);
                    b.setTypeface(Typeface.MONOSPACE);
                    b.setText(String.format("%s  -  %d  -  %c%c", order.getName(), order.getAmount(),
                            order.getWithHam() ? 'H' : ' ', order.getWithCheese() ? 'C' : ' '));
                    b.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    ranks.put(b, i);
                    if (order.getReady()) {
                        if (!order.getReceived()) {
                            b.setAlpha(0.5f);
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onButClick2(v);
                                }
                            });
                            ready.add(i);
                            llr.addView(b);
                        }
                    } else {
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onButClick(v);
                            }
                        });
                        ll.addView(b);
                    }
                }
                ((TextView) findViewById(R.id.ready)).setVisibility(
                        llr.getChildCount() > 0 ?
                                View.VISIBLE : View.INVISIBLE);
                ((TextView) findViewById(R.id.nothing)).setVisibility(
                       ll.getChildCount() <= 0 && llr.getChildCount() <= 0 ?
                                View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onButClick (View v) {
        Button but = (Button) v;
        but.setAlpha(0.5f);
        ((LinearLayout) findViewById(R.id.orderlist)).removeView(but);
        ((LinearLayout) findViewById(R.id.strikelist)).addView(but);
        ((TextView) findViewById(R.id.ready)).setVisibility(View.VISIBLE);
        ready.add(ranks.get(but));
        orders.child(""+ranks.get(but)).child("ready").setValue(true);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButClick2(v);
            }
        });
    }

    public void onButClick2 (View v) {
        Button but = (Button) v;
        ((LinearLayout) findViewById(R.id.strikelist)).removeView(but);
        int num = 0;
        for (int i : ready)
            if (i < ranks.get(but))
                num++;
        ((LinearLayout) findViewById(R.id.orderlist)).addView(but,ranks.get(but)-num);
        but.setAlpha(1f);
        ready.remove(ranks.get(but));
        orders.child(""+ranks.get(but)).child("ready").setValue(false);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButClick(v);
            }
        });
    }

    public void gotoHistory (View v) {
        startActivity(new Intent(this, HistoryActivity.class));
    }
}
