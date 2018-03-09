package com.saiteng.stptt;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Created by Moore on 2018-03-07.
 */

public class RecordView  extends RelativeLayout {
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private LayoutParams mVideoViewParams;
    private View viewRecordFrame;
    private RelativeLayout viewVideoView;
    public RecordView(Context context, WindowManager wm,
                      WindowManager.LayoutParams wmParams) {
        super(context);
        mWindowManager = wm;
        mWindowParams = wmParams;
        viewRecordFrame = View.inflate(context, R.layout.record_view1, null);
        this.addView(viewRecordFrame);
    }
}
