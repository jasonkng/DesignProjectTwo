package com.jason.designprojecttwo.Utility;

public class UploadModel {
    private String imageUrl;

    public UploadModel(){
    }

    public UploadModel(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
