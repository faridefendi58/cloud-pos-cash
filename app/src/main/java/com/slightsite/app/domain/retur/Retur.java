package com.slightsite.app.domain.retur;

import com.slightsite.app.domain.customer.Customer;

import java.io.Serializable;
import java.util.ArrayList;

public class Retur implements Serializable {

    private int server_invoice_id;
    private ArrayList payments = new ArrayList();
    private ArrayList items = new ArrayList();
    private ArrayList items_change = new ArrayList();
    private ArrayList<String> items_reason = new ArrayList<String>();
    private Customer customer;
    private String notes;

    public Retur(int server_invoice_id) {
        this.server_invoice_id = server_invoice_id;
    }

    public int getServerInvoiceId() {
        return server_invoice_id;
    }

    public void setPayment(ArrayList _payment) {
        this.payments = _payment;
    }

    public ArrayList getPayment() {
        return payments;
    }

    public void setItems(ArrayList _items) {
        this.items = _items;
    }

    public ArrayList getItems() {
        return items;
    }

    public void setCustomer(Customer _cust) {
        this.customer = _cust;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setItemsChange(ArrayList _change_item) {
        this.items_change = _change_item;
    }
    public ArrayList getItemsChange() {
        return items_change;
    }

    public void setItemsReason(ArrayList<String> _reason) {
        this.items_reason = _reason;
    }

    public ArrayList<String> getItemsReason(){
        return items_reason;
    }

    public void setNotes(String _notes) {
        this.notes = _notes;
    }

    public String getNotes() {
        return notes;
    }
}
