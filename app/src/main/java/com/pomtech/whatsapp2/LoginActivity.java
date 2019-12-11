package com.pomtech.whatsapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private Button mLoginBtn, mMobileLogingBtn;
    private EditText mEmailEdt, mPasswordEdt;
    private ProgressDialog loadingBar;

    private TextView mForgotPasswordTxt, mNeedNewAccountTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        initializeFields();

        mMobileLogingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToPhoneLoginActivity();
            }
        });

        Intent intent = getIntent();
        String username1 = intent.getStringExtra("username");
        mEmailEdt.setText(username1);

        String password1 = intent.getStringExtra("password");
        mPasswordEdt.setText(password1);

        mNeedNewAccountTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegisterActivity();
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUserToLogin();
            }
        });

    }


    private void allowUserToLogin() {

        final String email = mEmailEdt.getText().toString().trim();
        String password = mPasswordEdt.getText().toString().trim();

        if (email.isEmpty() && password.isEmpty()) {
            Toast.makeText(this, "Please enter your email and password...", Toast.LENGTH_SHORT).show();
        } else if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email...", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password...", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Sign in");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String currentUserID = mAuth.getCurrentUser().getUid();
                                String deciveToken = FirebaseInstanceId.getInstance().getToken();

                                usersRef.child(currentUserID).child("device_token").setValue(deciveToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    sendToMainActivity();
                                                    Toast.makeText(LoginActivity.this, "Welcome back: " + email, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }

                                            }
                                        });


                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendToRegisterActivity() {
        Intent goToRegisterActivity = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(goToRegisterActivity);

    }

    private void initializeFields() {
        mLoginBtn = findViewById(R.id.email_login_button);
        mMobileLogingBtn = findViewById(R.id.phone_login_Button);


        mEmailEdt = findViewById(R.id.login_email);
        mPasswordEdt = findViewById(R.id.login_password);

        mForgotPasswordTxt = findViewById(R.id.forget_password_link);
        mNeedNewAccountTxt = findViewById(R.id.need_new_account);
        loadingBar = new ProgressDialog(this);
    }

    private void sendToMainActivity() {

        Intent goToMainActivity = new Intent(this, MainActivity.class);
        goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMainActivity);
        finish();
    }

    private void sendToPhoneLoginActivity() {

        Intent goToPhoneLoginActivity = new Intent(this, PhoneLoginActivity.class);
        startActivity(goToPhoneLoginActivity);
    }
}
