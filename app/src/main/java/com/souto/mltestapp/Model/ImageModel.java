package com.souto.mltestapp.Model;

import com.google.firebase.database.Exclude;

public class ImageModel {

    private String ImageDate;
    private String ImageUri;
    private String mKey;

    public ImageModel() {
    }

    public ImageModel(String imageDate, String imageUri) {
        ImageDate = imageDate;
        ImageUri = imageUri;
    }

    public String getImageDate() {
        return ImageDate;
    }

    public void setImageDate(String imageDate) {
        ImageDate = imageDate;
    }

    public String getImageUri() {
        return ImageUri;
    }

    public void setImageUri(String imageUri) {
        ImageUri = imageUri;
    }

    // Excludes the key from the database
    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String mKey) {
        this.mKey = mKey;
    }
}
