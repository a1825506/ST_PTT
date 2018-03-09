package com.saiteng.audioHelper;


import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.saiteng.stptt.Config;
import com.saiteng.stptt.Utils;

/**
 * 音频采集类
 *
 */

public class AudioRecorder implements Runnable {

    String LOG = "Recorder ";

    private boolean isRecording = false;
    private AudioRecord audioRecord;

    private int audioBufSize = 0;

    /*
     * start recording
     */
    public void startRecording() {
        audioBufSize = AudioRecord.getMinBufferSize(Config.AUDIORECORD_SAMOLERATE, Config.AUDIORECORD_CHANNL,
                Config.AUDIORECORD_SAMPLEBIT);
        if (null == audioRecord) {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    Config.AUDIORECORD_SAMOLERATE, Config.AUDIORECORD_CHANNL, Config.AUDIORECORD_SAMPLEBIT, audioBufSize);
        }
        new Thread(this).start();
    }

    /*
     * stop
     */
    public void stopRecording() {
        this.isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void run() {
        // start encoder before recording
        AudioEncoder encoder = AudioEncoder.getInstance();
        encoder.startEncoding();
        audioRecord.startRecording();
        Log.e(LOG,"start recording");
        this.isRecording = true;
        int size =160;
        short[] samples = new short[160];
        while (isRecording) {
            int bufferRead = audioRecord.read(samples, 0, size);
            if (bufferRead > 0) {
                encoder.addData(samples, size);
            }
        }
        Log.e(LOG,"end recording");
        audioRecord.stop();
        encoder.stopEncoding();
    }
}
