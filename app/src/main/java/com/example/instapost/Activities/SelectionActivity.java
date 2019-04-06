package com.example.instapost.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.instapost.Adapters.RecyclerViewAdapter;
import com.example.instapost.R;

import java.util.ArrayList;

public class SelectionActivity extends AppCompatActivity {

    private static final String TAG = "SelectionActivity";
    RadioGroup viewSelection;
    Button createNew;
    RadioButton userRadiobtn;


    private ArrayList<String> mListContents = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

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
                        break;

                    case R.id.radio_hash:
                        Log.d(TAG, "onCheckedChanged: Hash");
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

        initArrayList();
        initRecyclerView();
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init called");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(this, mListContents);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initArrayList(){
        mListContents.add("Vinay");
        mListContents.add("krishna");
        mListContents.add("Pudyodu");
        mListContents.add("Pavan");
        mListContents.add("Abhishek");
        mListContents.add("Raghavan");
        mListContents.add("Yadav");
        mListContents.add("Chechu");
        mListContents.add("Raskit");
        mListContents.add("Albert");


    }




}
