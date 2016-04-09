package com.sandstormweb.droneone.statics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sandstormweb.droneone.datamodels.KeyValue;

public class DBHelper extends SQLiteOpenHelper
{
    private SQLiteDatabase database;

//    statics
    public static final String TABLE_INFO = "info";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    public DBHelper(Context context)
    {
        super(context, "DATABASE", null, 1);
        try {
            this.onCreate(context.openOrCreateDatabase("DATABASE", Context.MODE_PRIVATE, null));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            this.database = db;

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_INFO + "(key TEXT, value TEXT)");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    /**
     *
     * @param keyValue
     * @return number of rows affected
     */
    public int updateKeyValue(String table, KeyValue keyValue)
    {
        try{
            ContentValues cv = new ContentValues();
            cv.put("key", keyValue.getKey());
            cv.put("value", keyValue.getValue());

            return this.database.update(table, cv, "key = \""+keyValue.getKey()+"\"", null);
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public boolean isKeyExists(String table, KeyValue keyValue)
    {
        try{
            Cursor c = this.database.rawQuery("SELECT key FROM "+table+" WHERE key=\""+keyValue.getKey()+"\"", null);

            if(c.moveToNext())return true;
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void insertKeyValue(String table, KeyValue keyValue)
    {
        try{
            ContentValues cv = new ContentValues();
            cv.put("key", keyValue.getKey());
            cv.put("value", keyValue.getValue());

            this.database.insert(table, null, cv);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param table
     * @param keyValue
     * @return true if insert performed, false if update performed
     */
    public boolean insertOrUpdateKeyValue(String table, KeyValue keyValue)
    {
        try{
            if(isKeyExists(table, keyValue)){
                updateKeyValue(table, keyValue);
                return false;
            }else{
                insertKeyValue(table, keyValue);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public KeyValue getKeyValue(String table, String key)
    {
        try{
            Cursor c = this.database.rawQuery("SELECT value FROM "+table+" WHERE key = \""+key+"\"", null);
            if(c.moveToNext()){
                return new KeyValue(key, c.getString(0));
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void close()
    {
        try{
            this.database.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
