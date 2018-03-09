package com.videotakepicture;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.saiteng.stptt.Config;

/**
 * Created by Moore on 2018-01-26.
 */

public class videoService extends Service {

    private static videoService videoService = null;

    private Context mContext;

    private VideoView videoView = null;

    private RecordView recordView =null;

    private WindowManager wm;

    private WindowManager.LayoutParams wmParams;

    private int mStartId = 0;

    private static final String ACTION_SIZE = "Action_Size";

    private long lastPressBack;

    public static videoService getInstance(Context context) {

        return videoService;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        videoService=this;

        mContext = getBaseContext().getApplicationContext();
    }

    @Override
    public void onStart(Intent intent, int startId){

        videoView = null;

        videoView = new VideoView();

        createFloatView();

        mStartId = startId;

        videoView.init(recordView.getVideoDisplayView());

        videoView.setRecordView(recordView);

        Intent intent2 = new Intent(ACTION_SIZE);

        mContext.sendBroadcast(intent2);

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case Config.SERVICE_MESSAGE_EXIT:

                    if (recordView != null) {

                        wm.removeView(recordView);

                        recordView = null;

                        stopSelf(mStartId);
                    }

                    break;

                case Config.SERVICE_MESSAGE_STOP_RECORD:

                    videoView.stopRecord();

                    break;
                case Config.SERVICE_MESSAGE_TAKEPIC:

                    videoView.takePicture();

                    VideoUtils.vibrateOnce(mContext);

                    break;
                case Config.SERVICE_MESSAGE_START_RECORD:

                    if (videoView.isRecording()) {
                        VideoUtils.vibrateTwice(mContext);
                        videoView.stopRecord();
                    }else{
                        Intent intent = new Intent(ACTION_SIZE);
                        mContext.sendBroadcast(intent);
                        videoView.startRecord();
                        VideoUtils.vibrateOnce(mContext); // 震动一次
                    }
                    break;
                case Config.SERVICE_MESSAGE_CHANGE_CAMERA:

                    if (videoView.isRecording()) {

                        if (videoView.getmCurrentCameraId() == 1) {

                            videoView.setmCurrentCameraId(0); // 切换至后置摄像头

                            VideoUtils.vibrateTwice(mContext); // 震动两次

                        } else if (videoView.getmCurrentCameraId() == 0) {

                            videoView.setmCurrentCameraId(1); // 切换至前置摄像头

                            VideoUtils.vibrateOnce(mContext); // 震动一次
                        }
                        videoView.stopRecord();

                        if (VideoUtils.getAvailableSizeData() <= 2) {

                            VideoUtils.vibrateThrice(mContext);
                        } else {
                            Intent intent = new Intent(ACTION_SIZE);
                            sendBroadcast(intent);
                            videoView.startRecord();
                        }
                    } else {
                        if (videoView.getmCurrentCameraId() == 1) {
                            videoView.setmCurrentCameraId(0); // 切换至后置摄像头
                            VideoUtils.vibrateTwice(mContext); // 震动两次
                        } else if (videoView.getmCurrentCameraId() == 0) {
                            videoView.setmCurrentCameraId(1); // 切换至前置摄像头
                            VideoUtils.vibrateOnce(mContext); // 震动一次
                        }
                        videoView.startPreview();
                    }
                    break;
                case Config.SERVICE_MESSAGE_CHANGE_ZOOM:
                    int curZoomValue = msg.getData().getInt("progress");
                    videoView.changerZoom(curZoomValue);

                    break;
                case Config.VOLUME_CHANGED:
                    Log.e("VideoService","按下了音量键");
                    break;
            }
        }
    };

    private boolean createFloatView() {

        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        wmParams = new WindowManager.LayoutParams();

        // 设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;

        wmParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明

        // 设置Window flag
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        // 设置悬浮窗的长得宽
        wmParams.width = Config.viewWidth;

        wmParams.height = Config.viewHeight;

        // 调整悬浮窗到右上角
        wmParams.gravity = Gravity.TOP | Gravity.RIGHT;

        recordView = new RecordView(mContext, wm, wmParams);

        recordView.setFocusableInTouchMode(true);

        recordView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if(event.getAction()==KeyEvent.ACTION_UP){

                    if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
                        mHandler.sendEmptyMessage(Config.SERVICE_MESSAGE_START_RECORD);
                        Log.i("VideoService","KEYCODE_VOLUME_DOWN");
                        return true;
                    }else if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
                        mHandler.sendEmptyMessage(Config.SERVICE_MESSAGE_TAKEPIC);
                        Log.i("VideoService","KEYCODE_VOLUME_UP");
                        return true;
                    }else if(keyCode==KeyEvent.KEYCODE_ENTER){
                        mHandler.sendEmptyMessage(Config.SERVICE_MESSAGE_START_RECORD);
                        Log.i("VideoService","KEYCODE_ENTER");
                        return true;
                    }else if(keyCode==KeyEvent.KEYCODE_BACK){
                        if (System.currentTimeMillis() - lastPressBack <= 3000) {
                            Log.i("VideoService","KEYCODE_BACK");
                            mHandler.sendEmptyMessage(Config.SERVICE_MESSAGE_STOP_RECORD);
                            mHandler.sendEmptyMessage(Config.SERVICE_MESSAGE_EXIT);
                            return true;
                        } else {
                            Toast.makeText(mContext, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                            lastPressBack = System.currentTimeMillis();
                        }

                    }

                }

                return true;
            }
        });


        if (recordView == null) {

            return false;
        }
        recordView.setHandler(mHandler);

        recordView.setVideoView(videoView);

        wm.addView(recordView, wmParams);

        return true;

    }
}
