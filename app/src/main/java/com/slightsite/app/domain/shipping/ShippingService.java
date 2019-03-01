package com.slightsite.app.domain.shipping;

import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.shipping.ShippingDao;

public class ShippingService {

    private ShippingCatalog shippingCatalog;
    private static ShippingService instance = null;
    private static ShippingDao shippingDao = null;

    /**
     * Constructs Data Access Object of inventory.
     * @throws NoDaoSetException if DAO is not exist.
     */
    private ShippingService() throws NoDaoSetException {
        if (!isDaoSet()) {
            throw new NoDaoSetException();
        }

        shippingCatalog = new ShippingCatalog(shippingDao);
    }

    /**
     * Determines whether the DAO already set or not.
     * @return true if the DAO already set; otherwise false.
     */
    public static boolean isDaoSet() {
        return shippingDao != null;
    }

    /**
     * Sets the database connector.
     * @param dao Data Access Object of inventory.
     */
    public static void setShippingDao(ShippingDao dao) {
        shippingDao = dao;
    }

    /**
     * Returns param catalog using in this inventory.
     * @return param catalog using in this inventory.
     */
    public ShippingCatalog getShippingCatalog() {
        return shippingCatalog;
    }

    /**
     * Returns the instance of this singleton class.
     * @return instance of this class.
     * @throws NoDaoSetException if DAO was not set.
     */
    public static ShippingService getInstance() throws NoDaoSetException {
        if (instance == null)
            instance = new ShippingService();
        return instance;
    }
}

