package com.slightsite.app.techicalservices.shipping;

import com.slightsite.app.domain.sale.Shipping;

import java.util.List;

/**
 *
 * @author Farid Efendi
 *
 */
public interface ShippingDao {

    int addShipping(Shipping payment);

    boolean editShipping(Shipping payment);

    Shipping getShippingById(int id);

    Shipping getShippingBySaleId(int sale_id);

    List<Shipping> getAllShipping();

    List<Shipping> searchShipping(String search);

    void clearShippingCatalog();

    void suspendShipping(Shipping payment);
}

