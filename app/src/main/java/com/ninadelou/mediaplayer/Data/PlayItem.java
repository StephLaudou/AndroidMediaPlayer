package com.ninadelou.mediaplayer.Data;

public class PlayItem {
    private int mId;
    private String mName;
    private String mAuthor;
    private String mRecord;
    private String mURL;
    private String mFile;

    public PlayItem(int id, String name, String author,
                    String record, String url, String file) {
        this.mId = id;
        this.mName = name;
        this.mAuthor = author;
        this.mRecord = record;
        this.mURL = url;
        this.mFile = file;
    }

    public int getId() {
        return this.mId;
    }

    public String getName() {
        return this.mName;
    }

    public String getAuthor() {
        return this.mAuthor;
    }

    public String getRecord() {
        return this.mRecord;
    }

    public String getUrl() {
        return this.mURL;
    }

    public String getFile() {
        return this.mFile;
    }

    public void setFile(String file) {
        this.mFile = file;
    }

    //@androidx.annotation.NonNull
    @Override
    public String toString() {
        return this.mName + " " +this.mAuthor;

    }
}