package com.saiteng.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Moore on 2017/8/8.
 */

//test1-测试1-123456-0-2-2-test_group1:test_group2

public class GroupAdapter extends BaseAdapter {

    private List<Map> mdata = new ArrayList<Map>();
    private LayoutInflater mInflater;
    private Context mcontext;
    public static Map<Integer,Boolean> map=new HashMap<>();// 存放已被选中的CheckBox
    public GroupAdapter(List<Map> data,Context context) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
         ViewHolder mviewHolder = null;
        if(convertView==null){
            mviewHolder = new ViewHolder();
            convertView  = mInflater.inflate(R.layout.item_grouplistview, null);
            mviewHolder.group_online_offline =(ImageView) convertView.findViewById(R.id.group_online_offline);
            mviewHolder.group_alias =(TextView)convertView.findViewById(R.id.group_alias);
            mviewHolder.group_name =(TextView)convertView.findViewById(R.id.group_name);
            mviewHolder.group_own =(ImageView) convertView.findViewById(R.id.group_own);
            mviewHolder.group_check = (CheckBox) convertView.findViewById(R.id.group_check);
            convertView.setTag(mviewHolder);
        }else{
            mviewHolder=(ViewHolder) convertView.getTag();
        }
        mviewHolder.group_online_offline.setBackgroundResource(R.mipmap.offline);
        mviewHolder.group_alias.setText(""+mdata.get(position).get("userid"));
        mviewHolder.group_name.setText((String) mdata.get(position).get("username"));
        mviewHolder.group_own.setBackgroundResource(R.mipmap.admin);
        mviewHolder.group_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 Intent intent;
                if(isChecked){
                    map.put(position,true);
                }else{
                    map.remove(position);
                }
                if(map.size()!=0){
                    intent = new Intent(Config.BOARDCAST_VISIBLE);
                }else {
                    intent = new Intent(Config.BOARDCAST_INVISIBLE);
                }
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
            }
        });
        if(map!=null&&map.containsKey(position)){
            mviewHolder.group_check.setChecked(true);
        }else {
            mviewHolder.group_check.setChecked(false);
        }
        return convertView;
    }

    public void update(){
        notifyDataSetChanged();
    }

    public class ViewHolder {
        public ImageView group_online_offline;
        public TextView group_alias;
        public TextView group_name;
        public ImageView group_own;
        public CheckBox group_check;
    }
}
