package com.slightsite.app.techicalservices.payment;

import android.content.ContentValues;
import android.util.Log;

import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;

import java.util.ArrayList;
import java.util.List;

public class PaymentDaoAndroid implements PaymentDao {

    private Database database;

    /**
     * Constructs CustomerDaoAndroid.
     * @payment database database for use in CustomerDaoAndroid.
     */
    public PaymentDaoAndroid(Database database) {
        this.database = database;
    }

    @Override
    public int addPayment(Payment payment) {
        ContentValues content = new ContentValues();
        content.put("sale_id", payment.getSaleId());
        content.put("payment_channel", payment.getPaymentChannel());
        content.put("amount", payment.getAmount());

        int id = database.insert(DatabaseContents.TABLE_SALE_PAYMENT.toString(), content);
        Log.e(getClass().getSimpleName(), "saved id : "+ id);
        Log.e(getClass().getSimpleName(), "content : "+ content.toString());

        return id;
    }

    /**
     * Converts list of object to list of payment.
     * @payment objectList list of object.
     * @return list of payment.
     */
    private List<Payment> toPaymentList(List<Object> objectList) {
        List<Payment> list = new ArrayList<Payment>();
        for (Object object: objectList) {
            ContentValues content = (ContentValues) object;
            list.add(new Payment(
                    content.getAsInteger("_id"),
                    content.getAsInteger("sale_id"),
                    content.getAsString("payment_channel"),
                    content.getAsDouble("amount"))
            );
        }
        return list;
    }

    @Override
    public List<Payment> getAllPayment() {
        return getAllPayment(" WHERE 1");
    }

    /**
     * Returns list of all payments.
     * @payment condition specific condition for getAllParam.
     * @return list of all payments.
     */
    private List<Payment> getAllPayment(String condition) {
        String queryString = "SELECT * FROM " + DatabaseContents.TABLE_SALE_PAYMENT.toString() + condition + " ORDER BY sale_id";
        List<Payment> list = toPaymentList(database.select(queryString));
        return list;
    }

    /**
     * Returns payments.
     * @payment reference reference value.
     * @payment value value for search.
     * @return list of payments.
     */
    private List<Payment> getPaymentBy(String reference, String value) {
        String condition = " WHERE " + reference + " = '" + value + "' ;";
        return getAllPayment(condition);
    }

    @Override
    public  List<Payment> getPaymentBySaleId(int sale_id) {
        List<Payment> list = getPaymentBy("sale_id", sale_id +"");
        if (list.isEmpty()) return null;
        return list;
    }

    @Override
    public Payment getPaymentById(int id) {
        return getPaymentBy("_id", id+"").get(0);
    }

    @Override
    public boolean editPayment(Payment payment) {
        ContentValues content = new ContentValues();
        content.put("_id", payment.getId());
        content.put("sale_id", payment.getSaleId());
        content.put("payment_channel", payment.getPaymentChannel());
        content.put("amount", payment.getAmount());

        return database.update(DatabaseContents.TABLE_SALE_PAYMENT.toString(), content);
    }

    @Override
    public List<Payment> searchPayment(String search) {
        String condition = " WHERE payment_channel LIKE '%" + search + "%' ;";
        return getAllPayment(condition);
    }

    @Override
    public void clearPaymentCatalog() {
        database.execute("DELETE FROM " + DatabaseContents.TABLE_SALE_PAYMENT);
    }

    @Override
    public void suspendPayment(Payment payment) {
        ContentValues content = new ContentValues();
        content.put("_id", payment.getId());
        content.put("sale_id", payment.getSaleId());
        content.put("payment_channel", payment.getPaymentChannel());
        content.put("amount", payment.getAmount());

        database.update(DatabaseContents.TABLE_SALE_PAYMENT.toString(), content);
    }

    @Override
    public void removePayment(int id) {
        database.delete(DatabaseContents.TABLE_SALE_PAYMENT.toString(), id);
    }
}
