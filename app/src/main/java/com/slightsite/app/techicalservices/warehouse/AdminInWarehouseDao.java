package com.slightsite.app.techicalservices.warehouse;

import com.slightsite.app.domain.warehouse.AdminInWarehouse;

import java.util.List;

public interface AdminInWarehouseDao {

    /**
     * Add customer data
     * @admin_in_warehouse admin_in_warehouse
     * @return
     */
    int addAdminInWarehouse(AdminInWarehouse admin_in_warehouse);

    /**
     * @admin_in_warehouse
     * @return
     */
    boolean editAdminInWarehouse(AdminInWarehouse admin_in_warehouse);

    AdminInWarehouse getDataById(int _id);

    List<AdminInWarehouse> getDataByAdminId(int admin_id);

    AdminInWarehouse getDataByWarehouseId(int warehouse_id);

    AdminInWarehouse getDataByAdminAndWH(int admin_id, int warehouse_id);

    /**
     * Returns list of all admin_in_warehouse
     * @return list of all admin_in_warehouse
     */
    List<AdminInWarehouse> getAllAdminInWarehouse();

    /**
     * @admin_in_warehouse search
     * @return
     */
    List<AdminInWarehouse> searchAdminInWarehouse(String search);

    void clearAdminInWarehouseCatalog();

    void suspendAdminInWarehouse(AdminInWarehouse admin_in_warehouse);
}
