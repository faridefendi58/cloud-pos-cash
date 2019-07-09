package com.slightsite.app.domain.warehouse;

import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.warehouse.WarehouseDao;

public class WarehouseService {
    private WarehouseCatalog warehouseCatalog;
    private static WarehouseService instance = null;
    private static WarehouseDao warehouseDao = null;

    /**
     * Constructs Data Access Object of inventory.
     * @throws NoDaoSetException if DAO is not exist.
     */
    private WarehouseService() throws NoDaoSetException {
        if (!isDaoSet()) {
            throw new NoDaoSetException();
        }

        warehouseCatalog = new WarehouseCatalog(warehouseDao);
    }

    /**
     * Determines whether the DAO already set or not.
     * @return true if the DAO already set; otherwise false.
     */
    public static boolean isDaoSet() {
        return warehouseDao != null;
    }

    /**
     * Sets the database connector.
     * @param dao Data Access Object of inventory.
     */
    public static void setWarehouseDao(WarehouseDao dao) {
        warehouseDao = dao;
    }

    /**
     * Returns param catalog using in this inventory.
     * @return param catalog using in this inventory.
     */
    public WarehouseCatalog getWarehouseCatalog() {
        return warehouseCatalog;
    }

    /**
     * Returns the instance of this singleton class.
     * @return instance of this class.
     * @throws NoDaoSetException if DAO was not set.
     */
    public static WarehouseService getInstance() throws NoDaoSetException {
        if (instance == null)
            instance = new WarehouseService();
        return instance;
    }
}
