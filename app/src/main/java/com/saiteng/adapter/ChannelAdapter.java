package com.saiteng.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Moore on 2017/8/10.
 */

public class ChannelAdapter extends BaseAdapter {

    private List<Map> mdata = new ArrayList<Map>();
    private LayoutInflater mInflater;
    private Context mcontext;
    private int defaultSelection=-1;
    private int switchtype=0;

    public ChannelAdapter(List<Map> data, Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mdata = data;
        this.mcontext = context;
    }
    @Override
    public int getCount() {
        return mdata.size();
    }

    @Override
    public Object getItem(int position) {
        return mdata.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mviewHolder = null;
        if(convertView==null){
            mviewHolder = new ViewHolder();
            convertView  = mInflater.inflate(R.layout.item_channelgridview, null);
            mviewHolder.channel_alias =(TextView)convertView.findViewById(R.id.channelname);
            mviewHolder.channel_num =(TextView)convertView.findViewById(R.id.channelnum);
            convertView.setTag(mviewHolder);
        }else{
            mviewHolder=(ViewHolder) convertView.getTag();
        }
        mviewHolder.channel_alias.setText((String) mdata.get(position).get("channelname"));
        mviewHolder.channel_num.setText(""+mdata.get(position).get("channelid"));
        if(switchtype==0){
            if (position == defaultSelection) {// 选中时设置
                convertView.setBackgroundResource(R.drawable.circle_choose_normal);
            } else {// 未选中时设置selector
                convertView.setBackgroundResource(R.drawable.circle_deep_normal);
            }
        }else
            convertView.setBackgroundResource(R.drawable.circle_deep_normal);

        if(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,"default").equals((String) mdata.get(position).get("channelname"))){
            convertView.setBackgroundResource(R.drawable.circle_choose_normal);
        }
        return convertView;
    }

    public void setSelectPosition(int position,int type) {
        if (!(position < 0 || position > mdata.size())) {
            defaultSelection = position;
            switchtype = type;
            notifyDataSetChanged();
        }
    }

    public class ViewHolder {
        public TextView channel_alias;
        public TextView channel_num;
    }
}
