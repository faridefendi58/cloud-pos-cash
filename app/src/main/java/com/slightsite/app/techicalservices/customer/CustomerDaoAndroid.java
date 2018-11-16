package com.slightsite.app.techicalservices.customer;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;

import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;

/**
 * DAO used by android for customer.
 *
 * @author Farid Efendi
 *
 */
public class CustomerDaoAndroid implements CustomerDao {

    private Database database;

    /**
     * Constructs CustomerDaoAndroid.
     * @param database database for use in CustomerDaoAndroid.
     */
    public CustomerDaoAndroid(Database database) {
        this.database = database;
    }

    @Override
    public int addCustomer(Customer customer) {
        ContentValues content = new ContentValues();
        content.put("name", customer.getName());
        content.put("email", customer.getEmail());
        content.put("phone", customer.getPhone());
        content.put("address", customer.getAddress());
        content.put("status", 1);

        int id = database.insert(DatabaseContents.TABLE_CUSTOMER.toString(), content);

        return id;
    }

    /**
     * Converts list of object to list of customer.
     * @param objectList list of object.
     * @return list of customer.
     */
    private List<Customer> toCustomerList(List<Object> objectList) {
        List<Customer> list = new ArrayList<Customer>();
        for (Object object: objectList) {
            ContentValues content = (ContentValues) object;
            list.add(new Customer(
                    content.getAsInteger("_id"),
                    content.getAsString("name"),
                    content.getAsString("email"),
                    content.getAsString("phone"),
                    content.getAsString("address"),
                    content.getAsInteger("status"))
            );
        }
        return list;
    }

    @Override
    public List<Customer> getAllCustomer() {
        return getAllCustomer(" WHERE status = 1");
    }

    /**
     * Returns list of all customer in inventory.
     * @param condition specific condition for getAllCustomer.
     * @return list of all customer in inventory.
     */
    private List<Customer> getAllCustomer(String condition) {
        String queryString = "SELECT * FROM " + DatabaseContents.TABLE_CUSTOMER.toString() + condition + " ORDER BY name";
        List<Customer> list = toCustomerList(database.select(queryString));
        return list;
    }

    /**
     * Returns customer from inventory finds by specific reference.
     * @param reference reference value.
     * @param value value for search.
     * @return list of customer.
     */
    private List<Customer> getCustomerBy(String reference, String value) {
        String condition = " WHERE " + reference + " = " + value + " ;";
        return getAllCustomer(condition);
    }

    /**
     * Returns customer from inventory finds by similar name.
     * @param reference reference value.
     * @param value value for search.
     * @return list of customer.
     */
    private List<Customer> getSimilarCustomerBy(String reference, String value) {
        String condition = " WHERE " + reference + " LIKE '%" + value + "%' ;";
        return getAllCustomer(condition);
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        List<Customer> list = getCustomerBy("email", email);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    @Override
    public Customer getCustomerById(int id) {
        return getCustomerBy("_id", id+"").get(0);
    }

    @Override
    public boolean editCustomer(Customer customer) {
        ContentValues content = new ContentValues();
        content.put("_id", customer.getId());
        content.put("name", customer.getName());
        content.put("email", customer.getEmail());
        content.put("phone", customer.getPhone());
        content.put("address", customer.getAddress());
        content.put("status", 1);
        return database.update(DatabaseContents.TABLE_CUSTOMER.toString(), content);
    }

    @Override
    public List<Customer> getCustomerByName(String name) {
        return getSimilarCustomerBy("name", name);
    }

    @Override
    public List<Customer> searchCustomer(String search) {
        String condition = " WHERE name LIKE '%" + search + "%' OR email LIKE '%" + search + "%' ;";
        return getAllCustomer(condition);
    }

    @Override
    public void clearCustomerCatalog() {
        database.execute("DELETE FROM " + DatabaseContents.TABLE_CUSTOMER);
    }

    @Override
    public void suspendCustomer(Customer customer) {
        ContentValues content = new ContentValues();
        content.put("_id", customer.getId());
        content.put("name", customer.getName());
        content.put("email", customer.getEmail());
        content.put("status", 0);
        database.update(DatabaseContents.TABLE_CUSTOMER.toString(), content);
    }
}

