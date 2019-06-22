package com.ds.zibzy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
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

public class Login extends AppCompatActivity {

    EditText e1,e2;
    Button b1;
    TextView reg,forpass;
    ProgressDialog p1;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        e1=(findViewById(R.id.emlo));
        e2=(findViewById(R.id.palo));
        b1=(findViewById(R.id.logbut));
        reg=(findViewById(R.id.regtxt));
        forpass=(findViewById(R.id.forpass));

        p1=new ProgressDialog(this);
        p1.setMessage("Logging in...");
        mAuth=FirebaseAuth.getInstance();

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent re= new Intent(Login.this,Register.class);
                startActivity(re);
            }
        });

        forpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fo= new Intent(Login.this,ForgotPassword.class);
                startActivity(fo);
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email= e1.getText().toString().trim();
                String passw= e2.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    e1.setError("Invalid Email");
                    e1.setFocusable(true);
                }
                else loginUser(email,passw);
            }
        });

        p1= new ProgressDialog(this);
        p1.setMessage("Logging in...");

    }

    private void loginUser(String email, String passw) {
        p1.show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            p1.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(Login.this, DashboardActivity.class));
                            finish();
                        }
                        else {
                            p1.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                p1.dismiss();
                Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
