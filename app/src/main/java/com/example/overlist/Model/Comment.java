package com.example.overlist.Model;

public class Comment {
    private String comment, date, postid, publisher;

    public Comment(String comment, String date, String postid, String publisher) {
        this.comment = comment;
        this.date = date;
        this.postid = postid;
        this.publisher = publisher;
    }

    public Comment() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
