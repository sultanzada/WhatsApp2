package com.pomtech.whatsapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class RegisterActivity extends AppCompatActivity {

    private Button mCreateAccountBtn;
    private EditText mEmailEdt, mPasswordEdt;
    private TextView mAlreadyHaveAccountTxt;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();

        initializeFields();


        mAlreadyHaveAccountTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendToLoginActivity();
            }
        });


        mCreateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createNewAccount();
            }
        });
    }

    private void createNewAccount() {

        String email = mEmailEdt.getText().toString().trim();
        String password = mPasswordEdt.getText().toString().trim();

        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please enter your email and password...", Toast.LENGTH_SHORT).show();
        }
       else if (TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please enter your password...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){

            Toast.makeText(this, "Please enter your email...", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Creating a new Account");
            loadingBar.setMessage("Please Waite...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        String currentUserID=mAuth.getCurrentUser().getUid();
                        rootRef.child("Users").child(currentUserID).setValue("");

                        rootRef.child("Users").child(currentUserID).child("device_token").setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            sendToMainActivity();
                                            Toast.makeText(RegisterActivity.this, "account is created.", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });


                    } else {

                        String message = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }

    }

    private void sendToLoginActivity() {

        Intent goToLoginActivity = new Intent(RegisterActivity.this, LoginActivity.class);
        goToLoginActivity.putExtra("username", mEmailEdt.getText().toString());
        goToLoginActivity.putExtra("password",mPasswordEdt.getText().toString());
        goToLoginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        goToLoginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToLoginActivity);
        finish();
    }
    private void sendToMainActivity() {

        Intent goToMainActivity = new Intent(RegisterActivity.this, MainActivity.class);
        goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMainActivity);
        finish();
    }

    private void initializeFields() {

        mCreateAccountBtn = findViewById(R.id.email_register_button);
        mEmailEdt = findViewById(R.id.register_email);
        mPasswordEdt = findViewById(R.id.register_password);
        mAlreadyHaveAccountTxt = findViewById(R.id.already_have_account);
        loadingBar = new ProgressDialog(this);
    }

}
