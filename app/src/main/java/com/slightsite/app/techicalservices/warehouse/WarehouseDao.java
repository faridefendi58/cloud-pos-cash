package com.slightsite.app.techicalservices.warehouse;

import com.slightsite.app.domain.warehouse.Warehouses;

import java.util.List;

public interface WarehouseDao {

    /**
     * Add customer data
     * @warehouse warehouse
     * @return
     */
    int addWarehouse(Warehouses warehouse);

    /**
     * Edit customer
     * @warehouse warehouse
     * @return
     */
    boolean editWarehouse(Warehouses warehouse);

    /**
     * Returns warehouse finds by id.
     * @warehouse id
     * @return
     */
    Warehouses getWarehouseById(int id);

    Warehouses getWarehouseByWarehouseId(int warehouse_id);

    Warehouses getWarehouseByTitle(String title);

    /**
     * Returns list of all warehouse
     * @return list of all warehouse
     */
    List<Warehouses> getAllWarehouses();

    /**
     * @warehouse search
     * @return
     */
    List<Warehouses> searchWarehouse(String search);

    void clearWarehouseCatalog();

    void suspendWarehouse(Warehouses warehouse);
}
