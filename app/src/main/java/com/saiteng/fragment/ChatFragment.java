package com.saiteng.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.lidroid.xutils.ViewUtils;
import com.saiteng.adapter.MessageAdapter;
import com.saiteng.audioHelper.VoicePlayClickListener;
import com.saiteng.audioHelper.VoiceRecorder;
import com.saiteng.stptt.CommonUtils;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.ImageGridActivity;
import com.saiteng.stptt.MainPTTActivity;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.PathUtil;
import com.saiteng.stptt.R;
import com.saiteng.stptt.ShareLocationActivity;
import com.saiteng.stptt.VideoChatActivity;
import com.saiteng.user.MessageInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moore on 2017/7/31.
 */

public class ChatFragment extends Fragment implements View.OnClickListener{

    private static final int REQUEST_CODE_EMPTY_HISTORY = 2;
    public static final int REQUEST_CODE_CONTEXT_MENU = 3;
    private static final int REQUEST_CODE_MAP = 4;
    public static final int REQUEST_CODE_TEXT = 5;
    public static final int REQUEST_CODE_VOICE = 6;
    public static final int REQUEST_CODE_PICTURE = 7;
    public static final int REQUEST_CODE_LOCATION = 8;
    public static final int REQUEST_CODE_NET_DISK = 9;
    public static final int REQUEST_CODE_FILE = 10;
    public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
    public static final int REQUEST_CODE_PICK_VIDEO = 12;
    public static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
    public static final int REQUEST_CODE_VIDEO = 14;
    public static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
    public static final int REQUEST_CODE_SELECT_USER_CARD = 16;
    public static final int REQUEST_CODE_SEND_USER_CARD = 17;
    public static final int REQUEST_CODE_CAMERA = 18;
    public static final int REQUEST_CODE_LOCAL = 19;
    public static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
    public static final int REQUEST_CODE_GROUP_DETAIL = 21;
    public static final int REQUEST_CODE_SELECT_VIDEO = 23;
    public static final int REQUEST_CODE_SELECT_FILE = 24;
    public static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;

    public static final int RESULT_CODE_COPY = 1;
    public static final int RESULT_CODE_DELETE = 2;
    public static final int RESULT_CODE_FORWARD = 3;
    public static final int RESULT_CODE_OPEN = 4;
    public static final int RESULT_CODE_DWONLOAD = 5;
    public static final int RESULT_CODE_TO_CLOUD = 6;
    public static final int RESULT_CODE_EXIT_GROUP = 7;
    private View view = null;
    private Button btn_talk,btn_kryboard,btn_more,btn_send;
    private ImageView btn_take_picture,btn_picture,btn_location,btn_video;
    private EditText et_sendmessage;
    private TextView recordingHint;
    private LinearLayout btn_presstospeak,btnContainer;
    private RelativeLayout edittext_layout;
    private View recordingContainer;
    private ImageView micImage;
    private InputMethodManager manager;
    private Drawable[] micImages;
    private View more;
    private List<MessageInfo> data = new ArrayList<MessageInfo>();
    private boolean haveMoreData = true;
    private ListView listView;
    private MessageAdapter adapter;
    private File cameraFile;
    public static String playMsgId;
    private ProgressBar loadmorePB;
    private MessageInfo messageInfo;
    private VoiceRecorder voiceRecorder;

    //多媒体消息类型
    public static final int MESSAGE_TXT_TYPE=0x03;
    public static final int MESSAGE_GPS_TYPE=0x04;
    public static final int MESSAGE_VIDEO_TYPE=0x01;
    public static final int MESSAGE_AUDIO_TYPE=0x00;
    public static final int MESSAGE_PIC_TYPE=0x02;

    //多媒体消息方向（接收/发送）
    public static final int MESSAGE_SEND = 0x06;
    public static final int MESSAGE_RECE = 0x07;

    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;


    private Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            // 切换msg切换图片
            micImage.setImageDrawable(micImages[msg.what]);
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.fragment_chat, (ViewGroup) getActivity().findViewById(R.id.study_viewpager), false);
        intentFilter = new IntentFilter();
        localReceiver = new LocalReceiver();
        intentFilter.addAction(Config.BOARDCAST_UPDATECHAT);
        MyPTTApplication.getInstance().getLocalBroadcastManager().registerReceiver(localReceiver, intentFilter);


        findview();
        setupView();
    }

    private void setupView() {
        data= MyPTTApplication.getInstance().getDBManager().selectData(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null),MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
        adapter = new MessageAdapter(getContext(),data);
        // 显示消息
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new ListScrollListener());
    }

    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Config.UPDATE_CHAT:
                    data.clear();
                    data= MyPTTApplication.getInstance().getDBManager().selectData(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null),MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
                    adapter.refresh();
                    break;
            }
        }
    };

    /**
     * listview滑动监听listener
     *
     */
    private class ListScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
//                    if (view.getFirstVisiblePosition() == 0 && !isloading
//                            && haveMoreData) {
//                        loadmorePB.setVisibility(View.VISIBLE);
//                    }
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

        }

    }

    public void findview(){
        recordingContainer = view.findViewById(R.id.recording_container);
        micImage = (ImageView)view.findViewById(R.id.mic_image);
        btn_send = (Button)view.findViewById(R.id.btn_send);
        btn_talk = (Button)view.findViewById(R.id.btn_set_mode_voice);
        btn_kryboard = (Button)view.findViewById(R.id.btn_set_mode_keyboard);
        btn_more = (Button)view.findViewById(R.id.btn_more);
        btn_presstospeak = (LinearLayout)view.findViewById(R.id.btn_press_to_speak);
        edittext_layout = (RelativeLayout) view.findViewById(R.id.edittext_layout);
        btnContainer = (LinearLayout)view.findViewById(R.id.ll_btn_container);
        et_sendmessage = (EditText) view.findViewById(R.id.et_sendmessage);
        btn_take_picture = (ImageView)view.findViewById(R.id.btn_take_picture);
        btn_picture = (ImageView)view.findViewById(R.id.btn_picture);
        btn_location = (ImageView)view.findViewById(R.id.btn_location);
        btn_video = (ImageView)view.findViewById(R.id.btn_video);
        recordingHint = (TextView) view.findViewById(R.id.recording_hint);
        listView = (ListView) view.findViewById(R.id.list);
        loadmorePB = (ProgressBar) view.findViewById(R.id.pb_load_more);
        more = view.findViewById(R.id.more);
        btn_talk.setOnClickListener(this);
        btn_kryboard.setOnClickListener(this);
        btn_presstospeak.setOnClickListener(this);
        btn_presstospeak.setOnTouchListener(new PressToSpeakListen());
        btn_more.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        btn_take_picture.setOnClickListener(this);
        btn_picture.setOnClickListener(this);
        btn_location.setOnClickListener(this);
        btn_video.setOnClickListener(this);

        // 动画资源文件,用于录制语音时
        micImages = new Drawable[] {
                getResources().getDrawable(R.mipmap.record_animate_01),
                getResources().getDrawable(R.mipmap.record_animate_02),
                getResources().getDrawable(R.mipmap.record_animate_03),
                getResources().getDrawable(R.mipmap.record_animate_04),
                getResources().getDrawable(R.mipmap.record_animate_05),
                getResources().getDrawable(R.mipmap.record_animate_06),
                getResources().getDrawable(R.mipmap.record_animate_07),
                getResources().getDrawable(R.mipmap.record_animate_08),
                getResources().getDrawable(R.mipmap.record_animate_09),
                getResources().getDrawable(R.mipmap.record_animate_10),
                getResources().getDrawable(R.mipmap.record_animate_11),
                getResources().getDrawable(R.mipmap.record_animate_12),
                getResources().getDrawable(R.mipmap.record_animate_13),
                getResources().getDrawable(R.mipmap.record_animate_14), };
        voiceRecorder = new VoiceRecorder(micImageHandler);
        manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        wakeLock = ((PowerManager)  getContext().getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA) {//拍摄的照片
            if (Config.cameraFile != null && Config.cameraFile.exists()){
                sendPicture(Config.cameraFile.getAbsolutePath(), Config.cameraFile.length());
                Config.cameraFile=null;
            }
        } else if (requestCode == REQUEST_CODE_MAP) { // 地图
            if(data!=null){
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("address");
                if (locationAddress != null && !locationAddress.equals("")) {
                    more(more);
                    sendLocationMsg(latitude, longitude, "", locationAddress);
                }
            }
        }else if(requestCode == REQUEST_CODE_LOCAL){//发送本地图片
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    sendPicByUri(selectedImage);
                }
            }
        }else if(requestCode == REQUEST_CODE_SELECT_VIDEO){
            if(data!=null){
                int duration = data.getIntExtra("dur", 0);
                String videoPath = data.getStringExtra("path");
                File file = new File(PathUtil.getInstance().getVideoChatPathName(),
                        "thvideo" + System.currentTimeMillis());
                Bitmap bitmap = null;
                FileOutputStream fos = null;
                try {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, 3);
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeResource(getResources(),
                                R.mipmap.app_panel_video_icon);
                    }
                    fos = new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        fos = null;
                    }
                    if (bitmap != null) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                }
                sendVideo(videoPath, file.getAbsolutePath(), duration / 1000);
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_set_mode_voice:
                btn_talk.setVisibility(View.GONE);
                edittext_layout.setVisibility(View.GONE);
                btn_presstospeak.setVisibility(View.VISIBLE);
                btn_kryboard.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_set_mode_keyboard:
                btn_talk.setVisibility(View.VISIBLE);
                edittext_layout.setVisibility(View.VISIBLE);
                btn_presstospeak.setVisibility(View.GONE);
                btn_kryboard.setVisibility(View.GONE);
                break;
            case R.id.btn_send:
                String s = et_sendmessage.getText().toString();
                sendText(s);
                et_sendmessage.setText("");
                hideKeyboard();
                break;
            case R.id.btn_more:
                if(more.getVisibility() == View.GONE){
                    hideKeyboard();
                    more.setVisibility(View.VISIBLE);
                    btnContainer.setVisibility(View.VISIBLE);
                }else
                    more.setVisibility(View.GONE);
                break;
            case R.id.btn_take_picture:
                selectPicFromCamera();
                break;
            case R.id.btn_picture:
                selectPicFromLocal();
                break;
            case R.id.btn_location:
               startActivityForResult(new Intent(MainPTTActivity.getActivity(), ShareLocationActivity.class),
                        REQUEST_CODE_MAP);
                break;
            case R.id.btn_video:
//                Intent intent = new Intent(getContext(),
//                        ImageGridActivity.class);
//                startActivityForResult(intent,REQUEST_CODE_SELECT_VIDEO);
                //启动仿微信视频聊天界面
                Intent intent = new Intent(getContext(),
                       VideoChatActivity.class);
                startActivity(intent);
                break;
        }
    }
    private PowerManager.WakeLock wakeLock;
    /**
     * 按住说话listener
     *
     */
    class PressToSpeakListen implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!CommonUtils.isExitsSdcard()) {
                        Toast.makeText(getContext(), "发送语音需要sdcard支持！",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    try {
                        v.setPressed(true);
                        wakeLock.acquire();
                        if (VoicePlayClickListener.isPlaying)
                            VoicePlayClickListener.currentPlayListener
                                    .stopPlayVoice();
                        recordingContainer.setVisibility(View.VISIBLE);
                        recordingHint
                                .setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                      //启动录制音频，保存为本地文件再发送
                        voiceRecorder.startRecording(null, "user1",
                                getContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                        v.setPressed(false);
                        if (wakeLock.isHeld())
                            wakeLock.release();
                        recordingContainer.setVisibility(View.INVISIBLE);
                        Toast.makeText(getContext(), R.string.recoding_fail,
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        recordingHint
                                .setText(getString(R.string.release_to_cancel));
                        recordingHint
                                .setBackgroundResource(R.drawable.recording_text_hint_bg);
                    } else {
                        recordingHint
                                .setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (wakeLock.isHeld())
                        wakeLock.release();
                    if (event.getY() < 0) {
                        // discard the recorded audio.
                       //放弃录音文件

                    } else {
                        // stop recording and send voice file
                        try {
                            int length= voiceRecorder.stopRecoding();
                            if (length > 0) {
                                sendVoice(voiceRecorder.getVoiceFilePath(),
                                        voiceRecorder
                                                .getVoiceFileName(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null)),
                                        Integer.toString(length), voiceRecorder.fileLength());
                            } else if (length == -1) {
                                Toast.makeText(getContext(), "无录音权限",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "录音时间太短",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "发送失败，请检测服务器是否连接",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                    return true;
                default:
                    recordingContainer.setVisibility(View.INVISIBLE);
//                    if (voiceRecorder != null)
//                        voiceRecorder.discardRecording();
                    return false;
            }
        }
    }

    /**
     * 发送语音
     *
     * @param filePath
     * @param fileName
     * @param length
     */
    private void sendVoice(String filePath, String fileName, String length,
                           long  filelength) {
        if (!(new File(filePath).exists())) {
            return;
        }
        messageInfo = new MessageInfo();
        messageInfo.setMessage_Type(MESSAGE_AUDIO_TYPE);
        messageInfo.setMessage_senderID(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null));
        messageInfo.setMessage_receiverID(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
        messageInfo.setMessage_Direct(MESSAGE_SEND);
        messageInfo.setMessage_length(filelength);
        messageInfo.setPath(filePath);
        messageInfo.setMessageContext(null);
        messageInfo.setSendstatus(0);
        messageInfo.sendMessage();
        MyPTTApplication.getInstance().getDBManager().insertData(messageInfo);
        data.clear();
        data= MyPTTApplication.getInstance().getDBManager().selectData(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null),MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
        adapter.refresh();
    }

    /**
     * 发送文本消息到服务器，再更新本地listview显示。
     * @param content
     *            message content
     *            boolean resend
     */
    private void sendText(String content) {
        if (content.length() > 0) {
            messageInfo = new MessageInfo();
            messageInfo.setMessage_Type(MESSAGE_TXT_TYPE);
            messageInfo.setMessage_senderID(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null));
            messageInfo.setMessage_receiverID(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
            messageInfo.setMessageContext(content.toString());
            messageInfo.setMessage_length(content.getBytes().length);
            messageInfo.setMessage_Direct(MESSAGE_SEND);
            messageInfo.setPath("");
            messageInfo.setSendstatus(0);
            messageInfo.sendMessage();
            MyPTTApplication.getInstance().getDBManager().insertData(messageInfo);
            data.clear();
            data= MyPTTApplication.getInstance().getDBManager().selectData(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null),MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
            adapter.refresh();
//            listView.setAdapter(adapter);
//            adapter.refresh();
//            listView.setSelection(listView.getCount() - 1);
        }
    }

    /**
     * 发送视频消息
     */
    private void sendVideo(final String filePath, final String thumbPath,
                           final int length) {
        final File videoFile = new File(filePath);
        if (!videoFile.exists()) {
            return;
        }
        messageInfo = new MessageInfo();
        messageInfo.setMessage_Type(MESSAGE_VIDEO_TYPE);
        messageInfo.setMessage_senderID(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null));
        messageInfo.setMessage_receiverID(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
        messageInfo.setMessageContext(null);
        messageInfo.setPath(filePath);
        messageInfo.setMessage_length(videoFile.length());
        messageInfo.setMessage_Direct(MESSAGE_SEND);
        messageInfo.setSendstatus(0);
        messageInfo.sendMessage();
        MyPTTApplication.getInstance().getDBManager().insertData(messageInfo);
        data.clear();
        data= MyPTTApplication.getInstance().getDBManager().selectData(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null),MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
        adapter.refresh();
    }

    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage
     */
    private void sendPicByUri(Uri selectedImage) {
        // String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = MainPTTActivity.getActivity().getContentResolver().query(selectedImage, null, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex("_data");
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;
            File file;
            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(MainPTTActivity.getActivity(), "找不到图片", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }else{
                file = new File(picturePath);
            }
            sendPicture(picturePath, file.length());
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(MainPTTActivity.getActivity(), "找不到图片", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendPicture(file.getAbsolutePath(), file.length());
        }
    }
    /**
     * 发送图片
     *
     * @param filePath
     */
    private void sendPicture(final String filePath, long  filelength) {
        messageInfo = new MessageInfo();
        messageInfo.setMessage_Type(MESSAGE_PIC_TYPE);
        messageInfo.setMessage_senderID(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null));
        messageInfo.setMessage_receiverID(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
        messageInfo.setMessageContext("");
        messageInfo.setMessage_Direct(MESSAGE_SEND);
        messageInfo.setMessage_length(filelength);
        messageInfo.setPath(filePath);
        messageInfo.setSendstatus(0);
        MyPTTApplication.getInstance().getDBManager().insertData(messageInfo);
        data.clear();
        data= MyPTTApplication.getInstance().getDBManager().selectData(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null),MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
        adapter.refresh();
        messageInfo.sendMessage();
    }

    /**
     * 发送位置信息
     *
     * @param latitude
     * @param longitude
     * @param imagePath
     * @param locationAddress
     */
    private void sendLocationMsg(double latitude, double longitude,
                                 String imagePath, String locationAddress) {

        String content = latitude+":"+longitude+":"+locationAddress;
        messageInfo = new MessageInfo();
        messageInfo.setMessage_Type(MESSAGE_GPS_TYPE);
        messageInfo.setMessage_senderID(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null));
        messageInfo.setMessage_receiverID(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
        messageInfo.setMessageContext(content);
        messageInfo.setMessage_Direct(MESSAGE_SEND);
        messageInfo.setMessage_length(content.getBytes().length);
        messageInfo.setSendstatus(0);

        MyPTTApplication.getInstance().getDBManager().insertData(messageInfo);
        data.clear();
        data= MyPTTApplication.getInstance().getDBManager().selectData(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null),MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,null));
        adapter.refresh();
        messageInfo.sendMessage();

    }
    //发送系统相册里的图片
    private void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent,REQUEST_CODE_LOCAL);
    }
    //发送拍摄的相片
    private void selectPicFromCamera() {
        Config.cameraFile = new File(PathUtil.getImageChatPathName(),System.currentTimeMillis() + ".jpg");
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                        MediaStore.EXTRA_OUTPUT, Uri.fromFile( Config.cameraFile)),
                REQUEST_CODE_CAMERA);
    }
    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        if (((Activity)getContext()).getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (((Activity)getContext()).getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(((Activity)getContext()).getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 显示或隐藏图标按钮页
     *
     * @param view
     */
    public void more(View view) {
        if (more.getVisibility() == View.GONE) {
            System.out.println("more gone");
            hideKeyboard();
            more.setVisibility(View.VISIBLE);
            btnContainer.setVisibility(View.VISIBLE);

        } else {
                more.setVisibility(View.GONE);

        }

    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction()==Config.BOARDCAST_UPDATECHAT){
                mhandler.sendEmptyMessage(Config.UPDATE_CHAT);
            }
        }
    }

}
