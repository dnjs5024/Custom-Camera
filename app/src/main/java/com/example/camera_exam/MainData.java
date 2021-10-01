package com.example.camera_exam;

import android.net.Uri;

public class MainData {

    private int iv_profile;
    private String tv_title;
    private Uri iv_setProfile;


    public MainData(){

    }



    public MainData(int iv_profile, String tv_title, Uri iv_setProfile) {
        this.iv_profile = iv_profile;
        this.tv_title = tv_title;
        this.iv_setProfile = iv_setProfile;

    }

    public Uri getIv_setProfile() {
        return iv_setProfile;
    }

    public void setIv_setProfile(Uri iv_setProfile) {
        this.iv_setProfile = iv_setProfile;
    }
    public int getIv_profile() {
        return iv_profile;
    }

    public void setIv_profile(int iv_profile) {
        this.iv_profile = iv_profile;
    }

    public String getTv_title() {
        return tv_title;
    }

    public void setTv_title(String tv_title) {
        this.tv_title = tv_title;
    }
}

