package com.saiteng.Service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.saiteng.stptt.Config;


import static android.content.ContentValues.TAG;

/**
 * Created by Moore on 2017/7/25.
 */

public class AudioPlayService extends Service{
    private AudioTrack mAudioTrack = null;
    private PlayAudioThread mPlayAudioThread = null; // 播放线程
    private boolean mPlayThreadExitFlag = false; // 播放线程退出标志
    private int mMinPlayBufSize = 0;
    private boolean mAudioPlayReleased = false;
    private byte[] aacData;//需要解码的数据
    private byte[] pcmData;//解码后返回的数据
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
       // Config.DECODER_HANDLER=AudioCodec.create(Config.EASY_SDK_AUDIO_CODEC_AAC,Config.AUDIORECORD_SAMOLERATE,Config.AUDIORECORD_CHANNL,Config.AUDIORECORD_SAMPLEBIT);
        initAudioPlayer();
    }

    // 初始化音频播放器
    @SuppressWarnings("deprecation")
    public int initAudioPlayer() {
        if (mAudioTrack != null)
            return 0;
        int channel, samplerate, samplebit;
            samplerate = 44100;
            channel = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
            samplebit = AudioFormat.ENCODING_PCM_16BIT;

        try {
            mAudioPlayReleased = false;
            // 获得构建对象的最小缓冲区大小
            mMinPlayBufSize = AudioTrack.getMinBufferSize(samplerate, channel,
                    samplebit);
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, samplerate,
                    channel, samplebit, mMinPlayBufSize, AudioTrack.MODE_STREAM);

            if (mPlayAudioThread == null) {
                mPlayThreadExitFlag = false;
                mPlayAudioThread = new PlayAudioThread();
                mPlayAudioThread.start();
            }
            Log.d(TAG, "mMinPlayBufSize = " + mMinPlayBufSize);
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }



    // 销毁音频播放器
    public void releaseAudioPlayer() {
        if (mAudioPlayReleased)
            return;
        mAudioPlayReleased = true;
        Log.d(TAG, "releaseAudioPlayer");
        if (mPlayAudioThread != null) {
            mPlayThreadExitFlag = true;
            mPlayAudioThread = null;
        }

        if (mAudioTrack != null) {
            try {
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;
            } catch (Exception e) {

            }
        }
    }


    /*
	 * 音频播放线程
	 */
    class PlayAudioThread extends Thread {
        @Override
        public void run() {
            if (mAudioTrack == null)
                return;
            try {
                android.os.Process
                        .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            } catch (Exception e) {
            }
            try {
                mAudioTrack.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (!mPlayThreadExitFlag) {
                try {
                    byte[] data=null;//MPUCoreSDK.FetchAudioPlayBuffer(640);
                    if (data == null || data.length <= 0)
                        break;
                    mAudioTrack.write(data, 0, data.length);
                    //Log.d(TAG, "收到音频数据"+data.length+"字节");
                } catch (Exception e) {
                    break;
                }
            }
        }
    }
}
