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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.souto.mltestapp.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class Classify extends AppCompatActivity {

    public TextView result, confidence;
    public ImageView imageView;
    public Button picture;
    int imageSize = 224;

    // Photo taken by the user is stored in this variable
    private File photoFile;
    private static final String FILE_NAME = "photo";

    private static final int CAPTURE_IMAGE_REQUEST = 100;

    // Bottom nav bar
    public BottomNavigationView bottomNavigationView;
    public ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); // Hide Status
        setContentView(R.layout.activity_classify);

        // If the user haven't allowed the camera permission yet, request it
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.button);

        picture.setOnClickListener(v -> {
            try {
                takePicture();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Set up the navigation tab
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_Classify);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.page_Train:
                        startActivity(new Intent(Classify.this, Train.class));
                        finish();
//                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.page_Settings:
                        startActivity(new Intent(Classify.this, Settings.class));
                        finish();
//                        viewPager.setCurrentItem(2);
                        break;
                    default:
//                        viewPager.setCurrentItem(0);
                }
                return true;
            }
        });
    }

    public void takePicture() throws IOException {
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

    private void classifyImage(Bitmap figure) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(figure);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(image);
            TensorBuffer probability = outputs.getProbabilityAsTensorBuffer();
//            Log.d("Debug", probability.get(0) +"");

            float[] confidences = probability.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Not Safe","Safe"};
            if(classes[maxPos].equals("Safe")){
                result.setTextColor(getResources().getColor(R.color.app_green));
            }else{
                result.setTextColor(getResources().getColor(R.color.app_red));
            }
            result.setText(classes[maxPos]);

            String s = "";
            for(int i = 0; i < classes.length; i++){
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }

            confidence.setText(s);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // To take a photo
        if(requestCode==CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            String filePath = photoFile.getPath();
            Bitmap image = BitmapFactory.decodeFile(filePath);

            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

            imageView.setRotation(90);

            Glide.with(this)
                    .load(image)
                    .into(imageView);


            // Rotating the bitmap
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);
            image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

            classifyImage(image);
        }
    }
}