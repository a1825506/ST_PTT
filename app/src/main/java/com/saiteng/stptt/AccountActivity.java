package com.saiteng.stptt;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.saiteng.adapter.SettingAdapter;
import com.saiteng.conn.TcpConnect;
import com.saiteng.dialog.CommonDialog;

/**
 * Created by Moore on 2017/8/16.
 */

public class AccountActivity extends Activity implements View.OnClickListener{

    private Button btn_loginout;
    private CommonDialog commonDialog;

    private ImageView channel_lock,setting,channel_status;
    private TextView channel_name,channel_status_text,ptt_status;
    private SettingAdapter settingAdapter;
    private ListView setting_listview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_account);
        MyPTTApplication.getInstance().addActivity(this);
        findview();

    }

    private void findview() {
        btn_loginout = (Button) findViewById(R.id.loginout);
        btn_loginout.setOnClickListener(this);
        channel_lock =  (ImageView)findViewById(R.id.channel_lock);
        setting =  (ImageView)findViewById(R.id.setting);
        channel_status =  (ImageView)findViewById(R.id.channel_status);
        channel_name = (TextView)findViewById(R.id.channel_name);
        channel_status_text = (TextView)findViewById(R.id.channel_status_text);

        channel_lock.setVisibility(View.GONE);
        setting.setVisibility(View.GONE);
        channel_status.setVisibility(View.GONE);
        channel_name.setText("账号管理");
        channel_status_text.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.loginout){
            commonDialog =new CommonDialog(this,"请稍后","确定要退出登录？","确定","取消");
            commonDialog.setOnDiaLogListener(new CommonDialog.OnDialogListener() {
                @Override
                public void dialogPositiveListener(View customView, DialogInterface dialogInterface, int which) {
                    Intent intent = new Intent(Config.BOARDCAST_LOGIN_QUE);
                    MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                }
                @Override
                public void dialogNegativeListener(View customView, DialogInterface dialogInterface, int which) {
                    //点击取消按钮
                }
            });
            commonDialog.showDialog();
        }
    }


}
