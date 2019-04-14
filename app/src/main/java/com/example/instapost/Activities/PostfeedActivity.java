package com.example.instapost.Activities;

import android.content.Intent;
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
    private boolean isName = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postfeed);

        Intent intent = getIntent();
        final String selectedItem = intent.getExtras().getString("ItemSelected");
        if(selectedItem.startsWith("#"))
            isName = false;

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
                    if(isName){
                        if(post.getmName().equals(selectedItem))
                            mPosts.add(post);
                    }
                    else{
                        if(post.getmHashTag().contains(selectedItem))
                            mPosts.add(post);
                    }


                }
                mAdapter.notifyDataSetChanged();
                mProgressImage.setVisibility(View.INVISIBLE);
                if(mPosts.isEmpty())
                    showMessage("No Posts Found!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostfeedActivity.this, databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                mProgressImage.setVisibility(View.INVISIBLE);
            }
        });

        initRecyclerView();

    }

    private void initRecyclerView(){
        mAdapter = new PostfeedAdapter(PostfeedActivity.this, mPosts);
        mPostsView.setAdapter(mAdapter);
    }

    private void showMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
