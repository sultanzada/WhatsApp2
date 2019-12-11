package com.pomtech.whatsapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseReference userRef;
    private DatabaseReference chatRequestRef;
    private DatabaseReference contactsRef;
    private DatabaseReference notificationRef;
    private FirebaseAuth mAuth;

    private String receiverUserID;
    private String senderUserID;
    private String current_User_status;


    private ImageView backgroundImage;
    private CircleImageView visitedProfileImage;

    private TextView visitedProfileUsername;
    private TextView visitedProfileStatus;

    private Button visitedProfileSendMessage;
    private Button declineRequestMessageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");


        receiverUserID = getIntent().getExtras().get("visited_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();


        backgroundImage = findViewById(R.id.profile_activity_background);
        visitedProfileImage = findViewById(R.id.visited_profile_image);

        visitedProfileUsername = findViewById(R.id.visited_user_name);
        visitedProfileStatus = findViewById(R.id.visited_profile_status);

        visitedProfileSendMessage = findViewById(R.id.visited_send_message_button);
        declineRequestMessageButton = findViewById(R.id.decline_message_request_button);

        current_User_status = "new";


        retrieveUserData();
    }

    private void retrieveUserData() {

        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if ((dataSnapshot.exists()) && dataSnapshot.hasChild("image")) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String username = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.no_image).into(visitedProfileImage);
                    visitedProfileUsername.setText(username);
                    visitedProfileStatus.setText(userStatus);

                    manageChatRequest();

                } else {

                    String username = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    visitedProfileUsername.setText(username);
                    visitedProfileStatus.setText(userStatus);

                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {
        chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)) {
                    String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if (request_type.equals("sent")) {

                        current_User_status = "request_sent";
                        visitedProfileSendMessage.setText("cancel chat request");

                    } else if (request_type.equals("received")) {

                        current_User_status = "request_received";
                        visitedProfileSendMessage.setText("accept chat request");

                        declineRequestMessageButton.setVisibility(View.VISIBLE);

                        declineRequestMessageButton.setEnabled(true);

                        declineRequestMessageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });


                    }
                } else {

                    contactsRef.child(senderUserID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(receiverUserID)) {
                                        current_User_status = "friends";
                                        visitedProfileSendMessage.setText("remove this contact");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (!senderUserID.equals(receiverUserID)) {

            visitedProfileSendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    visitedProfileSendMessage.setEnabled(false);

                    if (current_User_status.equals("new")) {
                        sendChatRequest();
                    }
                    if (current_User_status.equals("request_sent")) {
                        cancelChatRequest();
                    }
                    if (current_User_status.equals("request_received")) {
                        acceptRequestChat();
                    }
                    if (current_User_status.equals("friends")) {
                        removeSpecificContact();
                    }

                }
            });
        } else {
            visitedProfileSendMessage.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact() {

        contactsRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                visitedProfileSendMessage.setEnabled(true);
                                                current_User_status = "new";
                                                visitedProfileSendMessage.setText("send message");
                                                declineRequestMessageButton.setVisibility(View.INVISIBLE);
                                                declineRequestMessageButton.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });

    }

    private void acceptRequestChat() {

        contactsRef.child(senderUserID).child(receiverUserID).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserID).child(senderUserID).child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                chatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    visitedProfileSendMessage.setEnabled(true);
                                                                    current_User_status = "friends";
                                                                    visitedProfileSendMessage.setText("remove this contact");


                                                                    declineRequestMessageButton.setVisibility(View.INVISIBLE);
                                                                    declineRequestMessageButton.setEnabled(false);

                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }

                                        }
                                    });
                        }

                    }
                });


    }

    private void cancelChatRequest() {

        chatRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                visitedProfileSendMessage.setEnabled(true);
                                                current_User_status = "new";
                                                visitedProfileSendMessage.setText("send message");
                                                declineRequestMessageButton.setVisibility(View.INVISIBLE);
                                                declineRequestMessageButton.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });

    }

    private void sendChatRequest() {

        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");

                                                notificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    visitedProfileSendMessage.setEnabled(true);
                                                                    current_User_status = "request_sent";
                                                                    visitedProfileSendMessage.setText("cancel chat request");
                                                                }

                                                            }
                                                        });


                                            }
                                        }
                                    });
                        }
                    }
                });

    }
}
