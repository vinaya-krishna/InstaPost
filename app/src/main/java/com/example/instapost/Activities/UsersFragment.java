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
import com.example.instapost.Models.User;
import com.example.instapost.R;
import com.google.firebase.auth.FirebaseAuth;
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

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = mDatabase.getReference();
    private DatabaseReference mUsersRef = mRootRef.child("Users");
    private SelectionViewAdapter selectionViewAdapter;
    private SharedPreferences.Editor sharedPreferences;


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

        mAuth = FirebaseAuth.getInstance();
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

                        if(child.getKey().compareTo(FirebaseAuth.getInstance().getCurrentUser().getUid()) == 0){
                            saveUser();
                        }
                    }

                    selectionViewAdapter.notifyDataSetChanged();
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

    private void saveUser(){
        sharedPreferences = getContext().getSharedPreferences("Login", getActivity().MODE_PRIVATE).edit();
        sharedPreferences.putString("name", currentUser.getName());
        sharedPreferences.putString("nickName", currentUser.getNickName());
        sharedPreferences.putString("email", currentUser.getEmail());
        sharedPreferences.commit();
    }

    private void showMessage(String message){
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }
}
