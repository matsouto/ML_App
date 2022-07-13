package com.souto.mltestapp.Model;

import com.google.firebase.database.Exclude;

public class ImageModel {

    private String ImageDate;
    private String ImageUri;
    private String mKey;
    private String ImageComment;

    public ImageModel() {
    }

    public String getImageComment() {
        return ImageComment;
    }

    public void setImageComment(String imageComment) {
        ImageComment = imageComment;
    }

    public ImageModel(String imageDate, String imageUri, String imageComment) {
        ImageDate = imageDate;
        ImageUri = imageUri;
        ImageComment = imageComment;
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
