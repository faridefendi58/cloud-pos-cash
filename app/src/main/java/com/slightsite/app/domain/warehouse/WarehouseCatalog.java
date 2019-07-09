package com.slightsite.app.domain.warehouse;

import com.slightsite.app.techicalservices.warehouse.WarehouseDao;

import java.util.List;

public class WarehouseCatalog {
    private WarehouseDao warehouseDao;

    /**
     * Constructs Data Access Object of inventory in WarehouseCatalog.
     * @warehouse warehouseDao DAO of warehouse.
     */
    public WarehouseCatalog(WarehouseDao warehouseDao) {
        this.warehouseDao = warehouseDao;
    }

    /**
     *
     * @warehouse title
     * @warehouse address
     * @warehouse phone
     * @warehouse status
     * @return
     */
    public boolean addWarehouse(int warehouse_id, String title, String address, String phone, int status) {
        Warehouses warehouse = new Warehouses(warehouse_id, title, address, phone, status);
        int id = warehouseDao.addWarehouse(warehouse);
        return id != -1;
    }

    /**
     * Edit warehouse
     * @warehouse warehouse
     * @return
     */
    public boolean editWarehouse(Warehouses warehouse) {
        boolean respond = warehouseDao.editWarehouse(warehouse);
        return respond;
    }

    /**
     * Geting warehouse by title
     * @warehouse title
     * @return
     */
    public Warehouses getWarehouseByTitle(String title) {
        return warehouseDao.getWarehouseByTitle(title);
    }

    /**
     * Geting warehouse by id
     * @warehouse id
     * @return
     */
    public Warehouses getWarehouseById(int id) {
        return warehouseDao.getWarehouseById(id);
    }

    /**
     * Geting warehouse by warehouse_id
     * @warehouse warehouse_id
     * @return
     */
    public Warehouses getWarehouseByWarehouseId(int warehouse_id) {
        return warehouseDao.getWarehouseByWarehouseId(warehouse_id);
    }

    /**
     * Geting all warehouse
     * @return
     */
    public List<Warehouses> getAllWarehouses() {
        return warehouseDao.getAllWarehouses();
    }

    /**
     * Searching for some warehouse
     * @warehouse search
     * @return
     */
    public List<Warehouses> searchWarehouse(String search) {
        return warehouseDao.searchWarehouse(search);
    }

    /**
     * Clears WarehouseCatalog.
     */
    public void clearWarehouseCatalog() {
        warehouseDao.clearWarehouseCatalog();
    }

    /**
     * Suspend or dective warehouse
     * @warehouse warehouse
     */
    public void suspendWarehouse(Warehouses warehouse) {
        warehouseDao.suspendWarehouse(warehouse);
    }
}
