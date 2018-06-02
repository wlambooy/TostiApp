package com.example.wille.tostiapp;

import android.app.ListActivity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;
    private String date;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        auth = FirebaseAuth.getInstance();
        user = (User) getIntent().getExtras().get("user");
        assert(user != null);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        date = formatter.format(new Date());
        database = FirebaseDatabase.getInstance().getReference("orders");

        ((TextView) findViewById(R.id.login)).setText("Logged in as " + user.getEmail());

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(date)) {
                    findViewById(R.id.settosti1).setVisibility(View.GONE);
                    findViewById(R.id.settosti2).setVisibility(View.GONE);
                    findViewById(R.id.settosti3).setVisibility(View.GONE);
                    findViewById(R.id.resettosti).setVisibility(View.VISIBLE);
                }
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
        database.child(date).child("finished").setValue(false);
        database.child(date).child("counter").setValue(0);
        Toast.makeText(this, "Amount set", Toast.LENGTH_LONG).show();
    }

    public void gotoOrders (View v) {
        startActivity(new Intent(this, OrderlistActivity.class));
    }

    public void finishToday (View v) {
        database.child(date).child("finished").setValue(true);
        Toast.makeText(this, "Day finished", Toast.LENGTH_LONG).show();
    }

    public void gotoTopUp (View v) {
        startActivity(new Intent(this, TopUpActivity.class));
    }

    public void gotoOrder (View v) {
        Intent intent = new Intent(this, OrderActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    public void showSet (View v) {
        findViewById(R.id.settosti1).setVisibility(View.VISIBLE);
        findViewById(R.id.settosti2).setVisibility(View.VISIBLE);
        findViewById(R.id.settosti3).setVisibility(View.VISIBLE);
        findViewById(R.id.resettosti).setVisibility(View.GONE);
    }
}
