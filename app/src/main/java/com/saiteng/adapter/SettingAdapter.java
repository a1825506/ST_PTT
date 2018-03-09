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
 * Created by Moore on 2017/8/16.
 */

public class SettingAdapter extends BaseAdapter {
    private List<Map> mdata = new ArrayList<Map>();
    private LayoutInflater mInflater;
    private Context mcontext;

    public SettingAdapter(List<Map> data, Context context){
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
            convertView  = mInflater.inflate(R.layout.item_setting_user, null);
            mviewHolder.user_name =(TextView)convertView.findViewById(R.id.user_name);
            mviewHolder.user_id =(TextView)convertView.findViewById(R.id.user_id);
            convertView.setTag(mviewHolder);
        }else{
            mviewHolder=(ViewHolder) convertView.getTag();
        }

            mviewHolder.user_name.setText((String) mdata.get(position).get("user_name"));
            mviewHolder.user_id.setText(""+mdata.get(position).get("user_id"));

        return convertView;
    }

    private View getItemView(int position) {
        View convertView=null;
        if(position==0){
            convertView  = mInflater.inflate(R.layout.item_setting_user, null);
        }else if(position==1){
            convertView  = mInflater.inflate(R.layout.item_setting_audio, null);
        }

        return convertView;
    }

    public class ViewHolder {
        public TextView user_name;
        public TextView user_id;
        public TextView audio_set;
    }
}
