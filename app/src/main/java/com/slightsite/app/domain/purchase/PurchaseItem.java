package com.slightsite.app.domain.purchase;

import java.io.Serializable;
import java.util.ArrayList;

public class PurchaseItem implements Serializable {

    private int issue_id;
    private String notes;
    private String issue_number;
    private Double price;
    private String status;
    private String created_at;

    public PurchaseItem(int issue_id) {
        this.issue_id = issue_id;
    }

    public int getIssueId() {
        return issue_id;
    }

    public void setNotes(String _notes) {
        this.notes = _notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setIssueNumber(String issueNumber) {
        this.issue_number = issueNumber;
    }

    public String getIssueNumber() {
        return issue_number;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getCreatedAt() {
        return created_at;
    }
}