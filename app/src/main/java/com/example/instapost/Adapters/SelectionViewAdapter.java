package com.example.instapost.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.instapost.Activities.PostfeedActivity;
import com.example.instapost.R;

import java.util.ArrayList;

public class SelectionViewAdapter extends RecyclerView.Adapter<SelectionViewAdapter.ViewHolder>{
    private static final String TAG = "SelectionViewAdapter";

    private ArrayList<String> mTextContents;
    private Context mContext;

    public SelectionViewAdapter(Context mContext, ArrayList<String> mTextContents) {
        this.mTextContents = mTextContents;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_listitem, viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder,final int i) {
        Log.d(TAG, "onBindViewHolder Called");
        viewHolder.textContent.setText(mTextContents.get(i));
        viewHolder.holderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: "+ mTextContents.get(i));
                Intent postFeed = new Intent(v.getContext(), PostfeedActivity.class);
                mContext.startActivity(postFeed);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mTextContents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView textContent;
        RelativeLayout holderLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textContent = itemView.findViewById(R.id.text_content);
            holderLayout = itemView.findViewById(R.id.holder_layout);
        }
    }
}
