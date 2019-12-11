package com.pomtech.whatsapp2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SETTINGS";
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImageRef;


    private Button updateSettingsBtn;
    private EditText userNameEdt;
    private EditText statusEdt;
    private CircleImageView userProfileImageView;

    private ProgressDialog loadingBar;
    private ProgressDialog updateProgressBar;

    private Toolbar mToolbar;

    private String username;
    private String status;
    private String currentUserID;
    private String currentUserEmailAddress;

    private static final int GALLERY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentUserEmailAddress = mAuth.getCurrentUser().getEmail();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");

        InitializeFields();

        userNameEdt.setVisibility(View.GONE);

        userProfileImageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {

                ActivityCompat.requestPermissions(
                        SettingsActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        GALLERY_REQUEST_CODE
                );
            }
        });

        updateSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSetting();

            }
        });

        retrieveData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Don'/t have permission to access file location", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Set profile image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();


                StorageReference filePath = userProfileImageRef.child(currentUserID + "\'s-profile-picture.jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            final String downloadUri = task.getResult().getDownloadUrl().toString();

                            rootRef.child("Users").child(currentUserID).child("image").setValue(downloadUri)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SettingsActivity.this, "Image saved in Database, Successfully.", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();

                                            } else {
                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }

                                        }
                                    });

                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void InitializeFields() {

        updateSettingsBtn = findViewById(R.id.update_settings_button);
        userNameEdt = findViewById(R.id.set_profile_username);
        statusEdt = findViewById(R.id.set_profile_status);
        userProfileImageView = findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
        updateProgressBar = new ProgressDialog(this);
        mToolbar=findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");



    }

    private void updateSetting() {

        username = userNameEdt.getText().toString().trim();
        status = statusEdt.getText().toString().trim();

        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Please enter your username and status...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter your username...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Please enter your status...", Toast.LENGTH_SHORT).show();
        } else if (userNameEdt.getText().toString().contains(" ")) {
            Toast.makeText(this, "Invalid Username, there should not be space between letters", Toast.LENGTH_LONG).show();
        } else {

            updateProgressBar.setTitle("Updating the profile");
            updateProgressBar.setMessage("Please wait, the profile is updating...");
            updateProgressBar.setCanceledOnTouchOutside(false);
            updateProgressBar.show();
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", username);
            profileMap.put("status", status);

            rootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateProgressBar.dismiss();
                                Toast.makeText(SettingsActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                                sendToMainActivity();
                                //Toast.makeText(SettingsActivity.this, "Welcome: "+ username, Toast.LENGTH_SHORT).show();

                            } else {
                                String message = task.getException().getMessage();
                                updateProgressBar.dismiss();
                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }

    private void retrieveData() {

        rootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("name")) {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            userNameEdt.setText(retrieveUserName);
                        }
                        if (dataSnapshot.hasChild("status")) {
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                            statusEdt.setText(retrieveUserStatus);
                        }
                        if (dataSnapshot.hasChild("image")) {
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            Picasso.get()
                                    .load(retrieveProfileImage)
                                    .placeholder(R.drawable.profile_image)
                                    .into(userProfileImageView);
                        } else {
                            userNameEdt.setVisibility(View.VISIBLE);
                            userNameEdt.setFocusableInTouchMode(true);
                            userNameEdt.setFocusable(true);
                            userNameEdt.requestFocus();


                            Toast.makeText(SettingsActivity.this, "Please set and Update your profile details", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendToMainActivity() {
        Intent goToMainActivity = new Intent(this, MainActivity.class);
        goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMainActivity);
        finish();
    }
}
