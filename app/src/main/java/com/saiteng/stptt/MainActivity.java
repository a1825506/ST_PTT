package com.saiteng.stptt;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.saiteng.Service.MainPTTService;
import com.saiteng.shardPreferencesHelper.SharedTools;


public class MainActivity extends Activity implements OnClickListener{
    private LinearLayout layout_linearlayout;
    private String username;
    private String password;
    private String ip;
    private Button btn_login;
    private TextView ptt_version,ptt_title;
    private EditText edit_username;
    private EditText edit_password;
    private EditText edit_ip;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private int width, height;
    private Context context;
    private IntentFilter intentFilter;
    private LoginReceiver loginReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private SharedTools sharedTools;
    private  Intent service;
    private boolean conn=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyPTTApplication.getInstance().addActivity(this);
        context = getApplicationContext();
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        sharedTools = new SharedTools(MainActivity.this);
        MyPTTApplication.getInstance().setSharedTools(sharedTools);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        MyPTTApplication.getInstance().setLocalBroadcastManager(localBroadcastManager);
        if(isServiceRunning()){
                 Intent intent = new Intent();
                 //不需要发送通知帧
                 intent.putExtra("flag",0);
                 intent.setClass(MainActivity.this, MainPTTActivity.class);
                 startActivity(intent);
                 finish();
        }else{
           intentFilter = new IntentFilter();
          loginReceiver = new LoginReceiver();
          intentFilter.addAction(Config.BOARDCAST_LOGIN_UNREGISTER);
          intentFilter.addAction(Config.BOARDCAST_LOGIN_FAILED);
          intentFilter.addAction(Config.BOARDCAST_LOGIN_SUCCESS);
          intentFilter.addAction(Config.BOARDCAST_LOGIN_FORBIDDEN);
          intentFilter.addAction(Config.BOARDCAST_LOGIN_UNKOWN);
          intentFilter.addAction(Config.BOARDCAST_CONNECT_ERROR);
          intentFilter.addAction(Config. BOARDCAST_CONNECT_SUCCESS);

          localBroadcastManager.registerReceiver(loginReceiver, intentFilter);
          width = displayMetrics.widthPixels;
          height = displayMetrics.heightPixels;
          setContentView(R.layout.activity_main);
          findView();
        //启动主服务，该服务不与调用者绑定。
          service = new Intent(context, MainPTTService.class);
          context.startService(service);
        }
    }

  /**
   *获取系统中正在运行的服务
   */
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.saiteng.Service.MainPTTService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void findView() {
        layout_linearlayout = (LinearLayout)findViewById(R.id.linearLayout1);
        btn_login = (Button)findViewById(R.id.login);
        edit_username = (EditText)findViewById(R.id.username);
        edit_password = (EditText)findViewById(R.id.password);
        edit_ip = (EditText)findViewById(R.id.ip_address);
        ptt_version = (TextView) findViewById(R.id.ptt_version);
        ptt_title = (TextView) findViewById(R.id.ptt_title);
        edit_username.setText(sharedTools.getShareString(Config.CurrentUser,""));
        edit_password.setText(sharedTools.getShareString("password",""));
        edit_ip.setText(sharedTools.getShareString("ip",""));
        btn_login.setOnClickListener(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int)(width*0.8), (int)(height*0.2));
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layout_linearlayout.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.ABOVE,R.id.linearLayout1);
        ptt_title.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(
                (int)(width*0.8),(int)(height*0.08));
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.BELOW,R.id.linearLayout1);
        params.topMargin = (int)(height*0.05);
        btn_login.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.BELOW,R.id.login);
        params.topMargin = (int)(height*0.05);
        ptt_version.setLayoutParams(params);
        ptt_version.setText(getVersion());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(localBroadcastManager!=null){
            localBroadcastManager.unregisterReceiver(loginReceiver);
        }
    }

    /**
     2  * 获取版本号
     3  * @return 当前应用的版本号
     4  */
  public String getVersion() {
             try {
                     PackageManager manager = this.getPackageManager();
                     PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
                     String version = info.versionName;
                     return  "当前版本："+ version;
                 } catch (Exception e) {
                     e.printStackTrace();
                     return "无法获取当前版本号";
                 }
         }

    @Override
    public void onClick(View v) {
     switch(v.getId()){
         case R.id.login:
             username = edit_username.getText().toString();
             password = edit_password.getText().toString();
             ip = edit_ip.getText().toString();

             Config.SERVER_DESTIP = ip;
             Config.SERVER_IP = ip;
             if(ip.length()==0){
                 Toast.makeText(context,"填写服务器ip",Toast.LENGTH_LONG).show();
             }
             if(username.length()==0){
                 Toast.makeText(context,"账号，密码不能为空",Toast.LENGTH_LONG).show();
             }else{
                 //如果当前没有连接，则发送建立连接的通知，连接是在服务中创建
                 if(!conn){
                     Intent intent = new Intent(Config.BOARDCAST_CREATECONN);
                     localBroadcastManager.sendBroadcast(intent);
                 }else
                     createLoginOreder();
             }
             break;
     }
    }

    private Handler  mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==Config.LOGIN_SUCCESS){
                //登录成功保存当前的用户名和用户ID。
                conn=true;
                sharedTools.setShareString(Config.CurrentUser,username);
                sharedTools.setShareString(Config.CurrentChannel,"default");
                sharedTools.setShareString("password",password);
                sharedTools.setShareString("ip",ip);
                sharedTools.setShareInt(Config.CurrentUserID, Utils.bytes2Int(Config.USERID,0,4));
                Intent intent = new Intent();
                //需要发送通知帧
                intent.putExtra("flag",1);
                intent.setClass(MainActivity.this, MainPTTActivity.class);
                startActivity(intent);
                finish();
            }else if(msg.what==Config.LOGIN_FAILED){
                //密码错误
                conn=true;
                Toast.makeText(context,"密码错误",Toast.LENGTH_LONG).show();

            }else if(msg.what==Config.LOGIN_FORBIDDEN){
                //禁止登陆
                conn=true;
                Toast.makeText(context,"禁止登陆",Toast.LENGTH_LONG).show();
            }else if(msg.what==Config.LOGIN_UNREGISTER){
                //用户未注册
                conn=true;
                Toast.makeText(context,"用户未注册",Toast.LENGTH_LONG).show();
            }else if(msg.what==Config.LOGIN_UNKOWN){
                conn=true;
                Toast.makeText(context,"登录未知错误",Toast.LENGTH_LONG).show();
            }else if(msg.what==Config.CONN_ERROR){
                conn=false;
                Toast.makeText(context,"连接服务器失败",Toast.LENGTH_LONG).show();
            }else if(msg.what==Config.CONNECT_SUCCESS){
                conn=true;
                createLoginOreder();
            }
        }
    };
   // 创建用户登录命令
    private byte[] createLoginOreder() {
        byte[] name = username.getBytes();
        byte[] pw=password.getBytes();
        final byte[] order = new byte[106];
        byte [] head={0x68,0x00,0x64,0x68,0x00,0x00,0x00,0x01};
        System.arraycopy(head, 0, order, 0, head.length);
        System.arraycopy(name, 0, order, head.length, name.length);
        System.arraycopy(pw, 0, order, 72, pw.length);
        int sum=0;
        for(int i=4;i<104;i++){
            sum+=order[i];
        }
        order[104]=(byte)sum;
        order[105]=0x16;
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyPTTApplication.getInstance().getTcpConnect().sendOrder(order,order.length);
            }
        }).start();

        return order;
    }

    class LoginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction()==Config.BOARDCAST_LOGIN_SUCCESS){
                //登录成功
                mhandler.sendEmptyMessage(Config.LOGIN_SUCCESS);
            }else if(intent.getAction()==Config.BOARDCAST_LOGIN_FAILED){
                //登录失败
                mhandler.sendEmptyMessage(Config.LOGIN_FAILED);
            }else if(intent.getAction()==Config.BOARDCAST_LOGIN_UNREGISTER){
                //未注册用户
                mhandler.sendEmptyMessage(Config.LOGIN_UNREGISTER);
            }else if(intent.getAction()==Config.BOARDCAST_LOGIN_FORBIDDEN){
                //登录禁用
                mhandler.sendEmptyMessage(Config.LOGIN_FORBIDDEN);
            }else if(intent.getAction()==Config.BOARDCAST_LOGIN_UNKOWN){
                mhandler.sendEmptyMessage(Config.LOGIN_UNKOWN);
            }else if(intent.getAction()==Config.BOARDCAST_CONNECT_ERROR){
                mhandler.sendEmptyMessage(Config.CONN_ERROR);
            }else if(intent.getAction()==Config.BOARDCAST_CONNECT_SUCCESS){
                mhandler.sendEmptyMessage(Config.CONNECT_SUCCESS);
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if(MyPTTApplication.getInstance().getTcpConnect()!=null){
                MyPTTApplication.getInstance().getTcpConnect().close();
            }
            context.stopService(service);
        }
        return super.onKeyDown(keyCode, event);
    }
}
