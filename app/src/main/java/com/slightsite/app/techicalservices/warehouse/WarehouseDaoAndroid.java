package com.slightsite.app.techicalservices.warehouse;

import android.content.ContentValues;
import android.util.Log;

import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;

import java.util.ArrayList;
import java.util.List;

public class WarehouseDaoAndroid implements WarehouseDao {
    private Database database;

    /**
     * Constructs CustomerDaoAndroid.
     * @warehouse database database for use in CustomerDaoAndroid.
     */
    public WarehouseDaoAndroid(Database database) {
        this.database = database;
    }

    @Override
    public int addWarehouse(Warehouses warehouse) {
        ContentValues content = new ContentValues();
        content.put("warehouse_id", warehouse.getWarehouseId());
        content.put("title", warehouse.getTitle());
        content.put("address", warehouse.getAddress());
        content.put("phone", warehouse.getPhone());
        content.put("status", warehouse.getStatus());
        if (warehouse.getConfigs() != null) {
            content.put("configs", warehouse.getConfigs());
        }

        int id = database.insert(DatabaseContents.TABLE_WAREHOUSES.toString(), content);

        return id;
    }

    /**
     * Converts list of object to list of warehouse.
     * @warehouse objectList list of object.
     * @return list of warehouse.
     */
    private List<Warehouses> toWarehouseList(List<Object> objectList) {
        List<Warehouses> list = new ArrayList<Warehouses>();
        for (Object object: objectList) {
            ContentValues content = (ContentValues) object;
            Warehouses wh = new Warehouses(
                    content.getAsInteger("_id"),
                    content.getAsInteger("warehouse_id"),
                    content.getAsString("title"),
                    content.getAsString("address"),
                    content.getAsString("phone"),
                    content.getAsInteger("status"));
            wh.setServerProductData(content.getAsString("server_product_data"));
            wh.setDateProductRequest(content.getAsString("date_product_request"));
            wh.setConfigs(content.getAsString("configs"));
            list.add(wh);
        }
        return list;
    }

    @Override
    public List<Warehouses> getAllWarehouses() {
        return getAllWarehouses(" WHERE 1");
    }

    /**
     * Returns list of all warehouses.
     * @warehouse condition specific condition for getAllWarehouse.
     * @return list of all warehouses.
     */
    private List<Warehouses> getAllWarehouses(String condition) {
        String queryString = "SELECT * FROM " + DatabaseContents.TABLE_WAREHOUSES.toString() + condition + " ORDER BY warehouse_id";
        List<Warehouses> list = toWarehouseList(database.select(queryString));
        return list;
    }

    /**
     * Returns warehouses.
     * @warehouse reference reference val.
     * @return list of warehouses.
     */
    private List<Warehouses> getWarehouseBy(String reference, String val) {
        String condition = " WHERE " + reference + " = '" + val + "' ;";
        return getAllWarehouses(condition);
    }

    @Override
    public Warehouses getWarehouseByTitle(String title) {
        List<Warehouses> list = getWarehouseBy("title", title);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    @Override
    public Warehouses getWarehouseById(int id) {
        return getWarehouseBy("_id", id+"").get(0);
    }

    @Override
    public Warehouses getWarehouseByWarehouseId(int warehouse_id) {
        List<Warehouses> whs = getWarehouseBy("warehouse_id", warehouse_id+"");
        if (whs.size() == 0) {
            return null;
        }
        return whs.get(0);
    }

    @Override
    public boolean editWarehouse(Warehouses warehouse) {
        ContentValues content = new ContentValues();
        content.put("_id", warehouse.getId());
        content.put("warehouse_id", warehouse.getWarehouseId());
        content.put("title", warehouse.getTitle());
        content.put("address", warehouse.getAddress());
        content.put("phone", warehouse.getPhone());
        content.put("status", warehouse.getStatus());
        if (warehouse.getServerProductData() != null) {
            content.put("server_product_data", warehouse.getServerProductData());
            content.put("date_product_request", DateTimeStrategy.getCurrentTime());
        }
        if (warehouse.getConfigs() != null) {
            content.put("configs", warehouse.getConfigs());
        }

        return database.update(DatabaseContents.TABLE_WAREHOUSES.toString(), content);
    }

    @Override
    public List<Warehouses> searchWarehouse(String search) {
        String condition = " WHERE title LIKE '%" + search + "%' OR status LIKE '%" + search + "%' ;";
        return getAllWarehouses(condition);
    }

    @Override
    public void clearWarehouseCatalog() {
        database.execute("DELETE FROM " + DatabaseContents.TABLE_WAREHOUSES);
    }

    @Override
    public void suspendWarehouse(Warehouses warehouse) {
        ContentValues content = new ContentValues();
        content.put("_id", warehouse.getId());
        content.put("title", warehouse.getTitle());
        content.put("address", warehouse.getAddress());
        content.put("status", 0);

        database.update(DatabaseContents.TABLE_WAREHOUSES.toString(), content);
    }

    @Override
    public List<Warehouses> getListActiveWarehouses() {
        List<Warehouses> list = getWarehouseBy("status", "1");
        return list;
    }
}
