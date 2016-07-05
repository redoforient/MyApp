package com.ips.myapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    public static final String tag = MainActivity.class.getSimpleName();
    private ProgressDialog pd;
    private Context context;
    private boolean cancelable = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;
        CustomProgress.show(this, "登录中...", true, null);
    }

}
