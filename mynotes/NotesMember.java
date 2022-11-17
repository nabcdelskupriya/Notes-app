package com.example.mynotes;

public class NotesMember {

    String title;
    String note;
    String time;
    String type;
    String search;
    String uriimage;
    long delete;

    public NotesMember(){

    }
    public long getDelete() {
        return delete;
    }

    public void setDelete(long delete) {
        this.delete = delete;
    }


    public String getUriimage() {
        return uriimage;
    }

    public void setUriimage(String uriimage) {
        this.uriimage = uriimage;
    }


    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
