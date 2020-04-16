package com.slightsite.app.domain.warehouse;

import java.util.HashMap;
import java.util.Map;

public class AdminInWarehouse {
    private int id;
    private int admin_id;
    private int warehouse_id;
    private int role_id;
    private int status;
    private String warehouse_name;

    /**
     * Static address for UNDEFINED ID.
     */
    public static final int UNDEFINED_ID = -1;


    public AdminInWarehouse(int id, int admin_id, int warehouse_id, int status) {
        this.id = id;
        this.admin_id = admin_id;
        this.warehouse_id = warehouse_id;
        this.status = status;
    }

    public AdminInWarehouse(int admin_id, int warehouse_id, int status) {
        this(UNDEFINED_ID, admin_id, warehouse_id, status);
    }

    public void setWarehouseId(int warehouse_id) {
        this.warehouse_id = warehouse_id;
    }

    public void setAdminId(int admin_id) {
        this.admin_id = admin_id;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getWarehouseId() {
        return warehouse_id;
    }

    public void setWarehouseName(String warehouse_name) {
        this.warehouse_name = warehouse_name;
    }

    public String getWarehouseName() {
        return warehouse_name;
    }

    public int getAdminId() {
        return admin_id;
    }

    public int getStatus() {
        return status;
    }

    public void setRoleId(int role_id) {
        this.role_id = role_id;
    }

    public int getRoleId() {
        return role_id;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id + "");
        map.put("warehouse_id", warehouse_id + "");
        map.put("warehouse_name", warehouse_name + "");
        map.put("admin_id", admin_id + "");
        map.put("role_id", role_id + "");
        map.put("status", status + "");

        return map;

    }
}
