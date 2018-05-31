package com.example.wille.tostiapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderActivity extends AppCompatActivity {
    private User user;
    private FirebaseAuth auth;
    private DatabaseReference orders;
    private DatabaseReference users;
    private DatabaseReference today;
    private String date;

    private int max = 0;
    private int tostis_ordered = 0;
    private int remaining = max - tostis_ordered;
    private long user_ordered = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        // is there a list for today?

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        date = formatter.format(new Date());

        auth = FirebaseAuth.getInstance();
        orders = FirebaseDatabase.getInstance().getReference("orders");
        users = FirebaseDatabase.getInstance().getReference("Users");
        today = orders.child(date);
        user = (User) getIntent().getExtras().get("user");
        assert(user != null);

        orders.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(date) && (long) dataSnapshot.child(date).child("max").getValue() > 0) {
                    ((Button) findViewById(R.id.order)).setClickable(true);
                    ((Button) findViewById(R.id.order)).setAlpha(1f);
                    getMaxOrder(dataSnapshot.child(date));
                } else {
                    ((Button) findViewById(R.id.order)).setClickable(false);
                    ((Button) findViewById(R.id.order)).setAlpha(.5f);
                    ((TextView) findViewById(R.id.tostis_left)).setText(getString(R.string.no_tostis_available));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LoginActivity.getUser(user.getUid(), users).addOnCompleteListener(new OnCompleteListener<User>() {
                    @Override
                    public void onComplete(@NonNull Task<User> task) {
                        user = task.getResult();
                        ((TextView) findViewById(R.id.saldo)).setText(String.format("€%1.2f", user.getSaldo()));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            auth.signOut();
            finish();
            startActivity(new Intent(this, StartingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getMaxOrder(DataSnapshot list) {
        // textviews with tostis_ordered and user_ordered
        TextView user_tostis = findViewById(R.id.user_tostis);
        TextView tostis_left = findViewById(R.id.tostis_left);

        max = Integer.parseInt(list.child("max").getValue().toString());
        tostis_ordered =  Integer.parseInt(list.child("tostis_ordered").getValue().toString());
        remaining = max - tostis_ordered;

        user_ordered = getOrderedUser(list);
        user_tostis.setText(String.format(getString(R.string.tostis_remaining_1),2-user_ordered));
        tostis_left.setText(String.format(getString(R.string.tostis_remaining_2),remaining));
    }

    private long getOrderedUser(DataSnapshot list) {
        long total_ordered = 0;
        for (DataSnapshot order : list.getChildren())
            if(order.hasChild("uid"))
                if (order.child("uid").getValue().toString().equals(user.getUid()))
                    total_ordered += (long) order.child("amount").getValue();
        return total_ordered;
    }

    public void makeOrder (View v) {
        int amount = ((RadioButton) findViewById(R.id.amount_1)).isChecked() ? 1 : 2;
        boolean withHam = ((CheckBox) findViewById(R.id.ham)).isChecked();
        boolean withCheese = ((CheckBox) findViewById(R.id.cheese)).isChecked();
        if (
                remaining - amount > 0 &&
                user_ordered + amount <= 2 &&
                user.getSaldo() >= 0.50*amount &&
                (withHam || withCheese)) {
            Order order = new Order(user.getName(), user.getUid(), amount, withHam, withCheese);
            today.child(String.valueOf(tostis_ordered)).setValue(order);
            today.child("tostis_ordered").setValue(tostis_ordered + amount);
            users.child(user.getUid()).child("saldo").setValue(user.getSaldo()-0.50*amount);
            Snackbar.make(findViewById(R.id.root), "Order placed", Snackbar.LENGTH_LONG).show();
        } else {
            if (remaining <= 0)
                Toast.makeText(this, "Not enough tostis left", Toast.LENGTH_LONG).show();
            else if (user_ordered + amount > 2)
                Toast.makeText(this, "You cannot order more than 2 tostis per day", Toast.LENGTH_LONG).show();
            else if (!(withHam || withCheese))
                Toast.makeText(this, "Ordering a tosti without ham or cheese is not allowed", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Not enough saldo", Toast.LENGTH_LONG).show();
        }
    }
}
