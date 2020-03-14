package com.slightsite.app.domain.warehouse;

import com.slightsite.app.techicalservices.warehouse.WarehouseDao;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean addWarehouse2(Warehouses warehouse) {
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

    public Map<String, List<Warehouses>> getAllGroupedWarehouses(Boolean all_status) {
        List<Warehouses> whs = warehouseDao.getAllWarehouses();
        Map<String, List<Warehouses>> wh_groups = new HashMap<String, List<Warehouses>>();
        if (whs.size() > 0) {
            for (int m = 0; m < whs.size(); m++) {
                String configs = whs.get(m).getConfigs();
                if (configs != null) {
                    try {
                        JSONObject obj = new JSONObject(configs);
                        String group_name = obj.getString("category");
                        if (!wh_groups.containsKey(group_name)) {
                            List<Warehouses> items = new ArrayList<Warehouses>();
                            items.add(whs.get(m));
                            wh_groups.put(group_name, items);
                        } else {
                            List<Warehouses> items = wh_groups.get(group_name);
                            items.add(whs.get(m));
                            wh_groups.put(group_name, items);
                        }
                    } catch (Exception e){}
                }
            }
        }
        return wh_groups;
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
