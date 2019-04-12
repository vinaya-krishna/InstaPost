package com.example.instapost.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.instapost.Models.Post;
import com.example.instapost.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Button mButtonChooseImg;
    private Button mPost;
    private EditText mOptionalText;
    private EditText mHashTag;
    private ImageView mImageView;
    private ProgressBar mProgressbar;
    private Uri mImageUri;
    private StorageTask mUploadTask;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    SharedPreferences sharedPreferences;
    private String name,nickName;

    private static final String TAG = "PostFragment";



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_newpost, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButtonChooseImg = view.findViewById(R.id.choose_photo_button);
        mPost = view.findViewById(R.id.post_button);
        mOptionalText = view.findViewById(R.id.optional_text);
        mHashTag = view.findViewById(R.id.hash_tag);
        mImageView = view.findViewById(R.id.image_view);
        mProgressbar = view.findViewById(R.id.progress_bar_upload);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPreferences = getContext().getSharedPreferences("Login", getActivity().MODE_PRIVATE);
        name = (sharedPreferences.getString("name", ""));
        nickName = (sharedPreferences.getString("nickName", ""));

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Posts");

        mButtonChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();

            }
        });

        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUploadTask != null && mUploadTask.isInProgress())
                    showMessage("Upload in Progress");
                else{
                    String hashTags = mHashTag.getText().toString().trim();
                    Pattern hashTagPattern = Pattern.compile("(#[0-9a-zA-Z_]+)");
                    Matcher matchTags = hashTagPattern.matcher(hashTags);
                    StringBuilder hashtagsString = new StringBuilder();
                    while(matchTags.find())
                        hashtagsString.append(matchTags.group(0)+" ");

                    if(hashtagsString.length()>0)
                        uploadImage(hashtagsString.toString());
                    else
                        showMessage("HashTag Can't be Empty");
                }

            }
        });
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(final String hashtagsString){
        if(mImageUri != null){
            StorageReference fileRef = mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));
            mUploadTask = fileRef.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressbar.setProgress(0);
                                }
                            },500);

                            showMessage("Upload Success");

                            Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Post post = new Post(mOptionalText.getText().toString().trim(),
                                            uri.toString(),
                                            hashtagsString,
                                            user.getEmail(),nickName);
                                    String uploadId = mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(uploadId).setValue(post);

                                    mOptionalText.getText().clear();
                                    mHashTag.getText().clear();
                                    mImageView.setImageDrawable(null);

                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showMessage("Upload Failed");
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred())/ taskSnapshot.getTotalByteCount();
                            mProgressbar.setProgress((int)progress);
                        }
                    });
        }
        else{
            showMessage("No file selected");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null){
            mImageUri = data.getData();



            //https://developer.android.com/topic/performance/graphics/load-bitmap.html#read-bitmap
            Picasso.with(getActivity()).load(mImageUri).into(mImageView);
            Log.d(TAG, "onActivityResult: "+mImageUri);
//            mImageView.setImageURI(mImageUri);
        }
    }

    private void showMessage(String message){
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }
}
