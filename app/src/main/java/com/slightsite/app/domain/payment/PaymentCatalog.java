package com.slightsite.app.domain.payment;

import com.slightsite.app.techicalservices.payment.PaymentDao;

import java.util.List;

public class PaymentCatalog {
    private PaymentDao paymentDao;

    /**
     * Constructs Data Access Object of inventory in PaymentCatalog.
     * @payment paymentDao DAO of payment.
     */
    public PaymentCatalog(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    /**
     * @return
     */
    public boolean addPayment(Integer sale_id, String payment_channel, Double amount) {
        Payment payment = new Payment(sale_id, payment_channel, amount);
        int id = paymentDao.addPayment(payment);
        return id != -1;
    }

    /**
     * @return
     */
    public boolean editPayment(Payment payment) {
        boolean respond = paymentDao.editPayment(payment);
        return respond;
    }

    /**
     * @payment name
     * @return
     */
    public Payment getPaymentBySaleId(int sale_id) {
        return paymentDao.getPaymentBySaleId(sale_id);
    }

    /**
     * @payment id
     * @return
     */
    public Payment getPaymentById(int id) {
        return paymentDao.getPaymentById(id);
    }

    /**
     * Geting all paymenteter
     * @return
     */
    public List<Payment> getAllPayment() {
        return paymentDao.getAllPayment();
    }

    /**
     * Searching for some paymenteter
     * @payment search
     * @return
     */
    public List<Payment> searchPayment(String search) {
        return paymentDao.searchPayment(search);
    }

    /**
     * Clears PaymentCatalog.
     */
    public void clearPaymentCatalog() {
        paymentDao.clearPaymentCatalog();
    }

    /**
     * Suspend or dective payment
     * @payment payment
     */
    public void suspendPayment(Payment payment) {
        paymentDao.suspendPayment(payment);
    }
}
