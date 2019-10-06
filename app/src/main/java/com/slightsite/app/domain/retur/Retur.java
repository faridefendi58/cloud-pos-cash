package com.slightsite.app.domain.retur;

import com.slightsite.app.domain.customer.Customer;

import java.io.Serializable;
import java.util.ArrayList;

public class Retur implements Serializable {

    private int server_invoice_id;
    private ArrayList payments = new ArrayList();
    private ArrayList items = new ArrayList();
    private Customer customer;

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
}
