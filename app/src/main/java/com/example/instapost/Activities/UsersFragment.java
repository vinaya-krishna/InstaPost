package com.example.instapost.Activities;


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
import com.example.instapost.Models.User;
import com.example.instapost.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersFragment extends Fragment {


    private ProgressBar mProgressBar;
    private RecyclerView recyclerView;
    private User currentUser;

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = mDatabase.getReference();
    private DatabaseReference mUsersRef = mRootRef.child("Users");
    private SelectionViewAdapter selectionViewAdapter;

    private ArrayList<String> mUsers = new ArrayList<>();
    private static final String TAG = "UsersFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, null);
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

        mUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Log.d(TAG, "onDataChange Called");
                if(key == "Users"){
                    mUsers.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        currentUser = child.getValue(User.class);
                        mUsers.add(currentUser.getNickName());
                    }

                    selectionViewAdapter.notifyDataSetChanged();
                    if(mUsers.isEmpty())
                        showMessage("No Users Found!");
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
        Log.d(TAG, "initRecyclerView: init called");

        selectionViewAdapter = new SelectionViewAdapter(getActivity(), mUsers);

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
