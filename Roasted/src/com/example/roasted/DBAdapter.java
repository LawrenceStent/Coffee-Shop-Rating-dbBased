package com.example.roasted;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter 
{
	static final String KEY_SHOPID = "shopID";
	static final String KEY_NAME = "name";
	static final String KEY_ADDRESS = "address";
	static final String KEY_PHONE = "phone";
	static final String KEY_EMAIL = "email";
	static final String KEY_COFFEERATE = "coffeeRating";
	static final String KEY_FOODRATE = "foodRating";
	static final String TAG = "DBAdapter";
	
	static final String DATABASE_NAME = "myDB";
	
	static final String DATABASE_SHOP = "shopTable";
	
	static final int DATABASE_VERSION = 2;
	
	//create table statements
	//parent table Shop with Two 1-M table relationships
	static final String DATABASE_CREATESHOP = "create table shopTable(shopID integer primary key autoincrement,"
			+"name text not null, address text, phone text, email text, coffeeRating integer, foodRating integer);";
	
	final Context context;
	
	DatabaseHelper DBHelper;
	SQLiteDatabase db;
	
	public DBAdapter(Context ctx)
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context,DATABASE_NAME,null,DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			try
			{
				db.execSQL(DATABASE_CREATESHOP);
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
			db.execSQL("drop table if exists shopTable");
			
			onCreate(db);
		}
	}
	
	public DBAdapter open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	public void close()
	{
		DBHelper.close();
	}
	
	public long insertShop(String name, String address, String phone, String email)
	{
	    ContentValues initialValues = new ContentValues();
	    
	    initialValues.put(KEY_NAME, name);
	    initialValues.put(KEY_ADDRESS, address);
	    initialValues.put(KEY_PHONE, phone);
	    initialValues.put(KEY_EMAIL, email);
	    
	    return db.insert(DATABASE_SHOP, null, initialValues);
	}
	
	
	public Cursor getShopNames()
    {
        return db.query(DATABASE_SHOP, new String[] {KEY_NAME}, null, null, null, null, null);
    }
	
	 //---deletes a particular contact---
    public boolean deleteContact(int rowid)
    {
        return db.delete(DATABASE_SHOP, KEY_SHOPID + "=" + rowid, null) > 0;
    }
    
    public Cursor getRowID(String name)
    {
    	return db.query(DATABASE_SHOP, new String[] {KEY_SHOPID}, KEY_NAME + "=" +"'"+ name+"'" , null, null, null, null);
    	
    	
    }
    
    public boolean updateCoffee(long rowId, float rate)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_COFFEERATE, rate);
        return db.update(DATABASE_SHOP, args, KEY_SHOPID + "=" + rowId, null) > 0;
    }
    
    public boolean updateFood(long rowId, float rate)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_FOODRATE, rate);
        return db.update(DATABASE_SHOP, args, KEY_SHOPID + "=" + rowId, null) > 0;
    }
    
    public Cursor getContact(long rowId) throws SQLException
    {
        Cursor mCursor =
                db.query(true, DATABASE_SHOP, new String[] {
                KEY_NAME, KEY_ADDRESS, KEY_PHONE, KEY_EMAIL, KEY_COFFEERATE, KEY_FOODRATE}, KEY_SHOPID + "=" + rowId, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
	
    public Cursor getAllContacts()
    {
        return db.query(DATABASE_SHOP, new String[] {KEY_NAME,
                KEY_COFFEERATE, KEY_FOODRATE}, null, null, null, null, null);
    }
	
}
