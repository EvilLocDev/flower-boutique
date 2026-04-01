package com.example.flowerboutique.ui.productdetail;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class Review {
    private String content;
    private int rate;
    private DocumentReference user;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    public Review() {

    }

    public Review(String content, int rate, DocumentReference user, Timestamp createdDate, Timestamp updatedDate) {
        this.content = content;
        this.rate = rate;
        this.user = user;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }
}
