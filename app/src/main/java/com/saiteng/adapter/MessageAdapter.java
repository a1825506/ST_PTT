package com.saiteng.adapter;


import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.saiteng.audioHelper.VoicePlayClickListener;
import com.saiteng.fragment.ChatFragment;
import com.saiteng.picHelper.ImageCache;
import com.saiteng.picHelper.ImageUtils;
import com.saiteng.stptt.MainPTTActivity;
import com.saiteng.stptt.R;
import com.saiteng.stptt.ShareLocationActivity;
import com.saiteng.stptt.ShowBigImage;
import com.saiteng.stptt.ShowVideoActivity;
import com.saiteng.task.LoadImageTask;
import com.saiteng.task.LoadVideoImageTask;
import com.saiteng.user.MessageInfo;

import static com.saiteng.stptt.MainPTTActivity.activity;

public class MessageAdapter extends BaseAdapter {

    private final static String TAG = "msg";

    private LayoutInflater inflater;
    private Context context;
    private List<MessageInfo> data = new ArrayList<MessageInfo>();
    private Map<String, Timer> timers = new Hashtable<String, Timer>();

    private MessageInfo messageInfo;

    private final int SUCCESS = 0;//发送完成
    private final int FAIL = -1;//发送失败
    private final int INPROGRESS = 1;//发送中

    public MessageAdapter(Context context, List<MessageInfo> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    /**
     * 获取item数
     */
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        notifyDataSetChanged();
    }


    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取item类型，文本，图片，视频，语音，位置信息等
     */
    public int getItemViewType(int position) {

        return -1;
    }

    public int getViewTypeCount() {
        return 14;
    }

    //根据接收到的消息类型创建不同的view
    private View createViewByMessage(int position) {
        switch (messageInfo.getMessage_Type()) {
        case ChatFragment.MESSAGE_TXT_TYPE:
            return messageInfo.getMessage_Direct() == ChatFragment.MESSAGE_RECE ? inflater
                    .inflate(R.layout.row_received_message, null) : inflater
                    .inflate(R.layout.row_sent_message, null);
            case ChatFragment.MESSAGE_GPS_TYPE:
                return messageInfo.getMessage_Direct() ==  ChatFragment.MESSAGE_RECE ? inflater
                        .inflate(R.layout.row_received_location, null) : inflater
                        .inflate(R.layout.row_sent_location, null);

        case ChatFragment.MESSAGE_PIC_TYPE:
            return messageInfo.message_Direct == ChatFragment.MESSAGE_RECE ? inflater
                    .inflate(R.layout.row_received_picture, null) : inflater
                    .inflate(R.layout.row_sent_picture, null);
        case ChatFragment.MESSAGE_AUDIO_TYPE:
            return messageInfo.getMessage_Direct() ==ChatFragment.MESSAGE_RECE  ? inflater
                    .inflate(R.layout.row_received_voice, null) : inflater
                    .inflate(R.layout.row_sent_voice, null);
        case ChatFragment.MESSAGE_VIDEO_TYPE:
            return messageInfo.getMessage_Direct() == ChatFragment.MESSAGE_RECE ? inflater
                    .inflate(R.layout.row_received_video, null) : inflater
                    .inflate(R.layout.row_sent_video, null);
//        case FILE:
//            return message.direct == Direct.RECEIVE ? inflater
//                    .inflate(R.layout.row_received_file, null) : inflater
//                    .inflate(R.layout.row_sent_file, null);null

//        default:
//            // 语音电话
//            if (message.getBooleanAttribute(
//                    Config.MESSAGE_ATTR_IS_VOICE_CALL, false))
//                return message.direct == Direct.RECEIVE ? inflater
//                        .inflate(R.layout.row_received_voice_call, null)
//                        : inflater.inflate(R.layout.row_sent_voice_call, null);
//            return message.direct == Direct.RECEIVE ? inflater
//                    .inflate(R.layout.row_received_message, null) : inflater
//                    .inflate(R.layout.row_sent_message, null);
        }
        return null;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        messageInfo = data.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByMessage(position);
            if (messageInfo.getMessage_Type()==ChatFragment.MESSAGE_TXT_TYPE) {
                try {
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    // 这里是文字内容
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.tv_chatcontent);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }
            }else if(messageInfo.getMessage_Type() == ChatFragment.MESSAGE_AUDIO_TYPE){
                try {
                    holder.iv = ((ImageView) convertView
                            .findViewById(R.id.iv_voice));
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.tv_length);
//                    holder.pb = (ProgressBar) convertView
//                            .findViewById(R.id.pb_sending);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    holder.iv_read_status = (ImageView) convertView
                            .findViewById(R.id.iv_unread_voice);
                } catch (Exception e) {
                }
            }else if(messageInfo.getMessage_Type() == ChatFragment.MESSAGE_GPS_TYPE){
                try {
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.tv_location);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }
            }else if(messageInfo.getMessage_Type() == ChatFragment.MESSAGE_PIC_TYPE){
                try {
                    holder.iv = ((ImageView) convertView
                            .findViewById(R.id.iv_sendPicture));
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.percentage);

                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }
            }else if(messageInfo.getMessage_Type() == ChatFragment.MESSAGE_VIDEO_TYPE){
                try {
                    holder.iv = ((ImageView) convertView
                            .findViewById(R.id.chatting_content_iv));
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.percentage);

                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.size = (TextView) convertView
                            .findViewById(R.id.chatting_size_iv);
                    holder.timeLength = (TextView) convertView
                            .findViewById(R.id.chatting_length_iv);
                    holder.playBtn = (ImageView) convertView
                            .findViewById(R.id.chatting_status_btn);
                    holder.container_status_btn = (LinearLayout) convertView
                            .findViewById(R.id.container_status_btn);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);

                } catch (Exception e) {
                }

            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
            switch (messageInfo.getMessage_Type()) {
            // 根据消息type显示item
            case ChatFragment.MESSAGE_TXT_TYPE: // 文本
                handleTextMessage(holder, position);
                break;
            case ChatFragment.MESSAGE_AUDIO_TYPE://语音
                handleVoiceMessage(holder, position, convertView);
                break;
            case ChatFragment.MESSAGE_GPS_TYPE: // 位置
                 handleLocationMessage(holder, position, convertView);
                 break;
             case ChatFragment.MESSAGE_PIC_TYPE: // 图片
                  handleImageMessage(holder, convertView);
                  break;
             case ChatFragment.MESSAGE_VIDEO_TYPE://视频
                 handleVideoMessage(holder, position, convertView);
            }
            return convertView;
    }

    public static class ViewHolder {
        ImageView iv;
        TextView tv;

        ImageView staus_iv;
        ImageView head_iv;
        TextView tv_userId;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;

        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;
    }


    /**
     * 文本消息
     * @param holder
     * @param position
     */
    private void handleTextMessage(ViewHolder holder,
                                   final int position) {
        // 设置内容
        holder.tv.setText(messageInfo.getMessageContext());
        if(messageInfo.getMessage_Direct()==ChatFragment.MESSAGE_RECE){
            holder.tv_userId.setText(""+messageInfo.getMessage_senderID());
        }
    }

    /**
     * 语音消息
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleVoiceMessage(
                                    final ViewHolder holder, final int position, View convertView) {

        holder.tv.setText(messageInfo.getMessage_length()+"");

        //点击进行播放
        holder.iv.setOnClickListener(new VoicePlayClickListener(messageInfo,
                holder.iv, holder.iv_read_status, this));
            if (messageInfo.getMessage_Direct() == ChatFragment.MESSAGE_RECE){
                holder.iv.setImageResource(R.mipmap.chatfrom_voice_playing);
            } else {
                holder.iv.setImageResource(R.mipmap.chatto_voice_playing);
            }
        if(messageInfo.getMessage_Direct()==ChatFragment.MESSAGE_RECE){
            holder.tv_userId.setText(""+messageInfo.getMessage_senderID());
        }

    }

    /**
     * 处理位置消息
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleLocationMessage(
                                       final ViewHolder holder, final int position, View convertView) {
        TextView locationView = ((TextView) convertView
                .findViewById(R.id.tv_location));
        String[] Locationinfo = messageInfo.getMessageContext().split(":");
        locationView.setText(Locationinfo[2]);
        LatLng loc = new LatLng(Double.valueOf(Locationinfo[0]), Double.valueOf(Locationinfo[1]));
        locationView.setOnClickListener(new MapClickListener(loc,Locationinfo[2]));
        if(messageInfo.getMessage_Direct()==ChatFragment.MESSAGE_RECE){
            holder.tv_userId.setText(""+messageInfo.getMessage_senderID());
        }

    }

    /*
     * 点击地图消息listener
     */
    class MapClickListener implements View.OnClickListener {

        LatLng location;
        String address;

        public MapClickListener(LatLng loc, String address) {
            location = loc;
            this.address = address;

        }

        @Override
        public void onClick(View v) {
            Intent intent;
            intent = new Intent(context, ShareLocationActivity.class);
            intent.putExtra("latitude", location.latitude);
            intent.putExtra("longitude", location.longitude);
            intent.putExtra("address", address);
            MainPTTActivity.getActivity().startActivity(intent);
        }

    }

    /**
     * 图片消息
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleImageMessage(final ViewHolder holder, View convertView) {

        // 接收方向的消息
        if(messageInfo.getMessage_Direct()==ChatFragment.MESSAGE_RECE){
            holder.tv_userId.setText(""+messageInfo.getMessage_senderID());
        }
      {
            // 发送的消息
            String filePath = messageInfo.getPath();
            if (filePath != null && new File(filePath).exists()) {
                showImageView(ImageUtils.getThumbnailImagePath(filePath),
                        holder.iv, filePath, null);
            }
        }
    }

    /**
     * 图片缩略展示
     *
     * @param thumbernailPath
     * @param iv
     * @return the image exists or not
     */
    private boolean showImageView(final String thumbernailPath,
                                  final ImageView iv, final String localFullSizePath,
                                  String remoteDir) {

        final String remote = remoteDir;
        Bitmap bitmap = ImageCache.getInstance().get(thumbernailPath);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);
            iv.setClickable(true);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.err.println("image view on click");
                    Intent intent = new Intent(activity, ShowBigImage.class);
                    File file = new File(localFullSizePath);
                    if (file.exists()) {
                        Uri uri = Uri.fromFile(file);
                        intent.putExtra("uri", uri);
                        System.err
                                .println("here need to check why download everytime");
                    }
                    activity.startActivity(intent);
                }
            });
            return true;
        }else{
            new LoadImageTask().execute(thumbernailPath, localFullSizePath,
                    remote,iv,MainPTTActivity.getActivity());
        }
        return false;
    }

    /**
     * 视频消息
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleVideoMessage(final ViewHolder holder, final int position, View convertView) {


        // final File image=new File(PathUtil.getInstance().getVideoPath(),
        // videoBody.getFileName());
        String localThumb = messageInfo.getPath();


        if (localThumb != null) {

            showVideoThumbView(localThumb, holder.iv);
        }
        if (messageInfo.getMessage_length() > 0) {
            String time = ""+messageInfo.getMessage_length();
            holder.timeLength.setText(time);
        }
        holder.playBtn.setImageResource(R.mipmap.video_download_btn_nor);

        if (messageInfo.getMessage_Direct()== ChatFragment.MESSAGE_RECE) {
            if(messageInfo.getMessage_Direct()==ChatFragment.MESSAGE_RECE){
                holder.tv_userId.setText(""+messageInfo.getMessage_senderID());
            }
            if (messageInfo.getMessage_length() > 0) {
                String size = messageInfo.getMessage_length()+"";
                holder.size.setText(size);
            }
        } else {

                String size ="";
                holder.size.setText(size);
            }


//        if(messageInfo.getMessage_Direct()== ChatFragment.MESSAGE_RECE){
//
//            // System.err.println("it is receive msg");
//            if (messageInfo.getSendstatus() == ChatFragment..Status.INPROGRESS) {
//                // System.err.println("!!!! back receive");
//                holder.iv.setImageResource(R.mipmap.default_image);
//                showDownloadImageProgress(message, holder);
//
//            } else {
//                // System.err.println("!!!! not back receive, show image directly");
//                holder.iv.setImageResource(R.drawable.default_image);
//                if (localThumb != null) {
//                    showVideoThumbView(localThumb, holder.iv,
//                            videoBody.getThumbnailUrl(), message);
//                }
//
//            }
//
//            return;
//        }
     //   holder.pb.setTag(position);
    }

    /**
     * 展示视频缩略图
     *
     * @param localThumb
     *            本地缩略图路径
     * @param iv
     *            远程缩略图路径
     */
    private void showVideoThumbView(final String localThumb, ImageView iv) {
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = ImageCache.getInstance().get(localThumb);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);
            iv.setClickable(true);
            iv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    System.err.println("video view is on click");
                    Intent intent = new Intent(activity,
                            ShowVideoActivity.class);
                    intent.putExtra("localpath", localThumb);
                    activity.startActivity(intent);

                }
            });

        } else {
            new LoadVideoImageTask().execute(localThumb, iv,
                    activity);
        }

    }

}