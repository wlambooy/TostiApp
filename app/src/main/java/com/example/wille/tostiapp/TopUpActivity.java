package com.example.wille.tostiapp;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class TopUpActivity extends AppCompatActivity {

    private DatabaseReference database;

    private EditText searchbar;
    private LinearLayout results;

    private DataSnapshot users = null;
    private User selecteduser = null;
    private Map<Button, String> uids = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);

        database = FirebaseDatabase.getInstance().getReference("Users");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                getSnapshot(database).addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) { // save datasnapshot of user database
                        users = task.getResult();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        searchbar = findViewById(R.id.search);
        results = findViewById(R.id.results);

        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();
                if (users != null && query.length() > 0) { // find users by a string contains method for name and email
                    results.removeAllViews();
                    uids.clear();
                    for (DataSnapshot i : users.getChildren()) { // list users satisfying the search conditions
                        if (((String) i.child("name").getValue()).toLowerCase().contains(query) ||
                                ((String) i.child("email").getValue()).toLowerCase().contains(query)) {
                            Button b = new Button(TopUpActivity.this);
                            b.setText(i.child("name").getValue() + "  -  " + i.child("email").getValue());
                            b.setTypeface(Typeface.MONOSPACE);
                            b.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                            uids.put(b, (String) i.child("uid").getValue());
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) { // on click of a user, show top up menu
                                    LoginActivity.getUser(uids.get(v), database).addOnCompleteListener(new OnCompleteListener<User>() {
                                        @Override
                                        public void onComplete(@NonNull Task<User> task) {
                                            selecteduser = task.getResult();
                                            ((TextView) findViewById(R.id.emailid)).setText(selecteduser.getEmail());
                                            ((LinearLayout) findViewById(R.id.topup)).setVisibility(View.VISIBLE);
                                            ((EditText) findViewById(R.id.amount)).requestFocus();
                                        }
                                    });
                                }
                            });
                            results.addView(b);
                        }
                    }
                }
            }
        });
    }

    public void topUp (View v) { // top up saldo of the selected user
        if (selecteduser != null) {
            double amount;
            try {
                amount = Double.parseDouble(((EditText) findViewById(R.id.amount)).getText().toString());
            } catch (Exception e) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_LONG).show();
                return;
            }
            if (amount <= 0) {
                Toast.makeText(this, "Please enter a positive amount", Toast.LENGTH_LONG).show();
                return;
            }
            amount += selecteduser.getSaldo();
            database.child(selecteduser.getUid()).child("saldo").setValue(amount);
            Toast.makeText(this, "Saldo topped up", Toast.LENGTH_LONG).show();
            selecteduser = null;
        } else {
            Toast.makeText(this,"Search again if you want to top up this user again", Toast.LENGTH_LONG).show();
        }
    }

    public static Task<DataSnapshot> getSnapshot (DatabaseReference database) {
        final TaskCompletionSource<DataSnapshot> tcs = new TaskCompletionSource<>();
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tcs.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                tcs.setException(databaseError.toException());
            }
        });
        return tcs.getTask();
    }
}
