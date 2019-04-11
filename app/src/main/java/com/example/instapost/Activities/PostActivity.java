package com.example.instapost.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
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

public class PostActivity extends AppCompatActivity {

    private static final String TAG = "PostActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button mButtonChooseImg;
    private Button mPost;
    private EditText mOptionalText;
    private EditText mHashTag;
    private ImageView mImageView;
    private ProgressBar mProgressbar;
    private Uri mImageUri;
    private StorageTask mUploadTask;
    private Spannable tagSpannable;

    private boolean isHashTag = false;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    SharedPreferences sharedPreferences;
    private String name,nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mButtonChooseImg = findViewById(R.id.choose_photo_button);
        mPost = findViewById(R.id.post_button);
        mOptionalText = findViewById(R.id.optional_text);
        mHashTag = findViewById(R.id.hash_tag);
        mImageView = findViewById(R.id.image_view);
        mProgressbar = findViewById(R.id.progress_bar_upload);

        tagSpannable = mHashTag.getText();

        sharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
        name = (sharedPreferences.getString("name", ""));
        nickName = (sharedPreferences.getString("nickName", ""));

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Posts");


//        mHashTag.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String startCharacter = null;
//                try{
//                    startCharacter = Character.toString(s.charAt(start));
//                    Log.i(getClass().getSimpleName(), "CHARACTER OF NEW WORD: "+ start +" "+before+" "+count +" "+ startCharacter);
//                }
//                catch(Exception ex){
//                    startCharacter = " ";
//                    isHashTag = true;
//                }
//
//
//                if (startCharacter.equals("#") || isHashTag) {
//                    if(start < count){
//                        hashtagCheck(s.toString().substring(start), start, start + count);
//                        isHashTag = true;
//                    }
//
//                }
//                if(startCharacter.equals(" ")){
//                    isHashTag = false;
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });


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
        ContentResolver contentResolver = getContentResolver();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null){
            mImageUri = data.getData();



            //https://developer.android.com/topic/performance/graphics/load-bitmap.html#read-bitmap
            Picasso.with(this).load(mImageUri).into(mImageView);
            Log.d(TAG, "onActivityResult: "+mImageUri);
//            mImageView.setImageURI(mImageUri);
        }
    }


    private void hashtagCheck(String s, int start, int end) {
        System.out.println(start +" "+end);
        tagSpannable.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.highlight, null)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
}
