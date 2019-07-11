package com.slightsite.app.domain.warehouse;

import com.slightsite.app.techicalservices.warehouse.AdminInWarehouseDao;

import java.util.List;

public class AdminInWarehouseCatalog {
    private AdminInWarehouseDao adminInWarehouseDao;

    /**
     * Constructs Data Access Object of inventory in WarehouseCatalog.
     * @warehouse adminInWarehouseDao DAO of warehouse.
     */
    public AdminInWarehouseCatalog(AdminInWarehouseDao adminInWarehouseDao) {
        this.adminInWarehouseDao = adminInWarehouseDao;
    }

    /**
     *
     * @warehouse title
     * @warehouse address
     * @warehouse phone
     * @warehouse status
     * @return
     */
    public boolean addAdminInWarehouse(int admin_id, int warehouse_id, int status) {
        AdminInWarehouse dt = new AdminInWarehouse(admin_id, warehouse_id, status);
        int id = adminInWarehouseDao.addAdminInWarehouse(dt);
        return id != -1;
    }

    public boolean editAdminInWarehouse(AdminInWarehouse dt) {
        boolean respond = adminInWarehouseDao.editAdminInWarehouse(dt);
        return respond;
    }

    public AdminInWarehouse getDataById(int id) {
        return adminInWarehouseDao.getDataById(id);
    }

    public List<AdminInWarehouse> getDataByAdminId(int admin_id) {
        return adminInWarehouseDao.getDataByAdminId(admin_id);
    }

    public AdminInWarehouse getDataByWarehouseId(int warehouse_id) {
        return adminInWarehouseDao.getDataByWarehouseId(warehouse_id);
    }

    public AdminInWarehouse getDataByAdminAndWH(int admin_id, int wh_id) {
        return adminInWarehouseDao.getDataByAdminAndWH(admin_id, wh_id);
    }

    public List<AdminInWarehouse> getAllData() {
        return adminInWarehouseDao.getAllAdminInWarehouse();
    }

    public List<AdminInWarehouse> searchData(String search) {
        return adminInWarehouseDao.searchAdminInWarehouse(search);
    }

    public void clearAdminInWarehouseCatalog() {
        adminInWarehouseDao.clearAdminInWarehouseCatalog();
    }

    public void suspendAdminInWarehouse(AdminInWarehouse dt) {
        adminInWarehouseDao.suspendAdminInWarehouse(dt);
    }
}
