package com.example.wille.tostiapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private User user;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = (User) getIntent().getExtras().get("user");
        assert(user != null);

        setTitle("Hello " + user.getName().split(" ")[0]);

        findViewById(R.id.button2).setVisibility(View.GONE);

        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LoginActivity.getUser(user.getUid(), FirebaseDatabase.getInstance().getReference("Users")).addOnCompleteListener(new OnCompleteListener<User>() {
                    @Override
                    public void onComplete(@NonNull Task<User> task) {
                        user = task.getResult();
                        findViewById(R.id.button2).setVisibility(user.getAdmin() ? View.VISIBLE : View.GONE);
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

    public void switchActivity (View v) {
        String text = ((Button) v).getText().toString();
        Intent intent = new Intent(this, text.equals("Order") ? OrderActivity.class : AdminActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }
}
