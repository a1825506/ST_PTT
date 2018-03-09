package com.saiteng.stptt;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;


public class BaseActivity extends FragmentActivity {
    private static final int notifiId = 11;
    protected NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onresume时，取消notification显示


    }

    @Override
    protected void onStart() {
        super.onStart();
        

    }



    /**
     * 返回
     * 
     * @param view
     */
    public void back(View view) {
        finish();
    }

}
