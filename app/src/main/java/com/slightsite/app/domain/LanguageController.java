package com.slightsite.app.domain;

import java.util.List;

import android.content.ContentValues;
import android.util.Log;

import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;

/**
 * Saves and loads language preference from database.
 *
 *
 */
public class LanguageController {
	
	private static final String DEFAULT_LANGUAGE = "en";
	private static Database database;
	private static LanguageController instance;
	
	private LanguageController() {
		
	}
	
	public static LanguageController getInstance() {
		if (instance == null)
			instance = new LanguageController();
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
	public void setLanguage(String localeString) {
		database.execute("UPDATE " + DatabaseContents.LANGUAGE + " SET language = '" + localeString + "'");
		//database.execute("DELETE FROM " + DatabaseContents.TABLE_SALE_LINEITEM);
		//database.execute("DELETE FROM " + DatabaseContents.TABLE_SALE);
		/*database.execute("CREATE TABLE " + DatabaseContents.TABLE_ADMIN + "("

				+ "_id INTEGER PRIMARY KEY,"
				+ "name TEXT(100),"
				+ "username TEXT(32),"
				+ "password TEXT(256),"
				+ "email TEXT(32),"
				+ "phone TEXT(32),"
				+ "status INTEGER DEFAULT 1,"
				+ "is_super_admin INTEGER DEFAULT 0,"
				+ "date_added DATETIME"
				+ ");");

		Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_ADMIN + " Successfully.");*/
	}
	
	/**
	 * Returns current language. 
	 * @return current language.
	 */
	public String getLanguage() {
		List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.LANGUAGE);

		if (contents.isEmpty()) {
			ContentValues defualtLang = new ContentValues();
			defualtLang.put("language", DEFAULT_LANGUAGE);
			database.insert( DatabaseContents.LANGUAGE.toString(), defualtLang);
	
			return DEFAULT_LANGUAGE;
		}

		ContentValues content = (ContentValues) contents.get(0);
		return content.getAsString("language");	
	}

	public Object getLanguages() {
		List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.LANGUAGE);

		return contents;
	}
}
