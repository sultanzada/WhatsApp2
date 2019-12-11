package com.pomtech.whatsapp2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.pomtech.whatsapp2.Adapters.MessageAdapter;
import com.pomtech.whatsapp2.Model.Messages;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

//    private final static int IMAGE_REQUEST_CODE = 1;
//    private final static  int PDF_REQUEST_CODE = 2;
//    private final static  int MS_WORD_REQUEST_CODE = 3;


    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private String receiverID;
    private String receiverName;
    private String receiverImage;
    private String senderID;

    private String saveCurrentTime;
    private String saveCurrentDate;

    private String checkType = "";
    private String myUrl = "";


    private Uri fileUri;

    private StorageTask uploadTask;


    private ImageButton backImageBtn;
    private ImageButton sendMessageBtn;
    private ImageButton sendFilesBtn;
    private RecyclerView userMessagesList;

    private EditText messageInputText;

    private Toolbar customToolbar;

    private TextView customToolbarUserName;
    private TextView customToolbarUserStatus;

    private CircleImageView customToolbarUserProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        senderID = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();

        receiverID = getIntent().getExtras().get("visit_user_id").toString();
        receiverName = getIntent().getExtras().get("visit_user_name").toString();
        receiverImage = getIntent().getExtras().get("visit_user_image").toString();



        initializeFields();


        customToolbarUserName.setText(receiverName);
        Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(customToolbarUserProfileImage);

        backImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToMainActivity();
            }
        });

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        displayLastSeen();

        sendFilesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{

                        "Images",
                        "PDF",
                        "MS Word"

                };

                AlertDialog.Builder builder =new AlertDialog.Builder(ChatActivity.this);

                builder.setTitle("Choose any type");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0){

                            checkType = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select any image"),438);

                        }
                        if (which == 1){
                            checkType = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select any pdf file"),438);
                        }
                        if (which == 2){
                            checkType = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select any word file"),438);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void initializeFields() {
        customToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(customToolbar);

        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowCustomEnabled(true);
//
//        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
//        actionBar.setCustomView(actionBarView);

        customToolbarUserName = findViewById(R.id.custom_profile_name);
        customToolbarUserStatus = findViewById(R.id.custom_user_last_seen);
        customToolbarUserProfileImage = findViewById(R.id.custom_profile_image);

        backImageBtn = findViewById(R.id.backBtn);
        sendMessageBtn = findViewById(R.id.send_message_button);
        sendFilesBtn = findViewById(R.id.send_file_button);

        loadingBar =new ProgressDialog(this);

        messageInputText = findViewById(R.id.input_message);

        userMessagesList = findViewById(R.id.private_messages_list_of_users);

        messageAdapter = new MessageAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);

        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);



        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            loadingBar.setTitle("Sending the file");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checkType.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + senderID + "/" + receiverID;
                final String messageReceiverRef = "Messages/" + receiverID + "/" + senderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(senderID).child(receiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + checkType);
                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", task.getResult().getDownloadUrl().toString());
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checkType);
                            messageTextBody.put("from", senderID);
                            messageTextBody.put("to", receiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                            rootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        double p = (int) (100 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage( p + "% uploading...");
                    }
                });

            }
            else if (checkType.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + senderID + "/" + receiverID;
                final String messageReceiverRef = "Messages/" + receiverID + "/" + senderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(senderID).child(receiverID).push();

                 final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + ".jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()){

                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){

                            Uri downloadUri = task.getResult();
                            myUrl =downloadUri.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checkType);
                            messageTextBody.put("from", senderID);
                            messageTextBody.put("to", receiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {

                                        loadingBar.dismiss();
                                         Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadingBar.dismiss();
                                        String error = task.getException().toString();
                                        Toast.makeText(ChatActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");

                                }
                            });

                        }
                    }
                });

            }
            else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing has chosen, Error.", Toast.LENGTH_SHORT).show();
            }



        }
    }

    private void displayLastSeen() {
        rootRef.child("Users").child(receiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    customToolbarUserStatus.setText("online");
                                }

                                else if (state.equals("offline")) {

                                    customToolbarUserStatus.setText("Last Seen: " + time + "  " + date);
                                }

                            } else {
                                customToolbarUserStatus.setText("offline");
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }



    @Override
    protected void onStart() {
        super.onStart();
      messagesList.clear();


        rootRef.child("Messages").child(senderID).child(receiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages messages = dataSnapshot.getValue(Messages.class);
                //  messagesList.clear();
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message = messageInputText.getText().toString();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "message box is empty!", Toast.LENGTH_SHORT).show();
        } else {

            String messageSenderRef = "Messages/" + senderID + "/" + receiverID;
            String messageReceiverRef = "Messages/" + receiverID + "/" + senderID;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                    .child(senderID).child(receiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", senderID);
            messageTextBody.put("to", receiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = task.getException().toString();
                        Toast.makeText(ChatActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");

                }
            });
        }


    }

    private void sendToMainActivity() {

        Intent goToMainActivity = new Intent(this, MainActivity.class);
        startActivity(goToMainActivity);
        finish();
    }

}
