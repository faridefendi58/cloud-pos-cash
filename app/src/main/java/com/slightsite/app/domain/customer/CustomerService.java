package com.slightsite.app.domain.customer;

import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.customer.CustomerDao;

/**
 * This class is service locater for Customer.
 *
 * @author Farid Efendi
 *
 */
public class CustomerService {

    private CustomerCatalog customerCatalog;
    private static CustomerService instance = null;
    private static CustomerDao customerDao = null;

    /**
     * Constructs Data Access Object of inventory.
     * @throws NoDaoSetException if DAO is not exist.
     */
    private CustomerService() throws NoDaoSetException {
        if (!isDaoSet()) {
            throw new NoDaoSetException();
        }

        customerCatalog = new CustomerCatalog(customerDao);
    }

    /**
     * Determines whether the DAO already set or not.
     * @return true if the DAO already set; otherwise false.
     */
    public static boolean isDaoSet() {
        return customerDao != null;
    }

    /**
     * Sets the database connector.
     * @param dao Data Access Object of inventory.
     */
    public static void setCustomerDao(CustomerDao dao) {
        customerDao = dao;
    }

    /**
     * Returns customer catalog using in this inventory.
     * @return customer catalog using in this inventory.
     */
    public CustomerCatalog getCustomerCatalog() {
        return customerCatalog;
    }

    /**
     * Returns the instance of this singleton class.
     * @return instance of this class.
     * @throws NoDaoSetException if DAO was not set.
     */
    public static CustomerService getInstance() throws NoDaoSetException {
        if (instance == null)
            instance = new CustomerService();
        return instance;
    }
}

