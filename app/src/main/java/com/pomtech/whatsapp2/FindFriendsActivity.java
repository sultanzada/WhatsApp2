package com.pomtech.whatsapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pomtech.whatsapp2.Model.Contacts;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar findFriendToolbar;
    private RecyclerView findFriendRecyclerList;

    private DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        findFriendRecyclerList = findViewById(R.id.find_friends_recycler_view);
        findFriendRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        findFriendToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(findFriendToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter <Contacts, FindFriendViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {

                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.userProfileImageView);


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visited_user_id=getRef(position).getKey();

                        Intent goToProfileActivity = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                        goToProfileActivity.putExtra("visited_user_id",visited_user_id);
                        startActivity(goToProfileActivity);


                    }
                });


            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_list, parent, false);
                FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                return viewHolder;

            }
        };

        findFriendRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userStatus;
        ImageView userProfileImageView;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userProfileImageView = itemView.findViewById(R.id.user_profile_image);
        }
    }
}
