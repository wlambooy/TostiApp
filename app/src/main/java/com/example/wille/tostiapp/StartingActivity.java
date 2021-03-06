package com.example.wille.tostiapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StartingActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;

    private Button signin;
    private Button createaccount;
    private TextView info;

    private boolean[] timeout = new boolean[]{true, false}; // make sure timeout doesn't happen when it shouldn't

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");

        signin = (Button) findViewById(R.id.sign_in);
        createaccount = (Button) findViewById(R.id.create_account);
        info = ((TextView) findViewById(R.id.signinfailed));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) { // check if user is logged in
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {     // Loading animation
                    info.setText("Loading....");
                }
            }, 2000);
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("Loading.....");
                }
            }, 3000);
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("Loading......");
                }
            }, 4000);
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText("Loading.......");
                }
            }, 5000);
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() { // timeout after 6 seconds
                    if (timeout[0]) {
                        timeout[1] = true;
                        auth.signOut();
                        info.setText(getString(R.string.sign_in_failed));
                        signin.setVisibility(View.VISIBLE);
                        createaccount.setVisibility(View.VISIBLE);
                    }
                }
            }, 6000);
            LoginActivity.getUser(currentUser.getUid(), database).addOnCompleteListener(new OnCompleteListener<User>() {
                @Override
                public void onComplete(@NonNull Task<User> task) { // FireBase tends to get stuck here, hence the timeout
                    timeout[0] = false;
                    User user = task.getResult();
                    if (user != null && !timeout[1]) {
                        finish();
                        Intent intent = new Intent(StartingActivity.this, user.getAdmin() ? AdminActivity.class : OrderActivity.class);
                        intent.putExtra("user", user);
                        startActivity(intent); // start main activity
                    }
                }
            });
        } else { // user not logged in, allow login/account creation
            info.setVisibility(View.INVISIBLE);
            signin.setVisibility(View.VISIBLE);
            createaccount.setVisibility(View.VISIBLE);
        }
    }

    public void onButtonClick (View v) {
        startActivity(new Intent(this, v.getId() == R.id.sign_in ? LoginActivity.class : CreateAccountActivity.class));
    }
}
