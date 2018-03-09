package com.videotakepicture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Moore on 2018-01-26.
 */

public class VideoMainActivity extends Activity {

    private Context mContext;

    private videoService mvideoservice;

    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);//关键代码
        VideoUtils.createDirectory2Store(this);
        VideoUtils.createFilePath(this);

        mContext = getApplicationContext();

        mvideoservice = videoService.getInstance(mContext);

        if (mvideoservice != null) {

            this.finish();
        }
        Intent recordFloatViewService = new Intent(mContext,
                videoService.class);

        mContext.startService(recordFloatViewService);

        finish();

    }
}
