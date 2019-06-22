package com.ds.zibzy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    TextInputLayout t1,t2;
    EditText e1,e2;
    Button b1;
    TextView log;
    ProgressDialog p1;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Join Zibzy");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        e1=(findViewById(R.id.emailet));
        e2=(findViewById(R.id.passet));
        b1=(findViewById(R.id.regbut));
        log=(findViewById(R.id.logtxt));

        p1=new ProgressDialog(this);
        p1.setMessage("Creating Your Account...");
        mAuth=FirebaseAuth.getInstance();

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lo= new Intent(Register.this,Login.class);
                startActivity(lo);
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=e1.getText().toString().trim();
                String password=e2.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    e1.setError("Invalid Email");
                    e1.setFocusable(true);
                }
                else if(password.length()<6){
                    e2.setError("Password length must be minimum 6 characters");
                    e2.setFocusable(true);
                }
                else registerUser(email,password);
            }
        });
    }

    private void registerUser(String email, String password) {
        p1.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity
                            p1.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            String email=user.getEmail();
                            String uid=user.getUid();
                            //storing registered details in realtime db also using hashmap
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name","");
                            hashMap.put("phone","");
                            hashMap.put("image","");
                            hashMap.put("cover","");

                            FirebaseDatabase database=FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(Register.this, "Registered..\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            Intent pr = new Intent(Register.this, DashboardActivity.class);
                            startActivity(pr);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            p1.dismiss();
                            Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                p1.dismiss();
                Toast.makeText(Register.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}
