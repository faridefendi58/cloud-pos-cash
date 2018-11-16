package com.slightsite.app.domain.customer;

import java.util.HashMap;
import java.util.Map;

import com.slightsite.app.techicalservices.NoDaoSetException;

/**
 *
 * @author Customer
 *
 */
public class Customer {

    private int id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private int status;

    /**
     * Static value for UNDEFINED ID.
     */
    public static final int UNDEFINED_ID = -1;


    public Customer(int id, String name, String email, String phone, String address, int status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status;
    }

    public Customer(String name, String email, String phone, String address, int status) {
        this(UNDEFINED_ID, name, email, phone, address, status);
    }

    /**
     * Returns name of this customer.
     * @return name of this customer.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of this customer.
     * @param name name of this customer.
     */
    public void setName(String name) {
        this.name = name;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setStatus(String status) {
        this.address = status;
    }

    /**
     * Returns id of this customer.
     * @return id of this customer.
     */
    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public int getStatus() {
        return status;
    }

    /**
     * Returns the description of this Customer in Map format.
     * @return the description of this Customer in Map format.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id + "");
        map.put("name", name);
        map.put("email", email);
        map.put("phone", phone);
        map.put("address", address);
        map.put("status", status + "");

        return map;

    }
}

