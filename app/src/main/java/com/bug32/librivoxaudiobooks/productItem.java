package com.bug32.librivoxaudiobooks;

public class productItem {
    private String mtitle;
    private String mYear;
    private String mlanguage;

    public productItem (String title, String year, String language){
        mtitle = title;
        mYear = year;
        mlanguage = language;
    }

    public String getMtitle() {
        return mtitle;
    }

    public String getmYear() {
        return mYear;
    }

    public String getMlanguage() {
        return mlanguage;
    }
}
