package com.slightsite.app.domain.customer;

import java.util.List;

import com.slightsite.app.techicalservices.customer.CustomerDao;

/**
 *
 * @author Farid Efendi
 *
 */
public class CustomerCatalog {

    private CustomerDao customerDao;

    /**
     * Constructs Data Access Object of inventory in CustomerCatalog.
     * @param customerDao DAO of inventory.
     */
    public CustomerCatalog(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /**
     * Constructs customer and adds customer to inventory.
     * @param name name of customer.
     * @param email barcode of customer.
     * @param phone price of customer.
     * @return true if customer adds in inventory success ; otherwise false.
     */
    public boolean addCustomer(String name, String email, String phone, String address, int status) {
        Customer customer = new Customer(name, email, phone, address, status);
        int id = customerDao.addCustomer(customer);
        return id != -1;
    }

    /**
     * Edits customer.
     * @param customer the customer to be edited.
     * @return true if customer edits success ; otherwise false.
     */
    public boolean editCustomer(Customer customer) {
        boolean respond = customerDao.editCustomer(customer);
        return respond;
    }

    /**
     * Returns customer from inventory finds by email.
     * @param email of customer.
     * @return customer
     */
    public Customer getCustomerByEmail(String email) {
        return customerDao.getCustomerByEmail(email);
    }

    /**
     * Returns customer from inventory finds by id.
     * @param id id of customer.
     * @return customer
     */
    public Customer getCustomerById(int id) {
        return customerDao.getCustomerById(id);
    }

    /**
     * Returns list of all customers in inventory.
     * @return list of all customers in inventory.
     */
    public List<Customer> getAllCustomer() {
        return customerDao.getAllCustomer();
    }

    /**
     * Returns list of customer in inventory finds by name.
     * @param name name of customer.
     * @return list of customer in inventory finds by name.
     */
    public List<Customer> getCustomerByName(String name) {
        return customerDao.getCustomerByName(name);
    }

    /**
     * Search customer from string in inventory.
     * @param search string for searching.
     * @return list of customer.
     */
    public List<Customer> searchCustomer(String search) {
        return customerDao.searchCustomer(search);
    }

    /**
     * Clears CustomerCatalog.
     */
    public void clearCustomerCatalog() {
        customerDao.clearCustomerCatalog();
    }

    /**
     * Hidden customer from inventory.
     * @param customer The customer to be hidden.
     */
    public void suspendCustomer(Customer customer) {
        customerDao.suspendCustomer(customer);
    }


}

