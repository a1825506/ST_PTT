package com.saiteng.stptt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Moore on 2017/7/5.
 */

public class ChannelActivity extends Activity{
    private ImageView channel_up,Img_channel_lock,Img_setting;
    private TextView text_channel;
    private LinearLayout layout_channelInfo;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private int width, height;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        setContentView(R.layout.activity_channel);
        context = getApplicationContext();
        findView();
    }
    private void findView() {
        channel_up = (ImageView)findViewById(R.id.channel_up);
        Img_channel_lock = (ImageView)findViewById(R.id.channel_lock);
        Img_setting = (ImageView)findViewById(R.id.setting);
        text_channel = (TextView)findViewById(R.id.channel_name);
        Img_channel_lock.setVisibility(View.INVISIBLE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.topMargin=(int)(height*0.85);
        channel_up.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin=(int)(width*0.85);
        Img_setting.setLayoutParams(params);






        channel_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ChannelActivity.this, MainPTTActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.out_to_up, R.anim.in_from_down);
            }
        });
    }
}
