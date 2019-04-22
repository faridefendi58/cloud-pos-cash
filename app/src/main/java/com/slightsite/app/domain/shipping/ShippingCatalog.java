package com.slightsite.app.domain.shipping;

import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.shipping.ShippingDao;

import java.util.List;

public class ShippingCatalog {
    private ShippingDao shippingDao;

    /**
     * Constructs Data Access Object of inventory in ShippingCatalog.
     * @payment shippingDao DAO of payment.
     */
    public ShippingCatalog(ShippingDao shippingDao) {
        this.shippingDao = shippingDao;
    }

    /**
     * @return
     */
    public boolean addShipping(int method, String date, String address, int warehouse_id) {
        Shipping payment = new Shipping(method, date, address, warehouse_id);
        int id = shippingDao.addShipping(payment);
        return id != -1;
    }

    /**
     * @return
     */
    public boolean editShipping(Shipping payment) {
        boolean respond = shippingDao.editShipping(payment);
        return respond;
    }

    /**
     * @payment name
     * @return
     */
    public Shipping getShippingBySaleId(int sale_id) {
        return shippingDao.getShippingBySaleId(sale_id);
    }

    /**
     * @payment id
     * @return
     */
    public Shipping getShippingById(int id) {
        return shippingDao.getShippingById(id);
    }

    /**
     * Geting all paymenteter
     * @return
     */
    public List<Shipping> getAllShipping() {
        return shippingDao.getAllShipping();
    }

    /**
     * Searching for some paymenteter
     * @payment search
     * @return
     */
    public List<Shipping> searchShipping(String search) {
        return shippingDao.searchShipping(search);
    }

    /**
     * Clears ShippingCatalog.
     */
    public void clearShippingCatalog() {
        shippingDao.clearShippingCatalog();
    }

    /**
     * Suspend or dective payment
     * @payment payment
     */
    public void suspendShipping(Shipping payment) {
        shippingDao.suspendShipping(payment);
    }
}

