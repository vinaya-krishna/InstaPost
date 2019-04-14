package com.example.instapost.Adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.instapost.Models.Post;
import com.example.instapost.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostfeedAdapter extends RecyclerView.Adapter<PostfeedAdapter.PostfeedHolder> {
    private Context mContext;
    private ArrayList<Post> mPosts;
    public PostfeedAdapter( Context context, ArrayList<Post> posts ){
        mContext = context;
        mPosts = posts;
    }

    @NonNull
    @Override
    public PostfeedHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, viewGroup, false);
        return new PostfeedHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostfeedHolder postfeedHolder, int i) {
        Post post = mPosts.get(i);
        postfeedHolder.optionalText.setText(post.getmOptionalText());
        postfeedHolder.hashTag.setText(post.getmHashTag());
        postfeedHolder.userName.setText(post.getmName());
        Picasso.with(mContext).load(post.getmImageURl())
                .fit()
                .placeholder(R.drawable.ic_imageload)
                .centerInside()
                .into(postfeedHolder.postImage);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class PostfeedHolder extends RecyclerView.ViewHolder {

        public TextView optionalText, hashTag, userName;
        public ImageView postImage;

        public PostfeedHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.post_name);
            optionalText = itemView.findViewById(R.id.post_text);
            hashTag = itemView.findViewById(R.id.post_hashtag);
            postImage = itemView.findViewById(R.id.post_image);
        }
    }


}
