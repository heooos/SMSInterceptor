package com.test.smsinterceptor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ZH on 2015/10/28.
 */
public class Db extends SQLiteOpenHelper {
    public Db(Context context) {
        super(context, "db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE InterceptorKeywordList(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "keyword TEXT DEFAULT \"\")");
        db.execSQL("CREATE TABLE InterceptorSenderList(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "sender TEXT DEFAULT \"\")");
        db.execSQL("CREATE TABLE InterceptedMessage(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT DEFAULT \"\","+
                "messageBody TEXT DEFAULT \"\"," +
                "messageSender TEXT DEFAULT \"\")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
