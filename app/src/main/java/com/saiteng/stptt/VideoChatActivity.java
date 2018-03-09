package com.saiteng.stptt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.saiteng.rtp.RtpSenderWrapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import example.sszpf.x264.x264sdk;

/**
 * Created by Moore on 2018-03-08.
 */

public class VideoChatActivity extends Activity implements  SurfaceHolder.Callback2,Camera.PreviewCallback{
    private SurfaceView msurfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera.Parameters parameters;

    private int width = 640;

    private int height = 480;

    private int fps = 20;

    private int bitrate = 90000;

    private x264sdk x264;

    private int timespan = 90000 / fps;

    private long time;

    private Camera camera;

    private static WindowManager wm;

    private static WindowManager.LayoutParams wmParams;

    private Context mcontext;

    private RecordView recordView;

    private String TAG = "VideoChatActivity";

    private RtpSenderWrapper rtpSenderWrapper;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.fragment_video);
        mcontext = VideoChatActivity.this;
        msurfaceView = (SurfaceView)findViewById(R.id.videoshow);
        surfaceHolder = msurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        x264 = new x264sdk(listener);
        createFloatView();

        Intent intent1 = new Intent(Config.BOARDCAST_CALL);
        MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent1);

    }



    private x264sdk.listener listener = new x264sdk.listener(){

        @Override
        public void h264data(byte[] buffer, int length) {
            // 编码后的数据准备发送
            rtpSenderWrapper.sendAvcPacket(buffer,0,length,0);
        }
    };
    private boolean createFloatView() {

        wm = (WindowManager) mcontext.getSystemService(Context.WINDOW_SERVICE);

        wmParams = new WindowManager.LayoutParams();

        String packname = mcontext.getPackageName();
        PackageManager pm = mcontext.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", packname));
        if(permission){
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }else{
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

        // 设置window type
       // wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;

        wmParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明

        // 设置Window flag
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // 设置悬浮窗的长得宽
        wmParams.width = Config.viewWidth;
        wmParams.height = Config.viewHeight;

        // 调整悬浮窗到右上角
        wmParams.gravity = Gravity.TOP | Gravity.RIGHT;

        recordView = new RecordView(mcontext, wm, wmParams);

        if (recordView == null) {
            return false;
        }

        wm.addView(recordView, wmParams);

        return true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        time += timespan;
        byte[] yuv420 = new byte[width*height*3/2];
        YUV420SP2YUV420(data,yuv420,width,height);
        x264.PushOriStream(yuv420, yuv420.length, time);
    }



    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        x264.initX264Encode(width, height, fps, bitrate);
        camera = getBackCamera();
        startcamera(camera);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != camera) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        wm.removeView(recordView);
        x264.CloseX264Encode();


        Intent intent1 = new Intent(Config.BOARDCAST_CALL_END);
        MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent1);
    }
    private void startcamera(Camera mCamera){
        if(mCamera != null){
            try {
                mCamera.setPreviewCallback(this);
                mCamera.setDisplayOrientation(90);
                if(parameters == null){
                    parameters = mCamera.getParameters();
                }
                parameters = mCamera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
                parameters.setPreviewSize(width, height);
                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Camera getBackCamera() {
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }


    private void YUV420SP2YUV420(byte[] yuv420sp, byte[] yuv420, int width, int height)
    {
        if (yuv420sp == null ||yuv420 == null)return;
        int framesize = width*height;
        int i = 0, j = 0;
        //copy y
        for (i = 0; i < framesize; i++)
        {
            yuv420[i] = yuv420sp[i];
        }
        i = 0;
        for (j = 0; j < framesize/2; j+=2)
        {
            yuv420[i + framesize*5/4] = yuv420sp[j+framesize];
            i++;
        }
        i = 0;
        for(j = 1; j < framesize/2;j+=2)
        {
            yuv420[i+framesize] = yuv420sp[j+framesize];
            i++;
        }
    }
}
