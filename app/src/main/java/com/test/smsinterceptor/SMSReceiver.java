package com.test.smsinterceptor;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SMSReceiver extends BroadcastReceiver {


    private SQLiteDatabase dbRead,dbWrite;
    private Db db;

    @Override
    public void onReceive(Context context, Intent intent) {

        db =new Db(context);
        dbRead = db.getReadableDatabase();
        dbWrite =db.getWritableDatabase();

        Bundle extras = intent.getExtras();
        if(extras == null){
            return ;
        }
        Object[] pdus = (Object[]) extras.get("pdus");
        for (int i = 0; i<pdus.length; i++){
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String date = getStringDate();
            String messageSender = message.getOriginatingAddress();
            String messageBody = message.getMessageBody();

            if(findSender(messageSender)){
                abortBroadcast();
                addMessage(messageSender, messageBody, date);
            }
            else if (findKeyword(messageBody)){
                abortBroadcast();
                addMessage(messageSender, messageBody, date);
            }
        }
    }

    public boolean findSender(String sender){

        Cursor c = dbRead.rawQuery("select * from InterceptorSenderList where sender=?", new String[]{sender});
        boolean result = c.moveToNext();
        c.close();
        return result;
    }
    //查重
    public boolean findKeyword(String keyword){
        Cursor c = dbRead.query("InterceptorKeywordList", null, null, null, null, null, null);
        while (c.moveToNext()) {
            String str = c.getString(c.getColumnIndex("keyword"));
            boolean result = keyword.contains(str);
            if (result) {
                return result;
            }
        }
        c.close();
        return false;
    }

    public void addMessage(String messageSender,String messageBody,String date){
        ContentValues values =new ContentValues();
        values.put("messageSender",messageSender);
        values.put("messageBody",messageBody);
        values.put("date", date);
        dbWrite.insert("InterceptedMessage", null, values);
        callback.onDataChange();
    }
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    //回调机制 用于动态刷新短信界面
    private Callback callback =null;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    public static interface Callback{
        void onDataChange();
    }
}
