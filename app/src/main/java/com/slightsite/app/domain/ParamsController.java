package com.slightsite.app.domain;

import java.util.List;

import android.content.ContentValues;
import android.util.Log;

import com.slightsite.app.R;
import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;

/**
 * Saves and loads params preference from database.
 */
public class ParamsController {

    private static Database database;
    private static ParamsController instance;

    private ParamsController() {

    }

    public static ParamsController getInstance() {
        if (instance == null)
            instance = new ParamsController();

        return instance;
    }

    /**
     * Sets database for use in this class.
     * @param db database.
     */
    public static void setDatabase(Database db) {
        database = db;
    }

    /**
     * Rebuild the database for use in application.
     */
    public void buildDatabase() {
        //database.execute("DELETE FROM " + DatabaseContents.TABLE_PARAMS);
		//database.execute("DROP TABLE " + DatabaseContents.TABLE_PARAMS);
        database.execute("CREATE TABLE " + DatabaseContents.TABLE_PARAMS + "("

                + "_id INTEGER PRIMARY KEY,"
                + "name TEXT(100),"
                + "value TEXT(256),"
                + "type TEXT(16) default 'text',"
                + "description TEXT(256),"
                + "date_added DATETIME"

                + ");");

        Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_PARAMS + " Successfully.");
    }

    /**
     * Returns current language.
     * @return current language.
     */
    public String getParam(String key) {
        List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.TABLE_PARAMS);

        if (contents.isEmpty()) {
            ContentValues defualtLang = new ContentValues();
            database.insert( DatabaseContents.TABLE_PARAMS.toString(), defualtLang);

            return "text";
        }

        ContentValues content = (ContentValues) contents.get(0);
        return content.getAsString(key);
    }

    public Object getParams() {
        List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.TABLE_PARAMS);

        return contents;
    }
}

