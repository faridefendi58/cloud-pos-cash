package com.slightsite.app.domain.warehouse;

import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.warehouse.AdminInWarehouseDao;

public class AdminInWarehouseService {
    private AdminInWarehouseCatalog adminInWarehouseCatalog;
    private static AdminInWarehouseService instance = null;
    private static AdminInWarehouseDao adminInWarehouseDao = null;

    /**
     * Constructs Data Access Object of inventory.
     * @throws NoDaoSetException if DAO is not exist.
     */
    private AdminInWarehouseService() throws NoDaoSetException {
        if (!isDaoSet()) {
            throw new NoDaoSetException();
        }

        adminInWarehouseCatalog = new AdminInWarehouseCatalog(adminInWarehouseDao);
    }

    /**
     * Determines whether the DAO already set or not.
     * @return true if the DAO already set; otherwise false.
     */
    public static boolean isDaoSet() {
        return adminInWarehouseDao != null;
    }

    /**
     * Sets the database connector.
     * @param dao Data Access Object of inventory.
     */
    public static void setAdminInWarehouseDao(AdminInWarehouseDao dao) {
        adminInWarehouseDao = dao;
    }

    /**
     * Returns param catalog using in this inventory.
     * @return param catalog using in this inventory.
     */
    public AdminInWarehouseCatalog getAdminInWarehouseCatalog() {
        return adminInWarehouseCatalog;
    }

    /**
     * Returns the instance of this singleton class.
     * @return instance of this class.
     * @throws NoDaoSetException if DAO was not set.
     */
    public static AdminInWarehouseService getInstance() throws NoDaoSetException {
        if (instance == null)
            instance = new AdminInWarehouseService();
        return instance;
    }
}
