package com.saiteng.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.saiteng.stptt.R;

/**
 * Created by Moore on 2017/8/9.
 */

public class TalkDialog extends Dialog implements View.OnClickListener{
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private View talkview;
    private Context mContext = null;
    private TextView title;
    private TextView status;
    private Button cannel;
    public TalkDialog(Context context,WindowManager wm,
                      WindowManager.LayoutParams wmParams) {
        super(context);
        mContext = context;
        mWindowManager = wm;
        mWindowParams = wmParams;
        talkview = View.inflate(context, R.layout.dialog_normal, null);
        title = (TextView) talkview.findViewById(R.id.dialog_title);
        status = (TextView)talkview.findViewById(R.id.dialog_status);
        cannel = (Button)talkview.findViewById(R.id.btn_dialog_cancel);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_dialog_cancel){

        }
    }
}
