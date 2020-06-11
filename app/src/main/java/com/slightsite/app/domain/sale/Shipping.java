package com.slightsite.app.domain.sale;

import com.slightsite.app.domain.AppController;
import com.slightsite.app.techicalservices.Tools;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Shipping implements Serializable {
    private int id;
    private int method;
    private int sale_id;
    private String date;
    private String name;
    private String phone;
    private String address;
    private String invoice_number = null;
    private int warehouse_id;
    private String warehouse_name;
    private String date_added;
    private String configs;
    private String pickup_date;
    private Boolean use_customer_data = false;

    public static final int UNDEFINED = -1;

    public Shipping(int method, String date, String address, int warehouse_id) {
        this(UNDEFINED, method, date, address, warehouse_id);
    }

    public Shipping(int id, int method, String date, String address, int warehouse_id) {
        this.id = id;
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
        map.put("sale_id", sale_id+"");
        map.put("method", method+"");
        try {
            String method_name = AppController.getPaymentMethod(method);
            map.put("method_name", method_name);
        } catch (Exception e){}
        map.put("date", date);
        map.put("address", address);
        map.put("warehouse_id", warehouse_id+"");
        map.put("warehouse_name", warehouse_name);
        map.put("date_added", date_added);
        map.put("pickup_date", pickup_date);
        map.put("configs", configs);
        map.put("recipient_name", name);
        map.put("recipient_phone", phone);
        if (invoice_number != null) {
            map.put("invoice_number", invoice_number); //gosend, gofood, or grabfood inv number
        }

        return map;

    }

    public int getId() {
        return id;
    }

    public void setSaleId(int sale_id) {
        this.sale_id = sale_id;
    }
    public int getSaleId() {
        return sale_id;
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

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getPhone() {
        return phone;
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

    public void setDateAdded(String date) {
        this.date_added = date;
    }
    public String getDateAdded() {
        return date_added;
    }

    public void setConfigs(String configs) {
        this.configs = configs;
    }
    public String getConfigs() {
        return configs;
    }

    public void setPickupDate(String pickup_date) {
        this.pickup_date = pickup_date;
    }
    public String getPickupDate() {
        return pickup_date;
    }

    public void setUseCustomerData(Boolean _use_cust_data) {
        this.use_customer_data = _use_cust_data;
    }
    public Boolean getUseCustomerData() {
        return use_customer_data;
    }

    public void setInvoiceNumber(String inv) {
        this.invoice_number = inv;
    }
    public String getInvoiceNumber() {
        return invoice_number;
    }
}
