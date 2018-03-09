package com.saiteng.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;
import android.widget.TextView;

/**
 * Created by Moore on 2017/8/11.
 * TextView自带了跑马灯功能，
 * 只要把它的ellipsize属性设置为marquee就可以了。
 * 但有个前提，就是TextView要处于被选中状态才能有效果
 * 因此 自定义一个textview 重写isFocused方法，让其一直返回true
 *
 */

public class MarqueeTextView extends AppCompatTextView {

    public MarqueeTextView(Context con) {
        super(con);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        // TODO Auto-generated method stub
        return true;

    }
}
