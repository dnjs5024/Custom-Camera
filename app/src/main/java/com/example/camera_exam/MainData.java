package com.example.camera_exam;

public class MainData {

    private String iv_profile;
    private String tv_title;

    public MainData(){

    }

    public MainData(String iv_profile, String tv_title) {
        this.iv_profile = iv_profile;
        this.tv_title = tv_title;
    }

    public String getIv_profile() {
        return iv_profile;
    }

    public void setIv_profile(String iv_profile) {
        this.iv_profile = iv_profile;
    }

    public String getTv_title() {
        return tv_title;
    }

    public void setTv_title(String tv_title) {
        this.tv_title = tv_title;
    }
}
