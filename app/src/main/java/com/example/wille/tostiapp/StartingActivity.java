package com.example.wille.tostiapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");

        signin = (Button) findViewById(R.id.sign_in);
        createaccount = (Button) findViewById(R.id.create_account);

        signin.setVisibility(View.GONE);
        createaccount.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            LoginActivity.getUser(currentUser.getUid(), database).addOnCompleteListener(new OnCompleteListener<User>() {
                @Override
                public void onComplete(@NonNull Task<User> task) {
                    User user = task.getResult();
                    if (user != null) {
                        finish();
                        Intent intent = new Intent(StartingActivity.this, user.getAdmin() ? AdminActivity.class : OrderActivity.class);
                        intent.putExtra("user", user);
                        startActivity(intent);
                    }
                }
            });
        } else {
            signin.setVisibility(View.VISIBLE);
            createaccount.setVisibility(View.VISIBLE);
        }
    }

    public void onButtonClick (View v) {
        if (v.getVisibility() == View.VISIBLE)
            startActivity(new Intent(this, v.getId() == R.id.sign_in ? LoginActivity.class : CreateAccountActivity.class));
    }
}
