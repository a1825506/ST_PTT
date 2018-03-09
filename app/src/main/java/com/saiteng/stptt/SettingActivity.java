package com.saiteng.stptt;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ione.opustool.OpusJni;
import com.saiteng.adapter.SettingAdapter;
import com.saiteng.conn.DefFrame;
import com.saiteng.dialog.CommonDialog;
import com.videotakepicture.VideoMainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Moore on 2017/8/16.
 */

public class SettingActivity extends Activity implements AdapterView.OnItemClickListener{
    private ImageView channel_lock,setting,channel_status;
    private TextView channel_name,channel_status_text,ptt_status;
    private SettingAdapter settingAdapter;
    private ListView setting_listview;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private int width, height;
    private List<Map> data = new ArrayList<Map>();
    private Context context = null;
    private RadioGroup radioGroup;
    private Handler mhandler;
    private int AUDIOPARAM_8K = 0x01;
    private int AUDIOPARAM_16K= 0x02;
    private int AUDIOPARAM_24K= 0x03;
    private int AUDIOPARAM_44K= 0x04;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_setting);
        MyPTTApplication.getInstance().addActivity(this);
        context = getApplicationContext();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        findView();
        mhandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what==AUDIOPARAM_8K){
                    Config.AUDIORECORD_SAMOLERATE=8000;
                }else if(msg.what==AUDIOPARAM_16K){
                    Config.AUDIORECORD_SAMOLERATE=16000;
                }else if(msg.what==AUDIOPARAM_24K){
                    //samplerate=24000;
                }else if(msg.what==AUDIOPARAM_44K){
                   // samplerate=44100;
                }
            }
        };
    }

    private void findView() {

        channel_lock =  (ImageView)findViewById(R.id.channel_lock);
        setting =  (ImageView)findViewById(R.id.setting);
        channel_status =  (ImageView)findViewById(R.id.channel_status);
        channel_name = (TextView)findViewById(R.id.channel_name);
        channel_status_text = (TextView)findViewById(R.id.channel_status_text);

        channel_lock.setVisibility(View.GONE);
        setting.setVisibility(View.GONE);
        channel_status.setVisibility(View.GONE);
        channel_name.setText("相关设置");
        channel_status_text.setVisibility(View.GONE);
        setting_listview = (ListView)findViewById(R.id.setting_listview);


        data = getData();
        settingAdapter = new SettingAdapter(data,context);
        setting_listview.setAdapter(settingAdapter);
        setting_listview.setOnItemClickListener(this);
    }

    private List<Map> getData() {
        Map<String,Object> map=null;

        for(int i=0;i<3;i++){
            map = new HashMap<String,Object>();
            if(i==0){
                map.put("user_name",MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,""));
                map.put("user_id",MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0));

            }else if(i==1){
                map.put("user_name","音频参数设置");
                map.put("user_id","");

            }else if(i==2){

                map.put("user_name","开启密录");

                map.put("user_id","");
            }
            data.add(map);
        }

        return data;
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position==0){
          //账户管理界面
            Intent intent = new Intent();
            intent.setClass(SettingActivity.this, AccountActivity.class);
            startActivity(intent);
        }else if(position==1){
            //音频参数设置界面

            CommonDialog commonDialogRec =new CommonDialog(this,R.layout.audiodialog_view,"音频参数设置","设置","取消");
            View contentview =commonDialogRec.getDialogView();
            radioGroup = (RadioGroup) contentview.findViewById(R.id.audio_param);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                  if(checkedId==R.id.audio_param8k){
                      mhandler.sendEmptyMessage(AUDIOPARAM_8K);
                  }else if(checkedId==R.id.audio_param16k){
                      mhandler.sendEmptyMessage(AUDIOPARAM_16K);
                  }else if(checkedId==R.id.audio_param24k){
                      mhandler.sendEmptyMessage(AUDIOPARAM_24K);
                  }else if(checkedId==R.id.audio_param44k){
                      mhandler.sendEmptyMessage(AUDIOPARAM_44K);
                  }
                }
            });
            commonDialogRec.setOnDiaLogListener(new CommonDialog.OnDialogListener() {
                @Override
                public void dialogPositiveListener(View customView, DialogInterface dialogInterface, int which) {
                    OpusJni.getInstance().Opusclose();
                    OpusJni.getInstance().Opusopen(Config.AUDIORECORD_SAMOLERATE,8);
                }

                @Override
                public void dialogNegativeListener(View customView, DialogInterface dialogInterface, int which) {

                }
            });
            commonDialogRec.showDialog();
        }else if(position==2){
            //开启密录
            Intent intent = new Intent();
            intent.setClass(SettingActivity.this, VideoMainActivity.class);
            startActivity(intent);
        }
    }
}
