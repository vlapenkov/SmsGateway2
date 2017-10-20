package com.yst.sms.smsgateway.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by user on 07.02.2017.
 * Служебный класс для работы с базой данных
 */
public class DbHelper extends SQLiteOpenHelper {


    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "messages.db";
    public static final String TABLE_NAME = "smsmessages";


    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PHONENUMBER = "phonenumber";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_ISSENT = "issent";
    public static final String COLUMN_DATEOFRECEPTION = "dateofreception";
    public static final String COLUMN_DATEOFDISPATCH = "dateofdispatch";


    private static final String SQL_CREATE_MESSAGES_TABLE =
            "create table " + TABLE_NAME + " (" +
                    COLUMN_ID + " integer primary key autoincrement ," +
                    COLUMN_PHONENUMBER + " text," +
                    COLUMN_MESSAGE + " text," +
                    COLUMN_DATEOFRECEPTION + " text," +
                    COLUMN_DATEOFDISPATCH + " text," +
                    COLUMN_ISSENT + " integer );";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

        try {

            db.execSQL(SQL_CREATE_MESSAGES_TABLE);

            //    initializeData(db);
        } catch (Exception e) {
            Log.e("db helper error", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAllTables(db);

        onCreate(db);
    }


    public void dropAllTables(SQLiteDatabase db) {
        db.execSQL("drop table if exists messages");


    }

    /*
    Добавляет запись в базу данных
     */
    public boolean addSmsMessage(String phoneNumber, String message) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_PHONENUMBER, phoneNumber);

            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
            Date curDate = new Date();
            cv.put(COLUMN_DATEOFRECEPTION, dateFormat.format(curDate));
            cv.put(COLUMN_DATEOFDISPATCH, "");
            cv.put(COLUMN_MESSAGE, message);
            cv.put(COLUMN_ISSENT, 0);
            db.insert(TABLE_NAME, null, cv);
        } catch (Exception e) {
            return false;
        }

        return true;

    }


    public void clearTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + tableName);

    }

    public void deleteWhenSent() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME +" where issent=1");

    }


    /*
    Получить все сообщения
     */
    public Cursor getAllMessages(String filter) {

        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String sql_select = " SELECT * FROM " + TABLE_NAME;
     //   return db.rawQuery("select * from " + ProductsContract.ProductsEntry.TABLE_NAME + " where name like '%" + filter + "%'" +" or _id like '%"+ filter + "%'" , null);
        if (!TextUtils.isEmpty(filter))
        sql_select = " SELECT * FROM " + TABLE_NAME+ " where phonenumber like '%"+filter+"%'";

        cursor = db.rawQuery(sql_select, null);

        return cursor;

    }

    public List<SmsMessagePackage> getTop10Messages() {

        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String sql_select = " SELECT  * FROM " + TABLE_NAME + " WHERE ISSENT=0 LIMIT 10";

        cursor = db.rawQuery(sql_select, null);

        List<SmsMessagePackage> list = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
          /*  cursor.moveToFirst();

            Integer id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONENUMBER));
            String message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE));

            SmsMessagePackage item = new SmsMessagePackage(id,phoneNumber,message);
            list.add(item ); */

            while (cursor.moveToNext()) {

                Integer id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONENUMBER));
                String message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE));

                SmsMessagePackage item = new SmsMessagePackage(id, phoneNumber, message);
                list.add(item);

            }

        }
        return list;


    }


    public boolean updateMessage(int id )
    {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        Date curDate = new Date();

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("Update "+ TABLE_NAME+ " set issent=1, dateofdispatch='"+dateFormat.format(curDate) +"' where _id="+Integer.toString(id) );
        Log.d("DbHelper","SMS Message with id="+Integer.toString(id)+"is sent");
        return true;
    }
}

