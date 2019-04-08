package com.example.instapost.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.instapost.Adapters.SelectionViewAdapter;
import com.example.instapost.Models.Post;
import com.example.instapost.Models.User;
import com.example.instapost.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SelectionActivity extends AppCompatActivity implements ValueEventListener {

    private static final String TAG = "SelectionActivity";
    RadioGroup viewSelection;
    Button createNew;
    RadioButton userRadiobtn;
    ProgressBar mProgresshBar;
    RecyclerView recyclerView;
    private ArrayList<String> mListContents = new ArrayList<>();
    private ArrayList<String> mUsers = new ArrayList<>();
    private ArrayList<String> mHashTags = new ArrayList<>();
    private ArrayList<Post> mPosts = new ArrayList<>();



    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = mDatabase.getReference();
    private DatabaseReference mUsersRef = mRootRef.child("Users");
    private DatabaseReference mPostsRef = mRootRef.child("Posts");
    private SelectionViewAdapter selectionViewAdapter;




    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        switch( viewSelection.getCheckedRadioButtonId()){
            case R.id.radio_user:
                Log.d(TAG, "onCheckedChanged: User");
                mListContents.clear();
                mListContents.addAll(mUsers);
                selectionViewAdapter.notifyDataSetChanged();
                break;

            case R.id.radio_hash:
                Log.d(TAG, "onCheckedChanged: Hash");
                mListContents.clear();
                mListContents.addAll(mHashTags);
                selectionViewAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selection);
        mProgresshBar = findViewById(R.id.progress_bar_list);
        userRadiobtn = findViewById(R.id.radio_user);
        userRadiobtn.setChecked(true);
        createNew = findViewById(R.id.create_new);
        viewSelection = findViewById(R.id.radio_viewtype);
        viewSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_user:
                        Log.d(TAG, "onCheckedChanged: User");
                        mListContents.clear();
                        mListContents.addAll(mUsers);
                        selectionViewAdapter.notifyDataSetChanged();
                        break;

                    case R.id.radio_hash:
                        Log.d(TAG, "onCheckedChanged: Hash");
                        mListContents.clear();
                        mListContents.addAll(mHashTags);
                        selectionViewAdapter.notifyDataSetChanged();
                        break;
                }
            }
        });

        createNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Create new");
                Intent postIntent = new Intent(v.getContext(), PostActivity.class);
                startActivity(postIntent);
            }
        });



        mUsersRef.addValueEventListener(this);
        mPostsRef.addValueEventListener(this);

        initArrayList();
        initRecyclerView();
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init called");
        recyclerView = findViewById(R.id.recycler_view);
        selectionViewAdapter = new SelectionViewAdapter(this, mListContents);
        recyclerView.setAdapter(selectionViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initArrayList(){
//        mListContents.add("Vinay");
//        mListContents.add("krishna");
//        mListContents.add("Pudyodu");
//        mListContents.add("Pavan");
//        mListContents.add("Abhishek");
//        mListContents.add("Raghavan");
//        mListContents.add("Yadav");
//        mListContents.add("Chechu");
//        mListContents.add("Raskit");
//        mListContents.add("Albert");
    }


    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        String key = dataSnapshot.getKey();
        Log.d(TAG, "onDataChange Called");
        if(key == "Users"){
            mUsers.clear();
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                User user = child.getValue(User.class);
                mUsers.add(user.name);
            }
            mListContents.addAll(mUsers);
            selectionViewAdapter.notifyDataSetChanged();
        }
        else if(key == "Posts"){
            mHashTags.clear();
            mPosts.clear();
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                Post post = child.getValue(Post.class);
                mPosts.add(post);
                mHashTags.add(post.getmHashTag());
            }
        }

        mProgresshBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
