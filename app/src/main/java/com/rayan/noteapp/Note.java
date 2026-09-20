package com.rayan.noteapp;

public class Note {

    public long id;
    public String title = "";
    public String body = "";
    public String folder = "General";
    public String tags = "";

    public long created;
    public long updated;

    public int pinned;
    public int favorite;
    public int archived;
    public int trash;

    public int color;

    public String attachment = "";
    public long reminder;

    public boolean isPinned() {
        return pinned == 1;
    }

    public boolean isFavorite() {
        return favorite == 1;
    }

    public boolean isArchived() {
        return archived == 1;
    }

    public boolean isTrash() {
        return trash == 1;
    }
}
