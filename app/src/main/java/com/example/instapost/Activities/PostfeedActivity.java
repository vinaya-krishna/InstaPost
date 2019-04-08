package com.example.instapost.Activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.instapost.Adapters.PostfeedAdapter;
import com.example.instapost.Models.Post;
import com.example.instapost.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostfeedActivity extends AppCompatActivity {

    private RecyclerView mPostsView;
    private PostfeedAdapter mAdapter;
    private DatabaseReference mDatabaseRef;
    private ArrayList<Post> mPosts;
    private ProgressBar mProgressImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postfeed);

        mProgressImage = findViewById(R.id.progress_bar_image);
        mPostsView = findViewById(R.id.recycler_view_posts);
        mPostsView.setLayoutManager(new LinearLayoutManager(this));
        mPosts = new ArrayList<>();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Posts");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot thisPost: dataSnapshot.getChildren()){
                    Post post = thisPost.getValue(Post.class);
                    mPosts.add(post);
                }

                mAdapter = new PostfeedAdapter(PostfeedActivity.this, mPosts);
                mPostsView.setAdapter(mAdapter);
                mProgressImage.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostfeedActivity.this, databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                mProgressImage.setVisibility(View.INVISIBLE);
            }
        });

    }
}