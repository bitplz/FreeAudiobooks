
package com.bug32.librivoxaudiobooks;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable{

    private String mImgUrl;
    private String mtitle;
    private String mSummary;
    private String mAuthor;
    private String mUrl_text;
    private String m3uUrl;
    private List<String> mSections;
    private List<String> mGenres;


    public Product(String title, String summary, String imgUrl, String author, String url_text, String m3uUrl, List<String> sections, List<String> gernres) {
        this.mtitle = title;
        this.mImgUrl = imgUrl;
        this.mSummary = summary;
        this.mAuthor = author;
        this.mUrl_text = url_text;
        this.m3uUrl = m3uUrl;
        this.mSections = sections;
        this.mGenres = gernres;

    }

    public String getMtitle() {
        return mtitle;
    }

    public void setMtitle(String mtitle) {
        this.mtitle = mtitle;
    }

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setmImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }

    public String getmSummary() {
        return mSummary;
    }

    public void setmSummary(String mSummary) {
        this.mSummary = mSummary;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public String getM3uUrl() {
        return m3uUrl;
    }

    public void setM3uUrl(String m3uUrl) {
        this.m3uUrl = m3uUrl;
    }

    public void setmAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getmUrl_text() {
        return mUrl_text;
    }

    public void setmUrl_text(String mUrl_text) {
        this.mUrl_text = mUrl_text;
    }

    public List<String> getmSections() {
        return mSections;
    }

    public void setmSections(List<String> mSections) {
        this.mSections = mSections;
    }

    public List<String> getmGenres() {
        return mGenres;
    }

    public void setmGenres(List<String> mGenres) {
        this.mGenres = mGenres;
    }
}
