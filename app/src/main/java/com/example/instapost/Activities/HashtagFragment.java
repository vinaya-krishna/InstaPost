package com.example.instapost.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.instapost.Adapters.SelectionViewAdapter;
import com.example.instapost.Models.Post;
import com.example.instapost.Models.User;
import com.example.instapost.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HashtagFragment extends Fragment {

    private ProgressBar mProgressBar;
    private RecyclerView recyclerView;

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = mDatabase.getReference();
    private DatabaseReference mPostsRef = mRootRef.child("Posts");
    private SelectionViewAdapter selectionViewAdapter;

    private static final String TAG = "HashtagFragment";
    private Set<String> mHashTags = new HashSet<>();
    private ArrayList<String> mListContents = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hashtag, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = view.findViewById(R.id.progress_bar_list);
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initRecyclerView();

        mPostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                mListContents.clear();
                mHashTags.clear();

                if(key == "Posts"){
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Post post = child.getValue(Post.class);
                        String[] allHashTags = post.getmHashTag().split("\\s+");
                        for(String hashTag: allHashTags)
                            mHashTags.add(hashTag);
                    }
                    mListContents.addAll(mHashTags);
                    selectionViewAdapter.notifyDataSetChanged();
                    if(mListContents.isEmpty())
                        showMessage("No HashTags Found!");
                }

                mProgressBar.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showMessage(databaseError.getMessage());
            }
        });

    }

    private void initRecyclerView(){

        selectionViewAdapter = new SelectionViewAdapter(getActivity(), mListContents);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider,null));
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(selectionViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    private void showMessage(String message){
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }
}
