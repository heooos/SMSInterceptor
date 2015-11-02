package com.test.smsinterceptor;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class InterceptKeyword extends ListActivity {

    private SimpleCursorAdapter adapter;
    private SQLiteDatabase dbWrite,dbRead;
    private Db db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intercept_keyword);

        db =new Db(this);
        dbRead = db.getReadableDatabase();
        dbWrite = db.getReadableDatabase();

        adapter = new SimpleCursorAdapter(InterceptKeyword.this,R.layout.keyword_sender_cell_list,null,new String[]{"keyword"},new int[]{R.id.tv_keyword});
        setListAdapter(adapter);
        refreshListView();

        findViewById(R.id.btn_add_keyword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog();
            }
        });
        //长按删除
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(InterceptKeyword.this).setTitle("提醒").setMessage("是否删除？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor c = adapter.getCursor();
                        c.moveToPosition(position);

                        int itemId = c.getInt(c.getColumnIndex("_id"));
                        dbRead.delete("InterceptorKeywordList", "_id=?", new String[]{itemId + ""});
                        refreshListView();
                    }
                }).setNegativeButton("取消", null).show();
                return true;
            }
        });

    }

    //添加屏蔽关键字对话框
    private void ShowDialog(){
            LayoutInflater inflater = LayoutInflater.from(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View view = inflater.inflate(R.layout.layout_keyword_dialog, null);
            builder.setView(view);
            //注意：此处的findViewById方法必须写在dialog的view加载之后！且注意调用的父对象不再是Activity中的view而是当前的View
            final EditText keyWord = (EditText) view.findViewById(R.id.et_input_keyword);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String keyword = keyWord.getText().toString();
                    if(!findKeyword(keyword)){
                        ContentValues cv = new ContentValues();
                        cv.put("keyword", keyword);
                        dbWrite.insert("InterceptorKeywordList", null, cv);
                        refreshListView();
                    }else {
                        Toast.makeText(InterceptKeyword.this, "重复输入", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("取消", null);
            AlertDialog dialog = builder.create();
            dialog.show();
    }
    //列表刷新
    private void refreshListView(){
        Cursor c = dbRead.query("InterceptorKeywordList", null, null, null, null, null, null);
        adapter.changeCursor(c);
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

}
