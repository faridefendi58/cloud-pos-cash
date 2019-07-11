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
	
	private static final String DEFAULT_LANGUAGE = "id";
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
