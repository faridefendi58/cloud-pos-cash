package com.slightsite.app.domain;

import com.slightsite.app.techicalservices.Database;

public class MasterDataController {
    private static Database database;
    private static MasterDataController instance;

    private MasterDataController() {}

    public static MasterDataController getInstance() {
        if (instance == null)
            instance = new MasterDataController();

        return instance;
    }

    /**
     * Sets database for use in this class.
     * @param db database.
     */
    public static void setDatabase(Database db) {
        database = db;
    }

    public static void buildProduct() {

    }
}
