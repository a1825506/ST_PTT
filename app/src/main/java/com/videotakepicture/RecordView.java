package com.videotakepicture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.saiteng.stptt.Config;
import com.saiteng.stptt.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Moore on 2018-01-26.
 */

public class RecordView extends RelativeLayout implements
        SeekBar.OnSeekBarChangeListener {

    private Context mContext = null;
    private Handler mParentHandler = null;
    private View viewRecordFrame;

    private RelativeLayout viewVideoViewControl;
    private RelativeLayout viewVideoView;

    private SurfaceView viewVideoDisplay;

    private TextView sizeView;
    private int duration;
    private TextView mTimeView;
    private int width;
    private int height;
    private SeekBar changeZoom;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private LayoutParams mVideoViewParams;
    private VideoView mVideoView;

    private int timers;
    private Timer timer;
    private Timer t;
    private TimerTask task;
    private TimerTask tasks;

    private int maxChangeZomm;

    private static final String ACTION_SIZE = "Action_Size";

    public RecordView(Context context, WindowManager wm,
                      WindowManager.LayoutParams wmParams) {
        super(context);

        mContext = context;

        mWindowManager = wm;

        mWindowParams = wmParams;

        viewRecordFrame = View.inflate(context, R.layout.record_view, null);

        viewVideoViewControl = (RelativeLayout) viewRecordFrame
                .findViewById(R.id.id_video_view_control);

        viewVideoView = (RelativeLayout) viewRecordFrame
                .findViewById(R.id.id_video_view);

        sizeView = (TextView) viewRecordFrame.findViewById(R.id.txtVideoFramerate);

        mTimeView = (TextView) viewRecordFrame.findViewById(R.id.txtNetType);


        changeZoom = (SeekBar) viewRecordFrame.findViewById(R.id.changeZoom);
        changeZoom.setOnSeekBarChangeListener(this);
        changeZoom.setMax(30); // 设置最大值

        viewVideoDisplay = (SurfaceView) viewRecordFrame
                .findViewById(R.id.id_video_display_view);

        viewVideoView.setOnTouchListener(viewOnTouchListener);
        viewVideoViewControl.setOnTouchListener(viewOnTouchListener);

        registerBoradcastReceiver();


        this.addView(viewRecordFrame);

    }

    public SurfaceView getVideoDisplayView() {
        return viewVideoDisplay;
    }

    OnTouchListener viewOnTouchListener = new OnTouchListener() {

        public boolean onTouch(View v, MotionEvent event) {
            int action;
            float posx;
            float posy;
            if (null == event) {
                return false;
            }

            action = event.getAction();
            posx = event.getX();
            posy = event.getY();


            if (v.getId() == R.id.id_video_view_control) {
                if (action == MotionEvent.ACTION_DOWN) {
                    viewVideoViewControl.setVisibility(View.GONE);
                    viewVideoView.setVisibility(View.VISIBLE);
                    updateViewPosition(Config.viewWidth, Config.viewHeight);
                    mVideoViewParams = (LayoutParams) viewVideoDisplay
                            .getLayoutParams();
                    mVideoViewParams.width = Config.viewWidth;
                    mVideoViewParams.height = Config.viewHeight;
                    viewVideoDisplay.setLayoutParams(mVideoViewParams);

                    return true;
                }
            } else if (v.getId() == R.id.id_video_view) {
                if (action == MotionEvent.ACTION_DOWN) {
                    if ((posx < width / 4) && (posy < height / 6)) {
                        changeZoom.setProgress(0);
                        // 切换前后摄像头
                        if (mParentHandler != null) {
                            mParentHandler
                                    .sendEmptyMessage(Config.SERVICE_MESSAGE_CHANGE_CAMERA);
                        }
                    } else if ((posx >= (width * 3 / 4)) && (posy < height / 6)) {
                        if (mParentHandler != null) {
                            viewVideoViewControl.setVisibility(View.VISIBLE);
                            viewVideoView.setVisibility(View.INVISIBLE);
                            updateViewPosition(100, 100);
                            mVideoViewParams = (LayoutParams) viewVideoDisplay
                                    .getLayoutParams();
                            mVideoViewParams.width = 1;
                            mVideoViewParams.height = 1;
                            viewVideoDisplay.setLayoutParams(mVideoViewParams);
                        }
                    } else if ((posx < (width / 4)) && posy >= (height * 5 / 6)) {
                        if (timer != null || task != null) {
                            timer.cancel();
                            task.cancel();
                        }

                        if (changeZoom.getVisibility() == View.GONE) {
                            changeZoom.setVisibility(View.VISIBLE);

                        } else if (changeZoom.getVisibility() == View.VISIBLE) {
                            changeZoom.setVisibility(View.GONE);
                        }
                    } else if ((posx >= (width * 3 / 4))
                            && posy >= (height * 5 / 6)) {
                        // 退出程序

                        if (mParentHandler != null) {
                            mParentHandler.sendEmptyMessage(Config.SERVICE_MESSAGE_STOP_RECORD);
                            mParentHandler.sendEmptyMessage(Config.SERVICE_MESSAGE_EXIT);
                        }

                    }
                }
            }
            return true;
        }
    };

    public void setVideoView(VideoView videoView){
        mVideoView = videoView;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setMaxChangeZomm(int maxChangeZomm) {
        this.maxChangeZomm = maxChangeZomm;
    }

    public void setHandler(Handler handler) {
        mParentHandler = handler;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                sendEmptyMessageDelayed(1, 1000);
                duration += 1;
                if (duration >= 15 * 60) {
                    if (VideoUtils.getAvailableSizeData() <= 2) {
                        VideoUtils.vibrateThrice(mContext);
                        sizeView.setText("存储空间不足！");
                        mVideoView.stopRecord();
                        mTimeView.setVisibility(View.INVISIBLE);
                    } else {
                        Intent intent = new Intent(ACTION_SIZE);
                        mContext.sendBroadcast(intent);
                        mVideoView.stopRecord();
                        mVideoView.startRecord();
                    }
                }
                mTimeView.setText(VideoUtils.time2String(duration));
                mTimeView.setVisibility(View.VISIBLE);
            } else if (msg.what == 2) {
                changeZoom.setVisibility(View.INVISIBLE);
                if (timer != null || task != null) {
                    timer.cancel();
                    task.cancel();
                }
                if (t != null || tasks != null) {
                    t.cancel();
                    tasks.cancel();
                }
            }else if(msg.what == 3){
                Intent intent = new Intent(ACTION_SIZE);
                mContext.sendBroadcast(intent);
                mVideoView.startRecord(); // 启动录像
                mTimeView.setVisibility(View.VISIBLE);
            }else if(msg.what == 4){
                mTimeView.setVisibility(View.INVISIBLE);
            }else if(msg.what==Config.SEND_MESSAGE_VISIBILITY_SEEKBAR){

                if (changeZoom.getVisibility() == View.VISIBLE) {
                    changeZoom.setVisibility(View.GONE);
                    timers = 0;
                    if (timer != null || task != null) {
                        timer.cancel();
                        task.cancel();
                    }

                }
            }
        }
    };


    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_SIZE);
        // 注册广播
        mContext.registerReceiver(MyReceiver, myIntentFilter);
    }

    private BroadcastReceiver MyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == ACTION_SIZE) {
                sizeView.setText(VideoUtils.getAvailableSizeData() + "G/"
                        + VideoUtils.getTotalSizeData() + "G");
            }
        }

    };

    private void updateViewPosition(int width, int height) {
        // 更新浮动窗口位置参数,x是鼠标在屏幕的位置，mTouchStartX是鼠标在图片的位置
        mWindowParams.x = 0;
        mWindowParams.y = 0;
        mWindowParams.width = width;
        mWindowParams.height = height;
        mWindowManager.updateViewLayout(this, mWindowParams);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mParentHandler != null) {
            Message msg = new Message();
            msg.what = Config.SERVICE_MESSAGE_CHANGE_ZOOM;
            Bundle bundle = new Bundle();
            bundle.putInt("progress", progress);
            msg.setData(bundle);
            mParentHandler.sendMessage(msg);
        }
        timers = 0;

        if (timer != null || task != null) {
            timer.cancel();
            task.cancel();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (timer != null || task != null) {
            timer.cancel();
            task.cancel();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                timers++;
                if (timers >= 3) {
                    mHandler.sendEmptyMessage(Config.SEND_MESSAGE_VISIBILITY_SEEKBAR);
                }
            }
        };
        timer.schedule(task, 1000, 1000);
    }
}
