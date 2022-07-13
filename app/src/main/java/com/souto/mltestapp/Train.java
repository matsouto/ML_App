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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputLayout;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Train extends AppCompatActivity {

    private FirebaseAuth mAuth;

    public Button btn_camera, btn_gallery, btn_save;
    public ImageView img_test;
    public ProgressBar progressBar;

    public TextInputLayout edt_comments;

    public Uri fileProvider;

    public LinearLayout buttonPannel;

    public Spinner spinner;

    // Photo taken by the user is stored in this variable
    private File photoFile;
    private static final String FILE_NAME = "photo.jpg";

    private static final int CAPTURE_IMAGE_REQUEST = 100;
    private static final int PICK_IMAGE_REQUEST = 101;

    // Bottom nav bar
    public BottomNavigationView bottomNavigationView;
    public ViewPager viewPager;

    public String Label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); // Hide Status
        setContentView(R.layout.activity_train);

        // If the user haven't allowed the camera permission yet, request it
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        edt_comments = findViewById(R.id.comments);

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
                        finish();
//                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.page_Settings:
                        startActivity(new Intent(Train.this, Settings.class));
                        finish();
//                        viewPager.setCurrentItem(2);
                        break;
                    default:
//                        viewPager.setCurrentItem(0);
                }
                return true;
            }
        });

        spinner = findViewById(R.id.spinner);
        List<String> listaSpinner = new ArrayList<>();
        listaSpinner.add("Select Label");
        listaSpinner.add("Aprovado");
        listaSpinner.add("Problema A");
        listaSpinner.add("Problema B");
        listaSpinner.add("Outro");

        // Adiciona a lista em seu adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, listaSpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // Muda a cor e tamanho do item do spinner
                int myColor = Color.parseColor("#c8c8c8");
                ((TextView) adapterView.getChildAt(0)).setTextColor(myColor);
                ((TextView) adapterView.getChildAt(0)).setTextSize(18);

                if(adapterView.getItemAtPosition(i).equals("Select Label")) {
                    // do nothing
                }else{
                    Label = adapterView.getItemAtPosition(i).toString();
                    if (Label == "Outro") {

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        img_test = findViewById(R.id.img_test);
        buttonPannel = findViewById(R.id.buttonPanel);
        progressBar = findViewById(R.id.progress_bar);

        btn_camera = findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    takePicture();
                } catch (Exception e) {
                    Log.d("Error", ""+e);
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

        btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Label.equals("Select Label")){
                    Toast.makeText(Train.this,"You must select the label!",Toast.LENGTH_SHORT).show();
                }else{
                    uploadImageFirebase(fileProvider);
                }
            }
        });

    }

    // Sets image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        bottomNavigationView.setSelectedItemId(R.id.page_Train);

        // To take a photo
        if(requestCode==CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Glide.with(this).load(photoFile).into(img_test);
            fileProvider = FileProvider.getUriForFile(this, "com.souto.MLprovider", photoFile);
            spinner.setVisibility(View.VISIBLE);
            btn_save.setVisibility(View.VISIBLE);
            buttonPannel.setVisibility(View.GONE);
            edt_comments.setVisibility(View.VISIBLE);
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

        // Gets the current time to name the photos in storage
        Date currentTime = Calendar.getInstance().getTime();

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = mDatabase.getReference().child("users").child(mAuth.getUid()).child("pics").child(Label);

        // Gets firebase storage reference
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        StorageReference mRef = mStorage.getReference().child("users").child(mAuth.getUid()).child(Label).child(""+currentTime);

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

                        ImageModel image = new ImageModel("" + currentTime, taskSnapshot.getUploadSessionUri().toString(), edt_comments.getEditText().getText().toString());
                        mDatabaseRef.child("" + currentTime).setValue(image);
                        Toast.makeText(Train.this,"Uploaded successfully to database!",Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.GONE);
                        btn_save.setVisibility(View.GONE);
                        buttonPannel.setVisibility(View.VISIBLE);
                        edt_comments.setVisibility(View.GONE);
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
