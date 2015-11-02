package com.test.smsinterceptor;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterceptSender extends ListActivity {

    private SimpleCursorAdapter adapter;
    private SQLiteDatabase dbWrite,dbRead;
    private Db db;
    private List<Map<String, Object>> dataList;
    private ListView listView;
    private SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intercept_sender);
        //数据库初始化
        db =new Db(this);
        dbRead = db.getReadableDatabase();
        dbWrite = db.getWritableDatabase();

        adapter = new SimpleCursorAdapter(InterceptSender.this,R.layout.keyword_sender_cell_list,null,new String[]{"sender"},new int[]{R.id.tv_sender});
        setListAdapter(adapter);
        refreshListView();

        findViewById(R.id.btn_add_sender).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog();
            }
        });
        findViewById(R.id.btn_add_senderFromContacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog1();
            }
        });

        //长按删除
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(InterceptSender.this).setTitle("提醒").setMessage("是否删除？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Cursor c = adapter.getCursor();
                        c.moveToPosition(position);

                        int itemId = c.getInt(c.getColumnIndex("_id"));
                        dbRead.delete("InterceptorSenderList", "_id=?", new String[]{itemId + ""});
                        refreshListView();
                        c.close();

                    }
                }).setNegativeButton("取消", null).show();
                return true;
            }
        });
    }

    //自定义添加发件人
    private void ShowDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = inflater.inflate(R.layout.layout_sender_dialog, null);
        builder.setView(view);
        //注意：此处的findViewById方法必须写在dialog的view加载之后！且注意调用的父对象不再是Activity中的view而是当前的View
        final EditText Sender = (EditText) view.findViewById(R.id.et_input_sender);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sender = Sender.getText().toString();
                if (!findSender(sender)) {
                    ContentValues cv = new ContentValues();
                    cv.put("sender", sender);
                    dbWrite.insert("InterceptorSenderList", null, cv);
                    refreshListView();
                }else {
                    Toast.makeText(InterceptSender.this, "重复输入", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //从联系人中添加发件人
    private void ShowDialog1(){
        LayoutInflater inflater = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = inflater.inflate(R.layout.dialog_listview, null);
        builder.setView(view);
        //注意：此处的findViewById方法必须写在dialog的view加载之后！且注意调用的父对象不再是Activity中的view而是当前的View
        listView = (ListView) view.findViewById(R.id.listView);
        dataList = new ArrayList<Map<String, Object>>();
        readContacts();
        listView.setAdapter(simpleAdapter);
        final AlertDialog dialog = builder.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listview = (ListView) parent;
                HashMap<String, Object> data = (HashMap<String, Object>) listview.getItemAtPosition(position);
                String number = (String) data.get("tvNumber");
                if(!findSender(number)){
                    ContentValues cv = new ContentValues();
                    cv.put("sender", number);
                    dbWrite.insert("InterceptorSenderList", null, cv);
                    refreshListView();
                    dialog.cancel();
                }else {
                    Toast.makeText(InterceptSender.this, "重复输入", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.show();
    }
    //刷新Activity列表
    private void refreshListView() {
        Cursor c = dbRead.query("InterceptorSenderList", null, null, null, null, null, null);
        adapter.changeCursor(c);
    }
        // 读取系统的联系人列表。
    public void readContacts(){
        Cursor cursor = InterceptSender.this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        String phoneNumber;
        String phoneName;
        while (cursor.moveToNext()) {
            phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));//电话号码
            phoneName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));//姓名
            simpleAdapter = new SimpleAdapter(InterceptSender.this, getData(phoneName,phoneNumber), R.layout.dialog_listview_cell, new String[]{"head", "tvName", "tvNumber"}, new int[]{R.id.head, R.id.tvName, R.id.tvNumber});
            listView.setAdapter(simpleAdapter);
        }
    }

    //生成联系人列表方法
    private List<Map<String, Object>> getData(String name, String number) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("head", R.drawable.contact);
        map.put("tvName", name);
        map.put("tvNumber", number);
        dataList.add(map);
        return dataList;
    }
    //查重
    public boolean findSender(String keyword){
        Cursor c = dbRead.query("InterceptorSenderList", null, null, null, null, null, null);
        while (c.moveToNext()) {
            String str = c.getString(c.getColumnIndex("sender"));
            boolean result = keyword.contains(str);
            if (result) {
                return result;
            }
        }
        c.close();
        return false;
    }

}