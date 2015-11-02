package com.test.smsinterceptor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity{

    private SimpleCursorAdapter adapter;
    private SQLiteDatabase dbRead;
    private Db db;
    private SMSReceiver s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView listView = (ListView) findViewById(R.id.list);
        db =new Db(this);
        dbRead = db.getReadableDatabase();
        adapter = new SimpleCursorAdapter(MainActivity.this,R.layout.layout_cell_list,null,new String[]{"messageSender","messageBody","date"},new int[]{R.id.tv_messageSender,R.id.tv_messageBody,R.id.tv_date});
        listView.setAdapter(adapter);
        refreshListView();
        //动态注册broadcastecriver
        s = new SMSReceiver();
        IntentFilter i = new IntentFilter();
        i.addAction("android.provider.Telephony.SMS_RECEIVED");
        i.setPriority(1000);
        MainActivity.this.registerReceiver(s, i);
        s.setCallback(new SMSReceiver.Callback() {
            @Override
            public void onDataChange() {
                refreshListView();
            }
        });
        //长按逻辑处理
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(MainActivity.this).setTitle("提醒").setMessage("是否删除？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor c = adapter.getCursor();
                        c.moveToPosition(position);

                        int itemId = c.getInt(c.getColumnIndex("_id"));
                        dbRead.delete("InterceptedMessage", "_id=?", new String[]{itemId + ""});
                        refreshListView();
                    }
                }).setNegativeButton("取消", null).show();
                return true;
            }
        });

    }

    private void refreshListView(){
        Cursor c = dbRead.query("InterceptedMessage",null, null, null, null, null, null);
        adapter.changeCursor(c);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.this.unregisterReceiver(s);
    }

    //使程序不被back键杀死
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_setting:
                Intent i = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
