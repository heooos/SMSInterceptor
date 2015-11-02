package com.test.smsinterceptor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingview);

        findViewById(R.id.btn_keyword).setOnClickListener(this);
        findViewById(R.id.btn_sender).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_keyword:
                startActivity(new Intent(this,InterceptKeyword.class));
                break;
            case R.id.btn_sender:
                startActivity(new Intent(this,InterceptSender.class));
                break;
        }
    }
}
