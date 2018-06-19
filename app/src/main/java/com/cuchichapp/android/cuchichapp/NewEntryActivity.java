package com.cuchichapp.android.cuchichapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cuchichapp.android.cuchichapp.model.Post;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewEntryActivity extends AppCompatActivity {

    private StorageReference mStorageRef;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private Uri selectImageUri;
    private Uri FinalUri;
    ImageView imageView;
    Button UploadButton;
    Button SaveButton;
    EditText postContent;
    EditText postTitle;
    TextView Title;
    TextView Content;
    Button TakePhoto;
    ProgressBar progressBar;
    TextView postActyvityHeader;
    private String uName;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FusedLocationProviderClient mFusedLocationClient;

    private boolean uLocationStatus;
    private boolean checkCameraPermissionStatus;
    private boolean checkWritePermissionStatus;
    private String uLocation;

    private static final String TAG = "New entry activity";

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 98;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 97;

    private static final int RC_PHOTO_PICKER = 2;
    private static final int RC_CAMERA = 5;
    String mCurrentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        imageView = (ImageView) findViewById(R.id.TestImageP);
        UploadButton = (Button) findViewById(R.id.PhotoPickerButton);
        postTitle = (EditText) findViewById(R.id.TitleET);
        postContent = (EditText) findViewById(R.id.ContentET);
        SaveButton = (Button) findViewById(R.id.SavePostButton);
        Title = (TextView) findViewById(R.id.TitleTV);
        Content = (TextView) findViewById(R.id.ContentTV);
        TakePhoto = (Button) findViewById(R.id.CameraButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        postActyvityHeader = (TextView) findViewById(R.id.postActivityHeader);


        mAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        mStorageRef = firebaseStorage.getReference().child("Posts");
        myRef = database.getReference("Posts");

        checkLocationPermission();
        checkCameraPermission();
        checkFilesPermission();

        if (!uLocationStatus){
            uLocation = null;
        }else {
            SetMyLocation();
        }

        takePhotoButton();

        findViewById(R.id.PhotoPickerButton).setOnClickListener((v->{
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action using")
                    , RC_PHOTO_PICKER);
        }));

        TakePhoto.setOnClickListener((v->{

            dispatchTakePictureIntent();

        }));

        findViewById(R.id.SavePostButton).setOnClickListener((v->{

            if (uName==null
                    ||postContent.getText().toString().isEmpty()
                    ||postTitle.getText().toString().isEmpty()){
                Toast.makeText(this,"Please Fill up the form",Toast.LENGTH_LONG)
                        .show();

            }else if (selectImageUri==null){
                Toast.makeText(this,"Please upload an image",Toast.LENGTH_LONG)
                        .show();

            }else {

                StorageReference photoRef =
                        mStorageRef.child(mAuth.getCurrentUser().getUid()
                                +postTitle.getText().toString()
                                +selectImageUri.getLastPathSegment());

                UploadTask task;

                task = photoRef.putFile(selectImageUri);
                task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error!",
                                Toast.LENGTH_SHORT).show();
                        OnFail();
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        Uploading();

                    }
                });

                Task<Uri> uriTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot
                        , Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task)
                            throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return photoRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            FinalUri = task.getResult();

                            String Title = postTitle.getText().toString();
                            String Content = postContent.getText().toString();
                            String uID = mAuth.getCurrentUser().getUid().toString();
                            String IMGPath = FinalUri.toString();

                            myRef.push().setValue(new Post(IMGPath
                                        , uLocation
                                        , Content
                                        , Title
                                        , uName
                                        , uID));
                            finish();
                        }
                    }
                });

            }
        }));

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser()!=null){
                    uName = mAuth.getCurrentUser().getDisplayName();
                    getSupportActionBar().setTitle(R.string.postCreateNew);
                    getSupportActionBar().setSubtitle(uName);

                }else{
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }
            }
        };
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==RC_PHOTO_PICKER && resultCode ==RESULT_OK){

            selectImageUri = data.getData();
            ShowImage(selectImageUri);

        } else if (requestCode == RC_CAMERA && resultCode == RESULT_OK) {

            ShowImage(selectImageUri);

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void ShowImage(Uri uri){
        if (uri!=null){
            imageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(uri)
            .into(imageView);
            UploadButton.setText(R.string.postChangePictureButton);
        }
    }
    private void Uploading(){
        SaveButton.setVisibility(View.GONE);
        UploadButton.setVisibility(View.GONE);
        postTitle.setVisibility(View.GONE);
        postContent.setVisibility(View.GONE);
        TakePhoto.setVisibility(View.GONE);
        Title.setVisibility(View.GONE);
        Content.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        postActyvityHeader.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

    }
    private void OnFail(){
        SaveButton.setVisibility(View.VISIBLE);
        UploadButton.setVisibility(View.VISIBLE);
        postTitle.setVisibility(View.VISIBLE);
        postContent.setVisibility(View.VISIBLE);
        TakePhoto.setVisibility(View.VISIBLE);
        Title.setVisibility(View.VISIBLE);
        Content.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        postActyvityHeader.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void takePhotoButton(){
        if (checkCameraPermissionStatus&&checkWritePermissionStatus){
            TakePhoto.setVisibility(View.VISIBLE);
        }else {
            TakePhoto.setVisibility(View.GONE);
        }
    }

    @SuppressLint("MissingPermission")
    public void SetMyLocation(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            uLocation = location.toString();
                        } else {
                            Toast.makeText(getApplicationContext()
                                    , "I have not idea how you come to this exception"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.postLocationPermissionTitle)
                        .setMessage(R.string.postLocationPermissionContent)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(NewEntryActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(NewEntryActivity.this,"Ok Grasia!!!"
                                ,Toast.LENGTH_LONG).show();
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return uLocationStatus = true;
        }
    }

    public boolean checkFilesPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(this)
                        .setTitle("Storage Access")//R.string.title_location_permission)
                        .setMessage("This app needs write access, to unlock all features.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(NewEntryActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(NewEntryActivity.this,"Ok Grasia!!!"
                                        ,Toast.LENGTH_LONG).show();
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            }
            return false;
        } else {
            return checkWritePermissionStatus = true;
        }
    }

    public boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                new AlertDialog.Builder(this)
                        .setTitle("Camera Access")//R.string.title_location_permission)
                        .setMessage("This app needs camera access, to unlock all features.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(NewEntryActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        MY_PERMISSIONS_REQUEST_CAMERA);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(NewEntryActivity.this,"Ok Grasia!!!"
                                        ,Toast.LENGTH_LONG).show();
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
            return false;
        } else {
            return checkCameraPermissionStatus = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uLocationStatus = true;
                    SetMyLocation();
                    if (!checkWritePermissionStatus){
                        checkFilesPermission();
                    }else if (!checkCameraPermissionStatus){
                        checkCameraPermission();
                    }
                } else {
                    uLocation = null;
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkCameraPermissionStatus = true;
                    takePhotoButton();
                    if (!uLocationStatus){
                        checkLocationPermission();
                    }else if (!checkWritePermissionStatus){
                        checkCameraPermission();
                    }
                } else {}
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkWritePermissionStatus = true;
                    takePhotoButton();
                    if (!uLocationStatus){
                        checkLocationPermission();
                    }else if (!checkCameraPermissionStatus){
                        checkCameraPermission();
                    }
                } else {}
                return;
            }
        }

    }


    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error occurred while creating the File: ", ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.cuchichapp.android.cuchichapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                selectImageUri = photoURI;
                startActivityForResult(takePictureIntent, RC_CAMERA);
            }
        }
    }


}
