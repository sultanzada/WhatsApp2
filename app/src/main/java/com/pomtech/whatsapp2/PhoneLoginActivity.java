package com.pomtech.whatsapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private static final String TAG = "PhoneLoginActivity";
    private Button sendVerficationCodeBtn;
    private Button verifyBtn;

    private EditText mobileNumberInput;
    private EditText verficationCodeInput;

    private TextView noticeLabel;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        initializeFields();

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        sendVerficationCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mobileNum = mobileNumberInput.getText().toString();
                if (TextUtils.isEmpty(mobileNum)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter your mobile number first.", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Mobile Verification");
                    loadingBar.setMessage("Please wait, we are authenticating your mobile number...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            mobileNum,                                    // Phone number to verify
                            60,                                        // Timeout duration
                            TimeUnit.SECONDS,                           // Unit of timeout
                            PhoneLoginActivity.this,             // Activity (for callback binding)
                            mCallbacks);                              // OnVerificationStateChangedCallbacks
                }

            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileNumberInput.setVisibility(View.INVISIBLE);
                sendVerficationCodeBtn.setVisibility(View.INVISIBLE);

                String verificationCode = verficationCodeInput.getText().toString();

                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter the verification code first.", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait, we are verifying your verification code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number.", Toast.LENGTH_SHORT).show();

                mobileNumberInput.setVisibility(View.VISIBLE);
                sendVerficationCodeBtn.setVisibility(View.VISIBLE);

                verficationCodeInput.setVisibility(View.INVISIBLE);
                verifyBtn.setVisibility(View.INVISIBLE);
                noticeLabel.setVisibility(View.INVISIBLE);


            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "The 6 digits code has been sent to your mobile number", Toast.LENGTH_SHORT).show();

                mobileNumberInput.setVisibility(View.INVISIBLE);
                sendVerficationCodeBtn.setVisibility(View.INVISIBLE);

                verficationCodeInput.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.VISIBLE);
                noticeLabel.setVisibility(View.VISIBLE);


            }
        };
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            usersRef.child(currentUserId).child("device_token").setValue(deviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                loadingBar.dismiss();
                                                Toast.makeText(PhoneLoginActivity.this, "Congratulation, you are Logged in successfully.", Toast.LENGTH_LONG).show();
                                                sendToMainActivity();
                                            }
                                        }
                                    });
                        } else {

                            String message = task.getException().getMessage();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void initializeFields() {
        sendVerficationCodeBtn = findViewById(R.id.send_ver_code_button);
        verifyBtn = findViewById(R.id.verify_button);

        mobileNumberInput = findViewById(R.id.phone_number_input);
        verficationCodeInput = findViewById(R.id.verification_code_input);

        noticeLabel = findViewById(R.id.notice_label);

        loadingBar = new ProgressDialog(this);
    }

    private void sendToMainActivity() {

        Intent goToMainActivity = new Intent(this, MainActivity.class);
        goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMainActivity);
        finish();
    }

    public static boolean isInternetAvailable(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

            if (activeNetworkInfo != null) { // connected to the internet
                // connected to the mobile provider's data plan
                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    return true;
                } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false;
    }
}
