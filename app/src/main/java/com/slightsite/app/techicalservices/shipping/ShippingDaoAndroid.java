package com.slightsite.app.techicalservices.shipping;

import android.content.ContentValues;

import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;

import org.json.JSONArray;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.List;

public class ShippingDaoAndroid implements ShippingDao {

    private Database database;

    /**
     * Constructs CustomerDaoAndroid.
     * @shipping database database for use in CustomerDaoAndroid.
     */
    public ShippingDaoAndroid(Database database) {
        this.database = database;
    }

    @Override
    public int addShipping(Shipping shipping) {
        ContentValues content = new ContentValues();
        content.put("sale_id", shipping.getSaleId());
        content.put("method", shipping.getMethod());
        content.put("warehouse_id", shipping.getWarehouseId());
        content.put("pickup_date", shipping.getDate());
        content.put("address", shipping.getAddress());
        content.put("notes", "");
        content.put("configs", shipping.toMap().toString());
        /*JSONArray json = new JSONArray();
        String configs = json.put(shipping.toMap()).toString();
        content.put("configs", configs);*/
        content.put("date_added", shipping.getDateAdded());

        int id = database.insert(DatabaseContents.TABLE_SALE_SHIPPING.toString(), content);

        return id;
    }

    /**
     * Converts list of object to list of shipping.
     * @shipping objectList list of object.
     * @return list of shipping.
     */
    private List<Shipping> toShippingList(List<Object> objectList) {
        List<Shipping> list = new ArrayList<Shipping>();
        for (Object object: objectList) {
            ContentValues content = (ContentValues) object;
            Shipping _shipping = new Shipping(
                    content.getAsInteger("_id"),
                    content.getAsString("pickup_date"),
                    content.getAsString("notes"),
                    content.getAsInteger("warehouse_id")
                    );
            _shipping.setSaleId(content.getAsInteger("sale_id"));
            _shipping.setDateAdded(content.getAsString("date_added"));
            list.add(_shipping);
        }
        return list;
    }

    @Override
    public List<Shipping> getAllShipping() {
        return getAllShipping(" WHERE 1");
    }

    /**
     * Returns list of all shippings.
     * @shipping condition specific condition for getAllParam.
     * @return list of all shippings.
     */
    private List<Shipping> getAllShipping(String condition) {
        String queryString = "SELECT * FROM " + DatabaseContents.TABLE_SALE_SHIPPING.toString() + condition + " ORDER BY sale_id";
        List<Shipping> list = toShippingList(database.select(queryString));
        return list;
    }

    /**
     * Returns shippings.
     * @shipping reference reference value.
     * @shipping value value for search.
     * @return list of shippings.
     */
    private List<Shipping> getShippingBy(String reference, String value) {
        String condition = " WHERE " + reference + " = '" + value + "' ;";
        return getAllShipping(condition);
    }

    @Override
    public  Shipping getShippingBySaleId(int sale_id) {
        List<Shipping> list = getShippingBy("sale_id", sale_id +"");
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    @Override
    public Shipping getShippingById(int id) {
        return getShippingBy("_id", id+"").get(0);
    }

    @Override
    public boolean editShipping(Shipping shipping) {
        ContentValues content = new ContentValues();
        content.put("_id", shipping.getId());
        content.put("sale_id", shipping.getSaleId());
        content.put("method", shipping.getMethod());

        return database.update(DatabaseContents.TABLE_SALE_SHIPPING.toString(), content);
    }

    @Override
    public List<Shipping> searchShipping(String search) {
        String condition = " WHERE method LIKE '%" + search + "%' ;";
        return getAllShipping(condition);
    }

    @Override
    public void clearShippingCatalog() {
        database.execute("DELETE FROM " + DatabaseContents.TABLE_SALE_SHIPPING);
    }

    @Override
    public void suspendShipping(Shipping shipping) {
        ContentValues content = new ContentValues();
        content.put("_id", shipping.getId());
        content.put("sale_id", shipping.getSaleId());
        content.put("method", shipping.getMethod());

        database.update(DatabaseContents.TABLE_SALE_SHIPPING.toString(), content);
    }

    @Override
    public void removeShipping(int id) {
        database.delete(DatabaseContents.TABLE_SALE_SHIPPING.toString(), id);
    }
}

