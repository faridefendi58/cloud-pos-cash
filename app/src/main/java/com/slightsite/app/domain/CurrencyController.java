package com.slightsite.app.domain;

import android.content.ContentValues;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;

public class CurrencyController {
    private static final String DEFAULT_CURRENCY = "idr";
    private static Database database;
    private static CurrencyController instance;

    private CurrencyController() {

    }

    public static CurrencyController getInstance() {
        if (instance == null)
            instance = new CurrencyController();

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
     * Sets language for use in application.
     * @param localeString local string of country.
     */
    public static void setCurrency(String localeString) {
        database.execute("UPDATE " + DatabaseContents.CURRENCY + " SET currency = '" + localeString + "'");
    }

    /**
     * Returns current language.
     * @return current language.
     */
    public String getCurrency() {
        List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.CURRENCY);

        /*if (contents.isEmpty()) {
            ContentValues defaultCurrency = new ContentValues();
            defaultCurrency.put("currency", DEFAULT_CURRENCY);
            database.insert( DatabaseContents.CURRENCY.toString(), defaultCurrency);

            return DEFAULT_CURRENCY;
        }

        ContentValues content = (ContentValues) contents.get(0);
        return content.getAsString("currency");*/
        return DEFAULT_CURRENCY;
    }

    public Object getCurrencies() {
        List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.CURRENCY);

        return contents;
    }

    public String moneyFormat(double numb)
    {
        String format = "#,###";

        NumberFormat formatter = new DecimalFormat(format);

        if (this.getCurrency().equals( DEFAULT_CURRENCY )) {
            String str = formatter.format(numb).replaceAll(",",".");
            return str;
        }

        return formatter.format(numb);
    }
}
