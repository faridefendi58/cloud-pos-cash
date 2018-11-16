package com.slightsite.app.domain;

import android.content.ContentValues;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;

public class ProfileController {
    private static Database database;
    private static ProfileController instance;

    private ProfileController() {

    }

    public static ProfileController getInstance() {
        if (instance == null)
            instance = new ProfileController();

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
    public static void buildDatabase() {
        database.execute("DROP TABLE " + DatabaseContents.TABLE_ADMIN);
        database.execute("CREATE TABLE " + DatabaseContents.TABLE_ADMIN + "("

                + "_id INTEGER PRIMARY KEY,"
                + "name TEXT(100),"
                + "email TEXT(32),"
                + "password TEXT(256),"
                + "phone TEXT(32),"
                + "status INTEGER DEFAULT 1,"
                + "is_super_admin INTEGER DEFAULT 0,"
                + "date_added DATETIME"
                + ");");

        Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_ADMIN + " Successfully.");
    }


    public ContentValues getDataByEmail(String email) {
        String queryString = "SELECT * FROM " + DatabaseContents.TABLE_ADMIN + " WHERE email = '"+ email +"'";
        List<Object> contents = database.select(queryString);

        if (contents.isEmpty()) {
            return null;
        }

        ContentValues content = (ContentValues) contents.get(0);
        return content;
    }

    public Object getAdmins() {
        List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.TABLE_ADMIN);

        return contents;
    }

    public int register(ContentValues content) {
        int id = database.insert(DatabaseContents.TABLE_ADMIN.toString(), content);

        return id;
    }

    public boolean update(ContentValues content) {
        boolean update = database.update(DatabaseContents.TABLE_ADMIN.toString(), content);

        return update;
    }
}
