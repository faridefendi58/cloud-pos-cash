package com.slightsite.app.techicalservices.params;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;

import com.slightsite.app.domain.params.Params;
import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;

/**
 * DAO used by android for parameter.
 *
 * @author Farid Efendi
 *
 */
public class ParamDaoAndroid implements ParamDao {

    private Database database;

    /**
     * Constructs CustomerDaoAndroid.
     * @param database database for use in CustomerDaoAndroid.
     */
    public ParamDaoAndroid(Database database) {
        this.database = database;
    }

    @Override
    public int addParam(Params param) {
        ContentValues content = new ContentValues();
        content.put("name", param.getName());
        content.put("value", param.getValue());
        content.put("type", param.getType());
        content.put("description", param.getDescription());

        int id = database.insert(DatabaseContents.TABLE_PARAMS.toString(), content);

        return id;
    }

    /**
     * Converts list of object to list of param.
     * @param objectList list of object.
     * @return list of param.
     */
    private List<Params> toParamList(List<Object> objectList) {
        List<Params> list = new ArrayList<Params>();
        for (Object object: objectList) {
            ContentValues content = (ContentValues) object;
            list.add(new Params(
                    content.getAsInteger("_id"),
                    content.getAsString("name"),
                    content.getAsString("value"),
                    content.getAsString("type"),
                    content.getAsString("description"))
            );
        }
        return list;
    }

    @Override
    public List<Params> getAllParams() {
        return getAllParams(" WHERE 1");
    }

    /**
     * Returns list of all params.
     * @param condition specific condition for getAllParam.
     * @return list of all params.
     */
    private List<Params> getAllParams(String condition) {
        String queryString = "SELECT * FROM " + DatabaseContents.TABLE_PARAMS.toString() + condition + " ORDER BY name";
        List<Params> list = toParamList(database.select(queryString));
        return list;
    }

    /**
     * Returns params.
     * @param reference reference value.
     * @param value value for search.
     * @return list of params.
     */
    private List<Params> getParamBy(String reference, String value) {
        String condition = " WHERE " + reference + " = '" + value + "' ;";
        return getAllParams(condition);
    }

    @Override
    public Params getParamByName(String name) {
        List<Params> list = getParamBy("name", name);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    @Override
    public Params getParamById(int id) {
        return getParamBy("_id", id+"").get(0);
    }

    @Override
    public boolean editParam(Params param) {
        ContentValues content = new ContentValues();
        content.put("_id", param.getId());
        content.put("name", param.getName());
        content.put("value", param.getValue());
        content.put("type", param.getType());
        content.put("description", param.getDescription());

        return database.update(DatabaseContents.TABLE_PARAMS.toString(), content);
    }

    @Override
    public List<Params> searchParam(String search) {
        String condition = " WHERE name LIKE '%" + search + "%' OR description LIKE '%" + search + "%' ;";
        return getAllParams(condition);
    }

    @Override
    public void clearParamCatalog() {
        database.execute("DELETE FROM " + DatabaseContents.TABLE_PARAMS);
    }

    @Override
    public void suspendParam(Params param) {
        ContentValues content = new ContentValues();
        content.put("_id", param.getId());
        content.put("name", param.getName());
        content.put("value", param.getValue());

        database.update(DatabaseContents.TABLE_PARAMS.toString(), content);
    }
}


