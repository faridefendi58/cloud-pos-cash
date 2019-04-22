package com.slightsite.app.techicalservices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Real database connector, provides all CRUD operation.
 * database tables are created here.
 *
 *
 */
public class AndroidDatabase extends SQLiteOpenHelper implements Database {

	private static final int DATABASE_VERSION = 3;

	/**
	 * Constructs a new AndroidDatabase.
	 * @param context The current stage of the application.
	 */
	public AndroidDatabase(Context context) {
		super(context, DatabaseContents.DATABASE.toString(), null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		
		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_PRODUCT_CATALOG + "("
				
				+ "_id INTEGER PRIMARY KEY,"
				+ "name TEXT(100),"
				+ "barcode TEXT(100),"
				+ "unit_price DOUBLE,"
				+ "status TEXT(10)"
				
				+ ");");
		Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_PRODUCT_CATALOG + " Successfully.");
		
		database.execSQL("CREATE TABLE "+ DatabaseContents.TABLE_STOCK + "(" 
				
				+ "_id INTEGER PRIMARY KEY,"
				+ "product_id INTEGER,"
				+ "quantity INTEGER,"
				+ "cost DOUBLE,"
				+ "date_added DATETIME"
				
				+ ");");
		Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_STOCK + " Successfully.");

		database.execSQL("CREATE TABLE "+ DatabaseContents.TABLE_PRODUCT_DISCOUNT + "("

				+ "_id INTEGER PRIMARY KEY,"
				+ "product_id INTEGER,"
				+ "quantity INTEGER,"
				+ "quantity_max INTEGER,"
				+ "cost DOUBLE,"
				+ "date_added DATETIME"

				+ ");");
		Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_PRODUCT_DISCOUNT + " Successfully.");
		
		database.execSQL("CREATE TABLE "+ DatabaseContents.TABLE_SALE + "("
				
				+ "_id INTEGER PRIMARY KEY,"
				+ "status TEXT(40),"
				+ "payment TEXT(50),"
				+ "total DOUBLE,"
				+ "start_time DATETIME,"
				+ "end_time DATETIME,"
				+ "customer_id INTEGER,"
				+ "orders INTEGER,"
				+ "pushed INTEGER DEFAULT 0"
				+ ");");
		Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_SALE + " Successfully.");
		
		database.execSQL("CREATE TABLE "+ DatabaseContents.TABLE_SALE_LINEITEM + "("
				
				+ "_id INTEGER PRIMARY KEY,"
				+ "sale_id INTEGER,"
				+ "product_id INTEGER,"
				+ "quantity INTEGER,"
				+ "unit_price DOUBLE"
				
				+ ");");
		Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_SALE_LINEITEM + " Successfully.");


		// this _id is product_id but for update method, it is easier to use name _id
		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_STOCK_SUM + "("
				
				+ "_id INTEGER PRIMARY KEY,"
				+ "quantity INTEGER"
				
				+ ");");
		Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_STOCK_SUM + " Successfully.");
		
		database.execSQL("CREATE TABLE " + DatabaseContents.LANGUAGE + "("
				
				+ "_id INTEGER PRIMARY KEY,"
				+ "language TEXT(5)"
				
				+ ");");
		Log.d("CREATE DATABASE", "Create " + DatabaseContents.LANGUAGE + " Successfully.");

		database.execSQL("CREATE TABLE " + DatabaseContents.CURRENCY + "("

				+ "_id INTEGER PRIMARY KEY,"
				+ "currency TEXT(5)"

				+ ");");
		Log.d("CREATE DATABASE", "Create " + DatabaseContents.CURRENCY + " Successfully.");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_CUSTOMER + "("

				+ "_id INTEGER PRIMARY KEY,"
				+ "name TEXT(100),"
				+ "email TEXT(100),"
				+ "phone TEXT(20),"
				+ "address TEXT(256),"
				+ "status INTEGER,"
				+ "date_added DATETIME"

				+ ");");
		Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_CUSTOMER + " Successfully.");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_PARAMS + "("

				+ "_id INTEGER PRIMARY KEY,"
				+ "name TEXT(100),"
				+ "value TEXT(256),"
				+ "type TEXT(16),"
				+ "description TEXT(256),"
				+ "date_added DATETIME"

				+ ");");

		Calendar c = Calendar.getInstance();
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String datetime = dateformat.format(c.getTime());
		database.execSQL("INSERT INTO " + DatabaseContents.TABLE_PARAMS + " (" +
				"_id, name, value, type, description, date_added)\n" +
				"VALUES ('1', 'store_name', 'Ucok Durian', 'text', '', '"+ datetime +"');");
		Log.d("CREATE DATABASE", "Create " + DatabaseContents.TABLE_PARAMS + " Successfully.");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_ADMIN + "("

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

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_SALE_PAYMENT + "("

				+ "_id INTEGER PRIMARY KEY,"
				+ "sale_id INTEGER,"
				+ "payment_channel TEXT(128),"
				+ "amount DOUBLE,"
				+ "date_added DATETIME"
				+ ");");

		database.execSQL("CREATE TABLE " + DatabaseContents.TABLE_SALE_SHIPPING + "("

				+ "_id INTEGER PRIMARY KEY,"
				+ "sale_id INTEGER,"
				+ "method INTEGER DEFAULT 0,"
				+ "warehouse_id INTEGER DEFAULT 0,"
				+ "pickup_date DATETIME,"
				+ "address TEXT(256),"
				+ "notes TEXT(256),"
				+ "configs TEXT(256),"
				+ "date_added DATETIME"
				+ ");");
		
		Log.d("CREATE DATABASE", "Create Database Successfully.");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public List<Object> select(String queryString) {
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			List<Object> list = new ArrayList<Object>();
			Cursor cursor = database.rawQuery(queryString, null);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						ContentValues content = new ContentValues();
						String[] columnNames = cursor.getColumnNames();
						for (String columnName : columnNames) {
							content.put(columnName, cursor.getString(cursor
									.getColumnIndex(columnName)));
						}
						list.add(content);
					} while (cursor.moveToNext());
				}
			}
			cursor.close();
			database.close();
			return list;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int insert(String tableName, Object content) {
		try {
			SQLiteDatabase database = this.getWritableDatabase();

			int id = (int) database.insert(tableName, null,
					(ContentValues) content);

			database.close();
			return id;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

	}

	@Override
	public boolean update(String tableName, Object content) {
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			ContentValues cont = (ContentValues) content;
			// this array will always contains only one element. 
			String[] array = new String[]{cont.get("_id")+""};
			database.update(tableName, cont, " _id = ?", array);
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

    @Override
    public boolean delete(String tableName, int id) {
            try {
                    SQLiteDatabase database = this.getWritableDatabase();
                    database.delete(tableName, " _id = ?", new String[]{id+""});
                    return true;
                    
            } catch (Exception e) {
                    e.printStackTrace();
                    return false;
            }
    }

	@Override
	public boolean execute(String query) {
		try{
			SQLiteDatabase database = this.getWritableDatabase();
			database.execSQL(query);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

}
