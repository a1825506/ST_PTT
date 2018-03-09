package com.saiteng.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saiteng.adapter.GroupAdapter;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MainPTTActivity;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.R;
import com.lidroid.xutils.ViewUtils;
import com.saiteng.stptt.Utils;
import com.saiteng.user.ChannelInfo;
import com.saiteng.user.UserInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Moore on 2017/7/31.
 */

public class GroupsFragment extends Fragment implements View.OnClickListener{

    private View view = null;
    private Button channel_group,all_group;
    private ImageView group_call,group_talk,group_exit;
    private LinearLayout group_title,temp_group;
    private ListView listview;
    private GroupAdapter gropAdapter;
    private List<Map> data = new ArrayList<Map>();
    private UserInfo userinfo;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private int width, height;
    private IntentFilter intentFilter;
    private VisibleReceiver visibleReceiver;
    String groupname="";
    String channelname="";//初始的临时会话名为所有成员的别名

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.fragment_group, (ViewGroup) getActivity().findViewById(R.id.study_viewpager), false);
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        intentFilter = new IntentFilter();
        visibleReceiver = new VisibleReceiver();
        intentFilter.addAction(Config.BOARDCAST_INVISIBLE);
        intentFilter.addAction(Config.BOARDCAST_VISIBLE);
        intentFilter.addAction(Config.BOARDCAST_SWITCHCHANNEL);
        MyPTTApplication.getInstance().getLocalBroadcastManager().registerReceiver(visibleReceiver, intentFilter);
        findview();
        setAdapter();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 这句话必须加
        ViewGroup p = (ViewGroup) view.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        ViewUtils.inject(this, view);

        return view;
    }


    private void setAdapter() {
        setVisible(false);
        data.clear();
        data = get_groupData();
        gropAdapter = new GroupAdapter(data,getContext());
        listview.setAdapter(gropAdapter);
    }

    private void findview() {
        group_title = (LinearLayout)view.findViewById(R.id.group_title);
        channel_group = (Button)view.findViewById(R.id.channel_group);
        all_group = (Button)view.findViewById(R.id.all_group);
        listview = (ListView) view.findViewById(R.id.group_listview);
        group_call = (ImageView)view.findViewById(R.id.group_call);
        group_talk = (ImageView)view.findViewById(R.id.group_talk);
        group_exit = (ImageView)view.findViewById(R.id.group_exit);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)(width*0.8)
                ,RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin=10;
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        group_title.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
                ,(int)(height*0.6));
        layoutParams.addRule(RelativeLayout.BELOW, R.id.group_title);
        listview.setLayoutParams(layoutParams);

        channel_group.setOnClickListener(this);
        all_group.setOnClickListener(this);
        group_call.setOnClickListener(this);
        group_talk.setOnClickListener(this);

        group_exit.setOnClickListener(this);

    }


    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case Config.VISIBLE:
                    setVisible(true);
                    break;
                case Config.INVISIBLE:
                    setVisible(false);
                    break;
            }
        }
    };

    private void setVisible(boolean b) {
        if(b){
            group_call.setVisibility(View.VISIBLE);
            group_talk.setVisibility(View.VISIBLE);
            group_exit.setVisibility(View.VISIBLE);
        }else{
            group_call.setVisibility(View.GONE);
            group_talk.setVisibility(View.GONE);
            group_exit.setVisibility(View.GONE);
        }

    }

    //test1-测试1-123456-0-2-2-test_group1:test_group2
    private List<Map> get_groupData() {
        Map<String,Object> map=null;
        String json=MyPTTApplication.getInstance().getSharedTools().getShareObject("user",null);
        if (json != null)
        {
            Gson gson = new Gson();
            Type type = new TypeToken<List<UserInfo>>(){}.getType();
            List<UserInfo> list_user = new ArrayList<UserInfo>();
            list_user = gson.fromJson(json, type);
            for(int i=0;i< list_user.size();i++){
                userinfo=list_user.get(i);
                String user_group = MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,"default");
                String usergroup = userinfo.getGroup().toString();
                if(usergroup.contains(user_group)){
                    //如果当前用户在当前的频道中
                    map = new HashMap<String,Object>();
                    map.put("username",userinfo.getUsername());
                    map.put("userid",userinfo.getUserid());
                    map.put("online",userinfo.getOnline());
                    data.add(map);
                    userinfo=null;
                }
            }
        }
        return data;
    }
    private List<Map> get_allData() {
        Map<String,Object> map=null;
        String json=MyPTTApplication.getInstance().getSharedTools().getShareObject("user",null);
        if(json!=null){
            Gson gson = new Gson();
            Type type = new TypeToken<List<UserInfo>>(){}.getType();
            List<UserInfo> list_user = new ArrayList<UserInfo>();
            list_user = gson.fromJson(json, type);
            for(int i=0;i< list_user.size();i++){
                userinfo=list_user.get(i);
                map = new HashMap<String,Object>();
                map.put("username",userinfo.getUsername());
                map.put("userid",userinfo.getUserid());
                map.put("online",userinfo.getOnline());
                data.add(map);
                userinfo=null;
            }
        }
        return data;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.channel_group:
                channel_group.setBackgroundResource(R.drawable.circle_choose_normal);
                all_group.setBackgroundResource(R.drawable.circle_normal);
                data.clear();
                data = get_groupData();
                //notifyDataSetChanged更新时List<Map> data 不能被重新new
                gropAdapter.notifyDataSetChanged();
                break;
            case R.id.all_group:
                channel_group.setBackgroundResource(R.drawable.circle_normal);
                all_group.setBackgroundResource(R.drawable.circle_choose_normal);
                data.clear();
                data = get_allData();
                gropAdapter.notifyDataSetChanged();

                break;
            case R.id.group_call:
                //发起临时会话，获取勾选的成员的用户名
                createTempTalkOrder();
                intent = new Intent(Config.BOARDCAST_TEMPTALK);
                intent.putExtra("tempGrouplist",channelname);
                intent.putExtra("tempchannelname",groupname);
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                groupname="";
                channelname="";
                break;
            case R.id.group_talk:
                //切换到聊天界面
                createTempTalkOrder();
                intent = new Intent(Config.BOARDCAST_SWITCHTOCHAT);
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                Config.CurrentChannel = channelname;
                channelname="";
                break;
            case R.id.group_exit:
                GroupAdapter.map.clear();
                gropAdapter.notifyDataSetChanged();
                break;
        }
    }
    /**
     * 创建临时群组的命令
     * */
    private void createTempTalkOrder() {

        groupname="";
        channelname="";
        for(Integer position:GroupAdapter.map.keySet()){
             groupname+=data.get(position).get("username")+":";
            channelname+=data.get(position).get("username")+"-";
        }
        groupname+=MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,"");
        channelname+=MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,"");
    }

    class VisibleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction()==Config.BOARDCAST_VISIBLE){
                mhandler.sendEmptyMessage(Config.VISIBLE);
            }else if(intent.getAction()==Config.BOARDCAST_INVISIBLE){
                mhandler.sendEmptyMessage(Config.INVISIBLE);
            }else if(intent.getAction()==Config.BOARDCAST_SWITCHCHANNEL){
                setAdapter();
            }

        }
    }
}
