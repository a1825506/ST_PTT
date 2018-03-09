package com.saiteng.audioHelper;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;

import com.saiteng.stptt.PathUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Moore on 2017/9/7.
 * 多媒体聊天语音消息录制类
 */

public class VoiceRecorder {
    MediaRecorder recorder;
    private String TAG="VoiceRecorder";
    static final String PREFIX = "voice";
    static final String EXTENSION = ".amr";
    private boolean isRecording = false;
    private long startTime;
    private String voiceFilePath = null;
    private String voiceFileName = null;
    private File file;
    private Handler handler;

    public VoiceRecorder(Handler var1) {
        this.handler = var1;
    }

    public String startRecording(String var1, String var2, Context var3) {
        if(this.recorder != null) {
            this.recorder.release();
            this.recorder = null;
        }
        try {
        this.recorder = new MediaRecorder();
        this.recorder.setAudioSource(1);
        this.recorder.setOutputFormat(3);
        this.recorder.setAudioEncoder(1);
        this.recorder.setAudioChannels(1);
        this.recorder.setAudioSamplingRate(8000);
        this.recorder.setAudioEncodingBitRate(64);
        this.voiceFileName = this.getVoiceFileName(var2);
        this.voiceFilePath = this.getVoiceFilePath();
        this.file = new File(this.voiceFilePath);
        this.recorder.setOutputFile(this.file.getAbsolutePath());
        this.recorder.prepare();
        this.isRecording = true;
        this.recorder.start();
            startTime = (new Date()).getTime();
        } catch (IOException e) {
            Log.e(TAG,"MediaRecorder 初始化失败");
            e.printStackTrace();
        }
        //录音时的动画效果
        (new Thread(new Runnable() {
            public void run() {
                while(true) {
                    try {
                        if(VoiceRecorder.this.isRecording) {
                            Message var1 = new Message();
                            var1.what = VoiceRecorder.this.recorder.getMaxAmplitude() * 13 / 32767;
                           VoiceRecorder.this.handler.sendMessage(var1);
                            SystemClock.sleep(100L);
                            continue;
                        }
                    } catch (Exception var2) {
                        Log.e("voice", var2.toString());
                    }
                    return;
                }
            }
        })).start();
        return null;
    }

    public int stopRecoding() {
        if(this.recorder != null) {
            this.isRecording = false;
            this.recorder.stop();
            this.recorder.release();
            this.recorder = null;
            if(this.file != null && this.file.exists() && this.file.isFile() && this.file.length() == 0L) {
                this.file.delete();
                return -1011;
            } else {
                int var1 = (int)((new Date()).getTime() - this.startTime) / 1000;
                Log.d("voice", "voice recording finished. seconds:" + var1 + " file length:" + this.file.length());
                return var1;
            }
        } else {
            return 0;
        }
    }

    public long fileLength(){
        if(this.file != null && this.file.exists() && this.file.isFile() && this.file.length() == 0L) {
            this.file.delete();
            return -1011;
        } else {
            return this.file.length();
        }

    }

    public String getVoiceFileName(String var1) {
        Time var2 = new Time();
        var2.setToNow();
        return var1 + var2.toString().substring(0, 15) + ".amr";
    }

    public String getVoiceFilePath() {
        return PathUtil.getInstance().getVoicePath() + "/" + this.voiceFileName;
    }
}
