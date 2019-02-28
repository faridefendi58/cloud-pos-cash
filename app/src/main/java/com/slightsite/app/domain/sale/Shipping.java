package com.slightsite.app.domain.sale;

import java.util.HashMap;
import java.util.Map;

public class Shipping {
    private int method;
    private String date;
    private String address;
    private int warehouse_id;
    private String warehouse_name;

    public static final int UNDEFINED = -1;

    public Shipping(int method, String date, String address, int warehouse_id) {
        this(UNDEFINED, method, date, address, warehouse_id);
    }

    public Shipping(int id, int method, String date, String address, int warehouse_id) {
        this.method = method;
        this.date = date;
        this.address = address;
        this.warehouse_id = warehouse_id;
    }

    /**
     * Returns the description of this LineItem in Map format.
     * @return the description of this LineItem in Map format.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("method", method+"");
        map.put("date", date);
        map.put("address", address);
        map.put("warehouse_id", warehouse_id+"");
        map.put("warehouse_name", warehouse_name);

        return map;

    }

    public void setMethod(int method) {
        this.method = method;
    }
    public int getMethod() {
        return method;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getDate() {
        return date;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getAddress() {
        return address;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouse_id = warehouseId;
    }
    public int getWarehouseId() {
        return warehouse_id;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouse_name = warehouseName;
    }
    public String getWarehouseName() {
        return warehouse_name;
    }
}
