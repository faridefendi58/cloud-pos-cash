package com.slightsite.app.domain.payment;

import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.payment.PaymentDao;

public class PaymentService {

    private PaymentCatalog paymentCatalog;
    private static PaymentService instance = null;
    private static PaymentDao paymentDao = null;

    /**
     * Constructs Data Access Object of inventory.
     * @throws NoDaoSetException if DAO is not exist.
     */
    private PaymentService() throws NoDaoSetException {
        if (!isDaoSet()) {
            throw new NoDaoSetException();
        }

        paymentCatalog = new PaymentCatalog(paymentDao);
    }

    /**
     * Determines whether the DAO already set or not.
     * @return true if the DAO already set; otherwise false.
     */
    public static boolean isDaoSet() {
        return paymentDao != null;
    }

    /**
     * Sets the database connector.
     * @param dao Data Access Object of inventory.
     */
    public static void setPaymentDao(PaymentDao dao) {
        paymentDao = dao;
    }

    /**
     * Returns param catalog using in this inventory.
     * @return param catalog using in this inventory.
     */
    public PaymentCatalog getPaymentCatalog() {
        return paymentCatalog;
    }

    /**
     * Returns the instance of this singleton class.
     * @return instance of this class.
     * @throws NoDaoSetException if DAO was not set.
     */
    public static PaymentService getInstance() throws NoDaoSetException {
        if (instance == null)
            instance = new PaymentService();
        return instance;
    }
}
