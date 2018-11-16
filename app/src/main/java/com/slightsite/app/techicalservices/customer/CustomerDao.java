package com.slightsite.app.techicalservices.customer;

import java.util.List;

import com.slightsite.app.domain.customer.Customer;

/**
 *
 * @author Farid Efendi
 *
 */
public interface CustomerDao {

    /**
     * Add customer data
     * @param customer
     * @return
     */
    int addCustomer(Customer customer);

    /**
     * Edit customer
     * @param customer
     * @return
     */
    boolean editCustomer(Customer customer);

    /**
     * Returns customer finds by id.
     * @param id
     * @return
     */
    Customer getCustomerById(int id);

    Customer getCustomerByEmail(String email);

    /**
     * Returns list of all customer
     * @return list of all customer
     */
    List<Customer> getAllCustomer();

    List<Customer> getCustomerByName(String name);

    /**
     * @param search
     * @return
     */
    List<Customer> searchCustomer(String search);

    void clearCustomerCatalog();

    void suspendCustomer(Customer customer);
}

