package com.saiteng.stptt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.saiteng.Service.MainPTTService;
import com.saiteng.adapter.ProjectPagerAdapter;
import com.saiteng.shardPreferencesHelper.DBManager;
import com.viewpagerindicator.TabPageIndicator;

/**
 * Created by Moore on 2017/7/5.
 */

public class MainPTTActivity extends FragmentActivity implements View.OnTouchListener,View.OnClickListener{
    private ImageView channel_lock,setting,channel_status;
    private TextView  channel_name,channel_status_text,ptt_status;
    private EditText edit_text;
    private FrameLayout ptt;
    private ImageView ptt_img,ptt_to_console,ptt_to_video,btn_talk,btn_add_more;
    private LinearLayout linearLayout;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private int width, height;
    private Context context;
    private TabPageIndicator tabPageIndicator;
    private ViewPager studyViewpager;
    private ProjectPagerAdapter mAdatpter;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private  Intent service;
    private DBManager dbManager;
    public static boolean connect=false;
    public static boolean isCall=false;
    public static Activity activity;



    public static Activity getActivity(){
           return activity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        MyPTTApplication.getInstance().addActivity(this);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        context = getApplicationContext();
        activity=this;
        Intent intent = getIntent();
        int flag = intent.getIntExtra("flag",-1);
        dbManager = new DBManager(MainPTTActivity.this);
        MyPTTApplication.getInstance().setDBManager(dbManager);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        setContentView(R.layout.activity_ptt);
        ViewUtils.inject(this);
        PathUtil.getInstance().initDirs();
        findView();
        mAdatpter = new ProjectPagerAdapter(getSupportFragmentManager(),context);// 此处，如果不是继承的FragmentActivity,而是继承的Fragment，则参数应该传入getChildFragmentManager()
        studyViewpager.setAdapter(mAdatpter);
        tabPageIndicator.setViewPager(studyViewpager);
        intentFilter = new IntentFilter();
        localReceiver = new LocalReceiver();
        intentFilter.addAction(Config.BOARDCAST_DISCONNECT);
        intentFilter.addAction(Config.BOARDCAST_SWITCHCHANNEL);
        intentFilter.addAction(Config.BOARDCAST_SWITCHTOCHAT);
        intentFilter.addAction(Config.BOARDCAST_CALL_FAILED);
        intentFilter.addAction(Config.BOARDCAST_CALL_SUCCESS);
        MyPTTApplication.getInstance().getLocalBroadcastManager().registerReceiver(localReceiver, intentFilter);
        if(flag==1){
            Intent intent1 = new Intent(Config.BOARDCAST_SENDNOTIFY_QUE);
            MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent1);
        }

    }



    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //case后只能跟常量
                case Config.DISCONNECT_FROM_SERVER:
                    context.stopService(service);
                    Intent intent = new Intent();
                    intent.setClass(MainPTTActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case Config.SWITCHTOCHAT:
                    channel_name.setText(Config.CurrentChannel);
                    studyViewpager.setCurrentItem(3);
                    break;
                case Config.SWITCHCHANNEL:
                    channel_name.setText( MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,"default"));
                    break;
                case Config.PTTVISIVLE:
                    Intent intent1 = new Intent(Config.BOARDCAST_CALL);
                    MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent1);
                   // ptt_status.setVisibility(View.VISIBLE);
                    break;
                case Config.PTTINVISIVLE:
                    Intent intent2 = new Intent(Config.BOARDCAST_CALL_END);
                    MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent2);
                    break;
                case Config.CALL_SUCCESS:
                    //通道申请成功，发送对讲语音
//                    if(!isCall){
//                        MyPTTApplication.getInstance().getAudioWrapper().startRecord();
//                    }
                    isCall=true;
                    break;
                case Config.CALL_FAILED:
                    //通道申请失败
                    isCall=false;
                    break;
            }
        }
    };

    private void findView() {
        channel_lock =  (ImageView)findViewById(R.id.channel_lock);
        setting =  (ImageView)findViewById(R.id.setting);
        channel_status =  (ImageView)findViewById(R.id.channel_status);
        ptt_status = (TextView)findViewById(R.id.ptt_status);
        channel_name = (TextView)findViewById(R.id.channel_name);
        channel_status_text = (TextView)findViewById(R.id.channel_status_text);
        ptt_img =  (ImageView)findViewById(R.id.ptt_img);
        linearLayout=  (LinearLayout) findViewById(R.id.viewpage);
        ptt = (FrameLayout) findViewById(R.id.ptt);
        ptt_img.setOnTouchListener(this);
        ptt_img.setOnClickListener(this);
        setting.setOnClickListener(this);
        tabPageIndicator = (TabPageIndicator)findViewById(R.id.tab_page_indicator);
        studyViewpager  = (ViewPager)findViewById(R.id.study_viewpager);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(80
                ,80);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.leftMargin=30;
        channel_lock.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(80
                ,80);
        layoutParams.leftMargin=(int)(width*0.9);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        setting.setLayoutParams(layoutParams);


        layoutParams = new RelativeLayout.LayoutParams((int)(width*0.4)
                ,RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        channel_name.setLayoutParams(layoutParams);
        //默认频道
        channel_name.setText(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,"default"));

        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
                ,RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.head);
        linearLayout.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
                ,RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.channel_name);
        layoutParams.topMargin=20;
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        channel_status.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
                ,RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.channel_name);
        layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.channel_status);
        channel_status_text.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
                ,RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.topMargin=(int)(height*0.8);
        ptt.setLayoutParams(layoutParams);
        ptt.setVisibility(View.INVISIBLE);

//        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
//                ,RelativeLayout.LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        layoutParams.addRule(RelativeLayout.BELOW,R.id.ptt);
//        ptt_status.setLayoutParams(layoutParams);
//        ptt_status.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyPTTApplication.getInstance().getLocalBroadcastManager().unregisterReceiver(localReceiver);
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            Utils.PlayVibrator(context);
            mhandler.sendEmptyMessage(Config.PTTVISIVLE);
            MyPTTApplication.getInstance().getAudioWrapper().startRecord();
            ptt_img.setBackgroundResource(R.mipmap.press_talkbtning);
        }else if(event.getAction()==MotionEvent.ACTION_UP){
            isCall=false;
            Utils.PlayVibrator(context);
           mhandler.sendEmptyMessage(Config.PTTINVISIVLE);
            MyPTTApplication.getInstance().getAudioWrapper().stopRecord();
            ptt_img.setBackgroundResource(R.mipmap.press_talkbtn);
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            event.startTracking();
            return true;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder=new AlertDialog.Builder(MainPTTActivity.this);
            builder.setTitle("警告");
            builder.setMessage("确定退出程序？");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(MyPTTApplication.getInstance().getTcpConnect()!=null){
                        MyPTTApplication.getInstance().getTcpConnect().close();
                    }
                    Intent intent2 = new Intent(Config.BOARDCAST_STOPSERVER);
                    MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent2);
                   finish();
                }
            });
            builder.setNegativeButton("取消",null);
            builder.create();
            builder.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            isCall=false;
            Utils.PlayVibrator(context);
            mhandler.sendEmptyMessage(Config.PTTINVISIVLE);
            MyPTTApplication.getInstance().getAudioWrapper().stopRecord();
            ptt_img.setBackgroundResource(R.mipmap.press_talkbtn);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                Utils.PlayVibrator(context);
                mhandler.sendEmptyMessage(Config.PTTVISIVLE);
                MyPTTApplication.getInstance().getAudioWrapper().startRecord();
                ptt_img.setBackgroundResource(R.mipmap.press_talkbtning);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }









    @Override
    public void onClick(View v) {
      if(v.getId()==R.id.setting){
          Intent intent = new Intent();
          intent.setClass(MainPTTActivity.this, SettingActivity.class);
          startActivity(intent);
      }
    }
    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction()==Config.BOARDCAST_DISCONNECT){
                //连接断开的广播
                Toast.makeText(context, R.string.disconn_from_server,
                        Toast.LENGTH_SHORT).show();
                mhandler.sendEmptyMessage(Config.DISCONNECT_FROM_SERVER);
            }else if(intent.getAction()==Config.BOARDCAST_SWITCHTOCHAT){
                mhandler.sendEmptyMessage(Config.SWITCHTOCHAT);
            }else if(intent.getAction()==Config.BOARDCAST_SWITCHCHANNEL){
                mhandler.sendEmptyMessage(Config.SWITCHCHANNEL);
            }else if(intent.getAction()==Config.BOARDCAST_CALL_SUCCESS){
                //通道申请成功才可以开始发送对讲语音
                mhandler.sendEmptyMessage(Config.CALL_SUCCESS);
            }else if(intent.getAction()==Config.BOARDCAST_CALL_FAILED){
                //通道申请失败
                mhandler.sendEmptyMessage(Config.CALL_FAILED);
            }
        }
    }



}
