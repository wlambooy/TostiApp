package com.example.wille.tostiapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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
    private int counter = 0;

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

        ((TextView) findViewById(R.id.loginid)).setText("Logged in as " + user.getEmail());

        orders.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((Button) findViewById(R.id.order)).setClickable(false);
                ((Button) findViewById(R.id.order)).setAlpha(.5f);
                if (dataSnapshot.hasChild(date)) {
                    boolean finished = false;
                    if (dataSnapshot.child(date).hasChild("finished")) // "max" is set the first when initializing a day so this check must be made
                        if ((boolean) dataSnapshot.child(date).child("finished").getValue()) { // day is finished
                            ((TextView) findViewById(R.id.tostis_left)).setText(getString(R.string.tostis_finished));
                            finished = true;
                        }
                    if ((long) dataSnapshot.child(date).child("max").getValue() > 0 && !finished) { // regular case
                        ((Button) findViewById(R.id.order)).setClickable(true);
                        ((Button) findViewById(R.id.order)).setAlpha(1f);
                        getMaxOrder(dataSnapshot.child(date));
                    } else if (!finished) // "max" is set to 0
                        ((TextView) findViewById(R.id.tostis_left)).setText(getString(R.string.no_tostis_today));
                    ((LinearLayout) findViewById(R.id.orders)).removeAllViews();
                    ((LinearLayout) findViewById(R.id.orderslayout)).setVisibility(View.GONE);
                    for (final DataSnapshot i : dataSnapshot.child(date).getChildren()) { // list your current orders
                        Order order;
                        try {
                             order = i.getValue(Order.class);
                        } catch (Exception e) {
                            continue;
                        }
                        if (order.getUid().equals(user.getUid()) && !(order.getReady() && order.getReceived())) {
                            Button b = new Button(OrderActivity.this);
                            b.setTypeface(Typeface.MONOSPACE);
                            b.setText(String.format("%s  -  %d  -  %c%c", order.getName(), order.getAmount(),
                                    order.getWithHam() ? 'H' : ' ', order.getWithCheese() ? 'C' : ' '));
                            b.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            b.setClickable(false);
                            if (!order.getReady()) // make orders that are ready pop out
                                b.setAlpha(0.5f);
                            LinearLayout ll = new LinearLayout(OrderActivity.this);
                            ll.setOrientation(LinearLayout.HORIZONTAL);
                            CheckBox cb = new CheckBox(OrderActivity.this);
                            cb.setText("Received");
                            cb.setChecked(order.getReceived());
                            cb.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    today.child(i.getKey()).child("received").setValue(((CheckBox) v).isChecked());
                                }
                            });
                            b.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT, 2f));
                            ll.addView(b);
                            ll.addView(cb);
                            ((LinearLayout) findViewById(R.id.orders)).addView(ll);
                            ((LinearLayout) findViewById(R.id.orderslayout)).setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    ((TextView) findViewById(R.id.tostis_left)).setText(getString(R.string.no_tostis_available));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        users.addValueEventListener(new ValueEventListener() { // update saldo
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
        if (id == R.id.action_settings) { // sign out menu item
            auth.signOut();
            finish();
            startActivity(new Intent(this, StartingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getMaxOrder(DataSnapshot list) {
        // textviews with tostis_ordered and user_ordered
        TextView tostis_left = findViewById(R.id.tostis_left);
        try {
            max = Integer.parseInt(list.child("max").getValue().toString());
            tostis_ordered = Integer.parseInt(list.child("tostis_ordered").getValue().toString());
            counter = Integer.parseInt(list.child("counter").getValue().toString());
        } catch (Exception e) {
            max = 1; tostis_ordered = 0; counter = 0; // since the "max", "tostis_ordered" and "counter" do not get initialized at the same time
        }
        remaining = max - tostis_ordered;
        if (remaining <= 0)
            today.child("finished").setValue(true);

        user_ordered = getOrderedUser(list);
        tostis_left.setText(String.format(getString(R.string.tostis_remaining),remaining));
        if (user_ordered == 1) { // user ordered one tosti
            ((RadioButton) findViewById(R.id.amount_2)).setClickable(false);
            ((RadioButton) findViewById(R.id.amount_2)).setAlpha(0.5f);
            ((RadioButton) findViewById(R.id.amount_1)).performClick();
        } else if (user_ordered >= 2) { // user ordered the maximum amount for today
            ((RadioButton) findViewById(R.id.amount_2)).setClickable(false);
            ((RadioButton) findViewById(R.id.amount_2)).setAlpha(0.5f);
            ((RadioButton) findViewById(R.id.amount_1)).setClickable(false);
            ((RadioButton) findViewById(R.id.amount_1)).setAlpha(0.5f);
            ((CheckBox) findViewById(R.id.ham)).setClickable(false);
            ((CheckBox) findViewById(R.id.ham)).setAlpha(0.5f);
            ((CheckBox) findViewById(R.id.cheese)).setClickable(false);
            ((CheckBox) findViewById(R.id.cheese)).setAlpha(0.5f);
            ((Button) findViewById(R.id.order)).setClickable(false);
            ((Button) findViewById(R.id.order)).setAlpha(.5f);
            tostis_left.setText(getString(R.string.user_max_reached));
        }
    }

    private long getOrderedUser(DataSnapshot list) { // returns the amount of tostis user has ordered today
        long total_ordered = 0;
        for (DataSnapshot order : list.getChildren())
            if(order.hasChild("uid"))
                if (order.child("uid").getValue().toString().equals(user.getUid()))
                    total_ordered += (long) order.child("amount").getValue();
        return total_ordered;
    }

    @SuppressLint("DefaultLocale")
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
            today.child(String.valueOf(counter)).setValue(order);
            today.child("counter").setValue(counter+1);
            today.child("tostis_ordered").setValue(tostis_ordered + amount);
            users.child(user.getUid()).child("saldo").setValue(user.getSaldo()-0.50*amount); // make new order
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
