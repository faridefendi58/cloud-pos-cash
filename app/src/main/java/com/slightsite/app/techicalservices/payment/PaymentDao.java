package com.slightsite.app.techicalservices.payment;

import java.util.List;

import com.slightsite.app.domain.payment.Payment;

/**
 *
 * @author Farid Efendi
 *
 */
public interface PaymentDao {

    int addPayment(Payment payment);

    boolean editPayment(Payment payment);

    Payment getPaymentById(int id);

    List<Payment> getPaymentBySaleId(int sale_id);

    List<Payment> getAllPayment();

    List<Payment> searchPayment(String search);

    void clearPaymentCatalog();

    void suspendPayment(Payment payment);

    void removePayment(int id);
}
