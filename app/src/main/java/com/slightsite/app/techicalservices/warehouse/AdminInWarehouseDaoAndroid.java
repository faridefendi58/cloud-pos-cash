package com.slightsite.app.techicalservices.warehouse;

import android.content.ContentValues;
import android.util.Log;

import com.slightsite.app.domain.warehouse.AdminInWarehouse;
import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;

import java.util.ArrayList;
import java.util.List;

public class AdminInWarehouseDaoAndroid implements AdminInWarehouseDao {

    private Database database;

    public AdminInWarehouseDaoAndroid(Database database) {
        this.database = database;
    }

    @Override
    public int addAdminInWarehouse(AdminInWarehouse warehouse) {
        ContentValues content = new ContentValues();
        content.put("warehouse_id", warehouse.getWarehouseId());
        content.put("admin_id", warehouse.getAdminId());
        content.put("role_id", warehouse.getRoleId());
        content.put("status", warehouse.getStatus());

        int id = database.insert(DatabaseContents.TABLE_ADMIN_IN_WAREHOUSE.toString(), content);

        return id;
    }

    @Override
    public boolean editAdminInWarehouse(AdminInWarehouse dt) {
        ContentValues content = new ContentValues();
        content.put("_id", dt.getId());
        content.put("admin_id", dt.getAdminId());
        content.put("warehouse_id", dt.getWarehouseId());
        content.put("role_id", dt.getRoleId());
        content.put("status", dt.getStatus());

        return database.update(DatabaseContents.TABLE_ADMIN_IN_WAREHOUSE.toString(), content);
    }

    private List<AdminInWarehouse> toAdminInWarehouseList(List<Object> objectList) {
        List<AdminInWarehouse> list = new ArrayList<AdminInWarehouse>();
        for (Object object: objectList) {
            ContentValues content = (ContentValues) object;
            AdminInWarehouse adminInWarehouse = new AdminInWarehouse(
                    content.getAsInteger("admin_id"),
                    content.getAsInteger("warehouse_id"),
                    content.getAsInteger("status"));
            adminInWarehouse.setWarehouseName(content.getAsString("title"));
            if (content.containsKey("role_id")) {
                adminInWarehouse.setRoleId(content.getAsInteger("role_id"));
            }
            list.add(adminInWarehouse);
        }
        return list;
    }

    @Override
    public List<AdminInWarehouse> getAllAdminInWarehouse() {
        return getAllAdminInWarehouse(" WHERE 1");
    }

    private List<AdminInWarehouse> getAllAdminInWarehouse(String condition) {
        String queryString = "SELECT t.*, wh.title FROM " + DatabaseContents.TABLE_ADMIN_IN_WAREHOUSE.toString() +
                " t LEFT JOIN "+ DatabaseContents.TABLE_WAREHOUSES.toString() +" wh ON t.warehouse_id = wh.warehouse_id " + condition + " ORDER BY admin_id";
        List<AdminInWarehouse> list = toAdminInWarehouseList(database.select(queryString));
        return list;
    }

    private List<AdminInWarehouse> getAdminInWarehouseBy(String reference, String val) {
        String condition = " WHERE " + reference + " = '" + val + "' ;";
        return getAllAdminInWarehouse(condition);
    }

    @Override
    public AdminInWarehouse getDataById(int id) {
        return getAdminInWarehouseBy("_id", id+"").get(0);
    }

    @Override
    public AdminInWarehouse getDataByWarehouseId(int warehouse_id) {
        List<AdminInWarehouse> datas = getAdminInWarehouseBy("warehouse_id", warehouse_id+"");
        if (datas.size() == 0) {
            return null;
        }
        return datas.get(0);
    }

    @Override
    public List<AdminInWarehouse> getDataByAdminId(int admin_id) {
        //List<AdminInWarehouse> datas = getAdminInWarehouseBy("admin_id", admin_id+"");
        List<AdminInWarehouse> datas = getAllAdminInWarehouse();
        if (datas.size() == 0) {
            return null;
        }
        return datas;
    }

    @Override
    public AdminInWarehouse getDataByAdminAndWH(int admin_id, int warehouse_id) {
        String condition = " WHERE admin_id ='"+ admin_id +"' AND warehouse_id ='"+ warehouse_id +"';";
        String queryString = "SELECT * FROM " + DatabaseContents.TABLE_ADMIN_IN_WAREHOUSE.toString() + condition + " ORDER BY admin_id";
        List<AdminInWarehouse> datas = toAdminInWarehouseList(database.select(queryString));
        if (datas.size() == 0) {
            return null;
        }
        return datas.get(0);
    }

    @Override
    public List<AdminInWarehouse> searchAdminInWarehouse(String search) {
        String condition = " WHERE title LIKE '%" + search + "%' OR status LIKE '%" + search + "%' ;";
        return getAllAdminInWarehouse(condition);
    }

    @Override
    public void clearAdminInWarehouseCatalog() {
        database.execute("DELETE FROM " + DatabaseContents.TABLE_ADMIN_IN_WAREHOUSE);
    }

    @Override
    public void suspendAdminInWarehouse(AdminInWarehouse warehouse) {
        ContentValues content = new ContentValues();
        content.put("_id", warehouse.getId());
        content.put("status", 0);

        database.update(DatabaseContents.TABLE_ADMIN_IN_WAREHOUSE.toString(), content);
    }
}
