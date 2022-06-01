package com.souto.mltestapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.souto.mltestapp.Auth.Login;
import com.souto.mltestapp.Model.ImageModel;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class Train extends AppCompatActivity {

    private FirebaseAuth mAuth;

    public Button btn_camera, btn_gallery;
    public ImageView img_test;
    public ProgressBar progressBar;

    // Photo taken by the user is stored in this variable
    private File photoFile;
    private static final String FILE_NAME = "photo.jpg";

    private static final int CAPTURE_IMAGE_REQUEST = 100;
    private static final int PICK_IMAGE_REQUEST = 101;

    // Bottom nav bar
    public BottomNavigationView bottomNavigationView;
    public ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); // Hide Status
        setContentView(R.layout.activity_train);

        // If the user haven't allowed the camera permission yet, request it
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        // Set up the navigation tab
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_Train);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.page_Classify:
                        startActivity(new Intent(Train.this, Classify.class));
//                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.page_Settings:
                        startActivity(new Intent(Train.this, Settings.class));
//                        viewPager.setCurrentItem(2);
                        break;
                    default:
//                        viewPager.setCurrentItem(0);
                }
                return true;
            }
        });

        img_test = findViewById(R.id.img_test);

        btn_camera = findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("debug", "Pressed");
                    takePicture();
                } catch (Exception e) {
                    Log.d("debug", ""+e);
                }
            }
        });

        btn_gallery = findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                openFileChooser();
            }
        });

        progressBar = findViewById(R.id.progress_bar);
    }

    // Sets image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        bottomNavigationView.setSelectedItemId(R.id.page_Train);

        // To take a photo
        if(requestCode==CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Glide.with(this).load(photoFile).into(img_test);
            Uri fileProvider = FileProvider.getUriForFile(this, "com.souto.MLprovider", photoFile);
            uploadImageFirebase(fileProvider);
        }

        // To select a photo from gallery
        if(requestCode==PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri mImageUri = (Uri) data.getData();
            Glide.with(this).load(mImageUri).into(img_test);
            uploadImageFirebase(mImageUri);
        }
    }

    // Gets image Uri and sends it to FBStorage
    private void uploadImageFirebase(Uri imageUri) {

        Uri mImageUri = imageUri;
        // Gets firebase authentication
        mAuth = FirebaseAuth.getInstance();
        Log.d("Debug", mAuth+"");

        // Gets the current time to name the photos in storage
        Date currentTime = Calendar.getInstance().getTime();

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = mDatabase.getReference().child("users").child(mAuth.getUid()).child("pics");

        // Gets firebase storage reference
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        StorageReference mRef = mStorage.getReference().child("users").child(mAuth.getUid()).child(""+currentTime);

        progressBar.setVisibility(View.VISIBLE);

        // Uploads file to storage
        mRef.putFile(mImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    // Delays the progress bar reset so the user can see it on 100%
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                progressBar.setProgress(0);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        },1000);

                        // O PROBLEMA TA AQUI NA HORA DE COLOCAR A URI ESSA URI TA ERRADA
                        ImageModel image = new ImageModel("" + currentTime, taskSnapshot.getUploadSessionUri().toString());
                        mDatabaseRef.child("" + currentTime).setValue(image);
                        Toast.makeText(Train.this,"Uploaded successfully to database!",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Train.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        // Gives the progress percentage of the bytes transferred and makes it visible
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressBar.setProgress((int) progress);
                    }
                });
    }

    private void takePicture() throws IOException {
        // Intent to capture the image
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFile(FILE_NAME);

        // Creates a Uri for the taken photo
        Uri fileProvider = FileProvider.getUriForFile(this, "com.souto.MLprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // Check if there is a camera
        if(intent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
        }else{
            Toast.makeText(this,"Unable to open camera!",Toast.LENGTH_SHORT).show();
        }
    }

    private File getPhotoFile(String fileName) throws IOException {
        // Gets taken photo and create a temporary file in the directory, returns the file
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName,".jpg",storageDirectory);
    }
}
