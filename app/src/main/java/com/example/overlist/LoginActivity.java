package com.example.overlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private TextView question;
    private EditText emailEd, passwordEd;
    private Button login;

    private ProgressDialog loader;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if(user != null){
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        };

        question = findViewById(R.id.loginPageQuestion);
        emailEd = findViewById(R.id.loginEmail);
        passwordEd = findViewById(R.id.loginPassword);
        login = findViewById(R.id.loginBtn);

        loader = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        question.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        login.setOnClickListener(v -> {
            String email = emailEd.getText().toString();
            String password = passwordEd.getText().toString();

            if(TextUtils.isEmpty(email)){
                emailEd.setError("Email is required");
            }
            if(TextUtils.isEmpty(password)){
                passwordEd.setError("Password is required");
            }else{
                loader.setMessage("Login in progress");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,"Login Successful, logged in as " + Objects.requireNonNull(mAuth.getCurrentUser()).getEmail() ,Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this,"Login Failed " + Objects.requireNonNull(task.getException()).toString(),Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }
}