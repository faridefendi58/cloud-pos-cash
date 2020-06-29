package com.slightsite.app.ui.deposit;

import com.slightsite.app.domain.customer.Customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Deposit implements Serializable {

    private int server_invoice_id;
    private ArrayList items = new ArrayList();
    private Customer customer;
    private String notes;
    private Map<Integer, Integer> avail_product_qty_stacks = new HashMap<Integer, Integer>();

    public Deposit(int server_invoice_id) {
        this.server_invoice_id = server_invoice_id;
    }

    public int getServerInvoiceId() {
        return server_invoice_id;
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

    public void setNotes(String _notes) {
        this.notes = _notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setAvailableQty(Map<Integer, Integer> avail_product) {
        this.avail_product_qty_stacks = avail_product;
    }

    public Map<Integer, Integer> getAvailableQty() {
        return avail_product_qty_stacks;
    }
}
