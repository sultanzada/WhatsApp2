package com.pomtech.whatsapp2.Adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pomtech.whatsapp2.ImageViewerActivity;
import com.pomtech.whatsapp2.MainActivity;
import com.pomtech.whatsapp2.Model.Messages;
import com.pomtech.whatsapp2.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.messageViewHolder> {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private List<Messages> userMessagesList;

    public MessageAdapter(List<Messages> userMessagesList) {

        this.userMessagesList = userMessagesList;
    }

    public class messageViewHolder extends RecyclerView.ViewHolder {

        TextView receiverMessageText;
        TextView senderMessageText;
        TextView senderDate;
        TextView receiverDate;
        TextView messageSenderImageDate;
        TextView messageReceiverImageDate;
        CircleImageView receiverProfileImage;
        ImageView messageSenderPicture;
        ImageView messageReceiverPicture;

        public messageViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.receiver_profile_image);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);

            senderDate = itemView.findViewById(R.id.sender_date);
            receiverDate = itemView.findViewById(R.id.receiver_date);

            messageSenderImageDate = itemView.findViewById(R.id.sender_picture_date);
            messageReceiverImageDate = itemView.findViewById(R.id.receiver_picture_date);

        }
    }


    @NonNull
    @Override
    public messageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new messageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final messageViewHolder holder, final int position) {

        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")) {

                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        holder.senderDate.setVisibility(View.GONE);
        holder.receiverDate.setVisibility(View.GONE);
        holder.messageSenderImageDate.setVisibility(View.GONE);
        holder.messageReceiverImageDate.setVisibility(View.GONE);


        if (fromMessageType.equals("text")) {


            if (fromUserID.equals(messageSenderID)) {

                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderDate.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages);
                holder.senderMessageText.setText(messages.getMessage());
                holder.senderDate.setText(messages.getTime());


            } else {


                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverDate.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages);
                holder.receiverMessageText.setText(messages.getMessage());
                holder.receiverDate.setText(messages.getTime());

            }
        } else if (fromMessageType.equals("image")) {

            if (fromUserID.equals(messageSenderID)) {

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.messageSenderImageDate.setVisibility(View.VISIBLE);

                holder.messageSenderImageDate.setText(messages.getTime());
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

            } else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.messageReceiverImageDate.setVisibility(View.VISIBLE);

                holder.messageReceiverImageDate.setText(messages.getTime());

                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);


            }

        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {

            if (fromUserID.equals(messageSenderID)) {

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.messageSenderImageDate.setVisibility(View.VISIBLE);

                holder.messageSenderImageDate.setText(messages.getTime());
                //holder.messageSenderPicture.setBackgroundResource(R.drawable.file2);

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whatsapp2-72fdd.appspot.com/o/Image%20Files%2Ffile3.png?alt=media&token=3fda8f51-0eb5-4a9e-97da-0e44e26e4a46")
                        .into(holder.messageSenderPicture);
            } else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.messageReceiverImageDate.setVisibility(View.VISIBLE);

                holder.messageReceiverImageDate.setText(messages.getTime());
                //  holder.messageReceiverPicture.setBackgroundResource(R.drawable.file2);
                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whatsapp2-72fdd.appspot.com/o/Image%20Files%2Ffile3.png?alt=media&token=3fda8f51-0eb5-4a9e-97da-0e44e26e4a46")
                        .into(holder.messageReceiverPicture);
            }

        }

        if (fromUserID.equals(messageSenderID)) {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {

                        CharSequence options[] = new CharSequence[]{

                                "Download and View this document",
                                "Delete for everyone",
                                "Delete for me",
                                "Cancel "
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);


                                }

                                else if (which == 1) {

                                    deleteMessageForEveryone(position,holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }

                                else if (which == 2) {

                                    deleteSentMessage(position,holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                else if (which == 3) {

                                    dialog.dismiss();
                                }
                            }
                        });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("text")) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete for everyone",
                                "Delete for me",
                                "Cancel "
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                if (which == 0) {

                                    deleteMessageForEveryone(position,holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 1) {

                                    deleteSentMessage(position,holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 2) {

                                    dialog.dismiss();
                                }
                            }
                        });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("image") ){

                        CharSequence options[] = new CharSequence[]{

                                "View this image",
                                "Delete for everyone",
                                "Delete for me",
                                "Cancel "
                        };

                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0){

                                    Intent intent =new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }

                                else if (which == 1){

                                    deleteMessageForEveryone(position,holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }

                                else if (which == 2){

                                    deleteSentMessage(position,holder);

                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                }
                                else if (which == 3){
                                    dialog.dismiss();

                                }
                            }
                        });
                        builder.show();
                    }

                }
            });

        }
        else {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {

                        CharSequence options[] = new CharSequence[]{

                                "Download and View this document",
                                "Delete for me",
                                "Cancel "
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);


                                } else if (which == 1) {

                                    deleteReceivedMessage(position,holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 2) {
                                    dialog.dismiss();

                                }
                            }
                        });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("text")) {

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "Cancel "
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                if (which == 0) {

                                    deleteReceivedMessage(position,holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 1) {

                                    dialog.dismiss();

                                }
                            }
                        });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("image") ){

                        CharSequence options[] = new CharSequence[]{

                                "View this image",
                                "Delete for me",
                                "Cancel "
                        };

                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0){

                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);

                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }

                                else if (which == 1){

                                    deleteReceivedMessage(position,holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                else if (which == 2){

                                    dialog.dismiss();

                                }

                            }
                        });
                        builder.show();
                    }

                }
            });


        }

    }


    @Override
    public int getItemCount() {

        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position, final messageViewHolder holder){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "The message has been deleted succefully", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deleteReceivedMessage(final int position, final messageViewHolder holder){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "The message has been deleted successfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deleteMessageForEveryone(final int position, final messageViewHolder holder){

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "The message has been deleted successfully", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
