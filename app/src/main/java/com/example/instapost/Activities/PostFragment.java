package com.example.instapost.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.instapost.BuildConfig;
import com.example.instapost.Models.Post;
import com.example.instapost.R;
import com.example.instapost.Util.FileCompressor;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST= 2;
    private Button mButtonCamera;
    private Button mPost;
    private EditText mOptionalText;
    private EditText mHashTag;
    private ImageView mImageView;
    private ProgressBar mProgressbar, mProgressbarUp;
    private StorageTask mUploadTask;
    private FragmentActivity parent;
    private Context context;
    private File mPhotoFile;
    private FileCompressor mCompressor;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    SharedPreferences sharedPreferences;
    private String nickName;

    private static final String TAG = "PostFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_newpost, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButtonCamera = view.findViewById(R.id.camera_button);
        mPost = view.findViewById(R.id.post_button);
        mOptionalText = view.findViewById(R.id.optional_text);
        mHashTag = view.findViewById(R.id.hash_tag);
        mImageView = view.findViewById(R.id.image_view);
        mProgressbar = view.findViewById(R.id.progress_bar_upload);
        mProgressbarUp = view.findViewById(R.id.progress_bar_up_load);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        parent = getActivity();
        context = getContext();
        mCompressor = new FileCompressor(parent);

        sharedPreferences = context.getSharedPreferences("Login", parent.MODE_PRIVATE);
        nickName = (sharedPreferences.getString("nickName", ""));

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Posts");

        selectImage();

        mButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                openCamera();
                selectImage();

            }
        });


        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHashTag.onEditorAction(EditorInfo.IME_ACTION_DONE);
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
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhotoIntent, PICK_IMAGE_REQUEST);

    }

    private void openCamera(){
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (captureIntent.resolveActivity(parent.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);

                mPhotoFile = photoFile;
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(captureIntent, CAPTURE_IMAGE_REQUEST);

            }
        }
    }

    private void uploadImage(final String hashtagsString){

        if(mPhotoFile != null){
            mProgressbarUp.setVisibility(View.VISIBLE);
            mPost.setVisibility(View.INVISIBLE);
            StorageReference fileRef = mStorageRef.child(mPhotoFile.getName());
            mUploadTask = fileRef.putFile(Uri.fromFile(mPhotoFile))
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
                            mProgressbarUp.setVisibility(View.INVISIBLE);
                            mPost.setVisibility(View.VISIBLE);

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
                            mProgressbarUp.setVisibility(View.INVISIBLE);
                            mPost.setVisibility(View.VISIBLE);
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
        if (resultCode == parent.RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_REQUEST) {
                try {
                    mPhotoFile = mCompressor.compressToFile(mPhotoFile);
                    System.out.println("**************************");
                    System.out.println(mPhotoFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Picasso.with(parent).load(mPhotoFile).into(mImageView);

            } else if (requestCode == PICK_IMAGE_REQUEST) {
                Uri selectedImage = data.getData();
                try {
                    mPhotoFile = mCompressor.compressToFile(new File(getRealPathFromUri(selectedImage)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Picasso.with(parent).load(mPhotoFile).into(mImageView);
            }
        }
    }

    private void showMessage(String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Take Photo")) {
                requestStoragePermission(true);
            } else if (items[which].equals("Choose from Library")) {
                requestStoragePermission(false);
            } else if (items[which].equals("Cancel")) {
                dialog.dismiss();
            }
            }
        });

        builder.show();
    }


    private void requestStoragePermission(final boolean isCamera) {
        Dexter.withActivity(parent).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            if (isCamera) {
                                openCamera();
                            } else {
                                openFileChooser();
                            }
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                showMessage("Error occured");
            }
        })
                .onSameThread()
                .check();
    }



    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");

        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", parent.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }



    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = parent.getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }



    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = parent.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }

}
