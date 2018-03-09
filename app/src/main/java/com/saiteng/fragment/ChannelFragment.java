package com.saiteng.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.ViewUtils;
import com.saiteng.adapter.ChannelAdapter;
import com.saiteng.adapter.TempChannelAdapter;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.R;
import com.saiteng.user.ChannelInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Moore on 2017/7/31.
 */

public class ChannelFragment extends Fragment{
    private View view = null;
    private TextView channnel_title,tempchannnel_title;
    private GridView channel_gridview,tempchannel_gridview;
    private LinearLayout channel_title_area,tempchannel_title_area;
    private ChannelAdapter gropAdapter;
    private TempChannelAdapter tempgropAdapter;
    private ChannelInfo channelinfo;
    private List<Map> data = new ArrayList<Map>();
    private List<Map> temp_data = new ArrayList<Map>();
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private int width, height;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private int switchType=-1;//当前切换的类型，0 : 常规频道   1: 临时频道


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.fragment_channel, (ViewGroup) getActivity().findViewById(R.id.study_viewpager), false);
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        intentFilter = new IntentFilter();
        localReceiver = new LocalReceiver();
        intentFilter.addAction(Config.BOARDCAST_SWITCHCHANNEL);
        intentFilter.addAction(Config.BOARDCAST_TEMPTALK_RECE);
        MyPTTApplication.getInstance().getLocalBroadcastManager().registerReceiver(localReceiver, intentFilter);
        findView();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // 这句话必须加
        ViewGroup p = (ViewGroup) view.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        ViewUtils.inject(this, view);
        setAdapter();
        return view;
    }

    private void findView() {
        channnel_title = (TextView) view.findViewById(R.id.channel_title);
        channel_gridview = (GridView) view.findViewById(R.id.channel_gridview);
        tempchannnel_title = (TextView) view.findViewById(R.id.tempchannel_title);
        tempchannel_gridview = (GridView) view.findViewById(R.id.tempchannel_gridview);
        channel_title_area = (LinearLayout) view.findViewById(R.id.channel_title_area);
        tempchannel_title_area = (LinearLayout) view.findViewById(R.id.tempchannel_title_area);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)(width*0.95)
                ,(int)(height*0.05));
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        channel_title_area.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
                ,(int)(height*0.3));
        layoutParams.addRule(RelativeLayout.BELOW,R.id.channel_title_area);
        channel_gridview.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams((int)(width*0.95)
                ,(int)(height*0.05));
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.BELOW,R.id.channel_gridview);
        tempchannel_title_area.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
                ,(int)(height*0.3));
        layoutParams.addRule(RelativeLayout.BELOW,R.id.tempchannel_title_area);
        tempchannel_gridview.setLayoutParams(layoutParams);

        channel_gridview.setOnItemClickListener(new MyItemOnclickListener());
        tempchannel_gridview.setOnItemClickListener(new MyTempItemOnclickListener());
        tempchannel_gridview.setOnItemLongClickListener(new ChooseDialog());
    }

    private void setAdapter() {
        data = get_channelData();
        gropAdapter = new ChannelAdapter(data,getContext());
        channel_gridview.setAdapter(gropAdapter);

        temp_data = get_tempChannelData();
        tempgropAdapter = new TempChannelAdapter(temp_data,getContext());
        tempchannel_gridview.setAdapter(tempgropAdapter);

    }

    public List<Map> get_tempChannelData() {
        List<Map> group_data  = new ArrayList<Map>();
        Map<String,Object> map=null;
        String json=MyPTTApplication.getInstance().getSharedTools().getShareObject("channel",null);
        if (json != null)
        {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ChannelInfo>>(){}.getType();
            List<ChannelInfo> list_channel = new ArrayList<ChannelInfo>();
            list_channel = gson.fromJson(json, type);
            for(int i = 0; i< list_channel.size(); i++){
                channelinfo=list_channel.get(i);
                if(channelinfo.getChannelidtype()==0){
                    if(channelinfo.getChannelname().contains(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,""))){
                        map = new HashMap<String,Object>();
                        map.put("channelname",channelinfo.getChannelname());
                        map.put("channelid",channelinfo.getChannelid());
                        group_data.add(map);
                    }
                }
            }
        }
        return group_data;
    }

    private List<Map> get_channelData() {
        List<Map> group_data  = new ArrayList<Map>();
        Map<String,Object> map=null;
        String json=MyPTTApplication.getInstance().getSharedTools().getShareObject("channel",null);
        if (json != null)
        {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ChannelInfo>>(){}.getType();
            List<ChannelInfo> list_channel = new ArrayList<ChannelInfo>();
            list_channel = gson.fromJson(json, type);
            for(int i = 0; i< list_channel.size(); i++){
                channelinfo=list_channel.get(i);
                if(channelinfo.getChannelidtype()==1){
                    if(channelinfo.getChannelname().equals(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,"default"))){
                        //设置当前的频道ID。
                        MyPTTApplication.getInstance().getSharedTools().setShareInt(Config.CurrentChannelID,channelinfo.getChannelid());
                        MyPTTApplication.getInstance().getSharedTools().setShareString(Config.CurrentChannel,channelinfo.getChannelname());
                    }
                    map = new HashMap<String,Object>();
                    map.put("channelname",channelinfo.getChannelname());
                    map.put("channelid",channelinfo.getChannelid());
                    group_data.add(map);
                }
            }
        }
        return group_data;
    }
    /**
     * 由临时频道名找到临时频道的ID
     */
    public int get_tempChannelID(String tempChannelname){
        String json=MyPTTApplication.getInstance().getSharedTools().getShareObject("channel",null);
        if (json != null)
        {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ChannelInfo>>(){}.getType();
            List<ChannelInfo> list_channel = new ArrayList<ChannelInfo>();
            list_channel = gson.fromJson(json, type);
            for(int i = 0; i< list_channel.size(); i++){
                channelinfo=list_channel.get(i);
                if(channelinfo.getChannelidtype()==0){
                    if(channelinfo.getChannelname().equals(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,""))){
                       Config.choosetempChannelID = channelinfo.getChannelid();
                    }
                }
            }
        }
        return 0;
    }


    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Config.SWITCHCHANNEL:
                    if(switchType==0){
                        gropAdapter.setSelectPosition(Config.position,0);
                        tempgropAdapter.setSelectPosition(Config.position,1);
                    }else{
                        gropAdapter.setSelectPosition(Config.position,1);
                        tempgropAdapter.setSelectPosition(Config.position,0);
                    }
                    break;
                case Config.UPDATE_TEMPCHANNEL:
                    temp_data = get_tempChannelData();
                    tempgropAdapter = new TempChannelAdapter(temp_data,getContext());
                    tempchannel_gridview.setAdapter(tempgropAdapter);
                    break;
            }
        }
    };

    class ChooseDialog implements android.widget.AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
            String tempChannelName = (String) temp_data.get(arg2).get("channelname");
            get_tempChannelID(tempChannelName);
            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
            String[] items={"重命名","退出临时会话"};
            builder.setItems(items, new ItemLongMonitor(arg2));
            builder.create().show();
            return true;
        }
    }
    class ItemLongMonitor implements DialogInterface.OnClickListener{
        private int pos=0;
        public ItemLongMonitor(int pos){
            this.pos=pos;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which){
                case 0:
                    ChooseDialog(0,pos);
                    break;
                case 1:
                    ChooseDialog(1,pos);
                    break;
            }
            dialog.dismiss();
        }
    }
    private void ChooseDialog(final int id,final int pos){
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.create();
        switch(id){
            case 0:
                builder.setTitle("输入新的名称");
                builder.setView(new EditText(getContext()));
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Config.BOARDCAST_RENAMETEMPCHANNEL);
                        MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                    }
                });
                builder.setNegativeButton("取消",null);
                break;
            case 1:
                builder.setTitle("确认退出临时会话？");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Config.BOARDCAST_EXITTEMPCHANNEL);
                        MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                    }
                });
                builder.setNegativeButton("取消",null);
                break;
        }
        builder.show();
    }
    public class MyItemOnclickListener implements AdapterView.OnItemClickListener{
        //切换编组
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Config.switchChannelID  = (int)data.get(position).get("channelid");
            Config.switchChannel = (String)data.get(position).get("channelname");
            Config.position = position;
           // Config.positiontemp=-1;
            switchType=0;
            //发送切换编组的命令
            Intent intent = new Intent(Config.BOARDCAST_SWITCHCHANNEL_QUE);
            MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
        }
    }
    public class MyTempItemOnclickListener implements AdapterView.OnItemClickListener{
        //切换临时编组
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Config.switchChannelID  = (int)temp_data.get(position).get("channelid");
            Config.switchChannel = (String)temp_data.get(position).get("channelname");
            Config.position = position;
            //Config.position=-1;
            switchType=1;
            //发送切换编组的命令
            Intent intent = new Intent(Config.BOARDCAST_SWITCHCHANNEL_QUE);
            MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
        }
    }
    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction()==Config.BOARDCAST_SWITCHCHANNEL){
                mhandler.sendEmptyMessage(Config.SWITCHCHANNEL);
            }else if(intent.getAction()==Config.BOARDCAST_TEMPTALK_RECE){
                mhandler.sendEmptyMessage(Config.UPDATE_TEMPCHANNEL);
            }
        }
    }
}
