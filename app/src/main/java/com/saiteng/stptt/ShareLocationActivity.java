package com.saiteng.stptt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Moore on 2017/9/8.
 */

public class ShareLocationActivity extends Activity implements PoiSearch.OnPoiSearchListener
    ,AMapLocationListener,LocationSource
         {

    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private LatLng mLatlng;
    private MarkerOptions markerOptions;
    // 位置列表
    private ListView mListView;
    private MapView mMapView = null;
    private AMap aMap;
    private MyLocationStyle mylocationstyle;

    //poiSearch相关
    private PoiSearch poiSearch;
    private PoiSearch.Query query;
    boolean isPoiSearched = false; //是否进行poi搜索

    //listview
    private ListView ll;
    ArrayList<PoiItem> arrayList;
    MyAdpter adapter;
    MyHandler myHandler;

    //字体
    Typeface tf;


    //搜索栏
    FrameLayout frameLayout;
    ImageView searchIv;
    EditText searchEt;
    TextView title;
    Button btn;
    ImageView success;
    boolean onSearch = false; //是否打开搜索栏
    ImageView back;

    private double mCurrentLat;
    private double mCurrentLng;
    private String Address;
    Map<String, String> currentInfo = new HashMap<>();
    int selectIndex = -1;
    ImageView currentSelectItem = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_chooseplace);
        findAllView();
        setAllViewOnclickLinster();
        //在执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        initAMap();
    }

    private void findAllView() {
        mMapView = (MapView)findViewById(R.id.map);
        frameLayout = (FrameLayout) findViewById(R.id.searchLayout);
        searchEt = (EditText) findViewById(R.id.search_input);
        searchIv = (ImageView) findViewById(R.id.search);
        btn = (Button) findViewById(R.id.search_go_btn);
        success = (ImageView) findViewById(R.id.success);
        back = (ImageView) findViewById(R.id.back);

        //初始化listview
        ll = (ListView) findViewById(R.id.ll);
        arrayList = new ArrayList<>();
        adapter = new MyAdpter();
        ll.setAdapter(adapter);

        (title = (TextView) findViewById(R.id.title)).setTypeface(tf);
        myHandler = new MyHandler();
    }


    /**
     * 设置点击事件
     */
    void setAllViewOnclickLinster() {


        //当搜索图标点击时，切换显示效果
        searchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (title.getVisibility() == View.VISIBLE) {
                    hideTitle();
                } else if (title.getVisibility() == View.GONE) {
                    showTitle();
                }
            }
        });

        //点击搜索按钮时，搜索关键字
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = searchEt.getText().toString();
                if (!key.trim().isEmpty()) {
                    if (currentSelectItem != null) {
                        currentSelectItem.setVisibility(View.INVISIBLE);
                    }
                    searchPoi(key, 0, currentInfo.get("cityCode"), false);
                }
            }
        });

        //使editText监听回车事件，进行搜索，效果同上
        searchEt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    String key = searchEt.getText().toString();
                    if (!key.trim().isEmpty()) {
                        if (currentSelectItem != null) {
                            currentSelectItem.setVisibility(View.INVISIBLE);
                        }
                        searchPoi(key, 0, currentInfo.get("cityCode"), false);
                    }
                    return true;
                }

                return false;
            }
        });

        //返回处理事件
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onSearch) {
                    showTitle();
                } else {
                    finish();
                }
            }
        });

        //完成事件
        success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取数据并返回上一个activity即可
                Intent intent = ShareLocationActivity.this.getIntent();
                intent.putExtra("latitude", mCurrentLat);
                intent.putExtra("longitude", mCurrentLng);
                intent.putExtra("address", Address);
                ShareLocationActivity.this.setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                Log.e("----",mCurrentLat+","+mCurrentLng);
            }
        });


        //listview点击事件
        ll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                PoiItem item = arrayList.get(i);

                //在地图上添加一个marker，并将地图中移动至此处
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.pin_gps));
                markerOptions.title(item.getAdName());
                Address=item.getAdName();
                LatLng ll = new LatLng(item.getLatLonPoint().getLatitude(), item.getLatLonPoint().getLongitude());
                mCurrentLat=ll.latitude;
                mCurrentLng=ll.longitude;
                markerOptions.position(ll);
                //清除所有marker等，保留自身
                aMap.clear(true);
                aMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                //CameraUpdate cu = CameraUpdateFactory.newLatLng(ll);
               // aMap.animateCamera(cu);
                aMap.addMarker(markerOptions);

                //存储当前点击位置
                selectIndex = i;

                //存储当前点击view，并修改view和上一个选中view的定位图标
                ImageView iv = (ImageView) view.findViewById(R.id.yes);
                iv.setVisibility(View.VISIBLE);
                if (currentSelectItem != null) {
                    currentSelectItem.setVisibility(View.INVISIBLE);
                }
                currentSelectItem = iv;
                if (onSearch) {
                    //退出搜索模式，显示地图
                    showTitle();
                }
            }
        });
    }

    /**
     * 初始化高德地图
     */
    void initAMap() {
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
            markerOptions = new MarkerOptions();
            aMap.animateCamera(CameraUpdateFactory.zoomTo(18));
            aMap.getUiSettings().setScaleControlsEnabled(false);
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
            mylocationstyle=new MyLocationStyle();
            mylocationstyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
            mylocationstyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
            aMap.setMyLocationStyle(mylocationstyle);
            aMap.setLocationSource(this);// 设置定位监
            aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //点击返回键时，将浏览器后退
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (onSearch) {
                showTitle();
                return true;
            } else
                return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

             @Override
             public void onLocationChanged(AMapLocation aMapLocation) {
                 if (!isPoiSearched) {
                     //存储定位数据
                     mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                     mLatlng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                     markerOptions.position(new LatLng(mLatlng.latitude, mLatlng.longitude));
                     mCurrentLat = aMapLocation.getLatitude();
                     mCurrentLng = aMapLocation.getLongitude();
                     Address=aMapLocation.getAddress();
                     String[] args = aMapLocation.toString().split("#");
                     for (String arg : args) {
                         String[] data = arg.split("=");
                         if (data.length >= 2)
                             currentInfo.put(data[0], data[1]);
                     }
                     //搜索poi
                     searchPoi("", 0, currentInfo.get("cityCode"), true);
                     //latitude=41.652146#longitude=123.427205#province=辽宁省#city=沈阳市#district=浑南区#cityCode=024#adCode=210112#address=辽宁省沈阳市浑南区创新一路靠近东北大学浑南校区#country=中国#road=创新一路#poiName=东北大学浑南校区#street=创新一路#streetNum=193号#aoiName=东北大学浑南校区#poiid=#floor=#errorCode=0#errorInfo=success#locationDetail=24 #csid:1cce9508143d493182a8da7745eb07b3#locationType=5

                 }
             }

             @Override
             public void activate(OnLocationChangedListener listener) {
                 // 激活定位
                 mListener = listener;
                 if (mlocationClient == null) {
                     mlocationClient = new AMapLocationClient(ShareLocationActivity.this);
                     mLocationOption = new AMapLocationClientOption();
                     //设置定位监听
                     mlocationClient.setLocationListener(this);
                     //设置为低功耗定位模式
                     mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                     //设置定位参数
                     mlocationClient.setLocationOption(mLocationOption);
                     // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
                     // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
                     mLocationOption.setInterval(10000);
                     // 在定位结束后，在合适的生命周期调用onDestroy()方法
                     // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
                     mlocationClient.startLocation();
                 }

             }

             @Override
             public void deactivate() {
                 mListener = null;
                 if (mlocationClient != null) {
                     mlocationClient.stopLocation();
                     mlocationClient.onDestroy();
                 }
                 mlocationClient = null;
             }

             class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x001:
                    //加载listview中数据
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    /**
     * 自定义adpter
     */
    class MyAdpter extends BaseAdapter {

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return arrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            //布局加载器
            LayoutInflater inflater = LayoutInflater.from(ShareLocationActivity.this);
            //加载location_item布局
            View view1 = inflater.inflate(R.layout.location, null);

            //修改文字和字体
            TextView v1 = (TextView) view1.findViewById(R.id.name);
            TextView v2 = (TextView) view1.findViewById(R.id.sub);
            ImageView iv = (ImageView) view1.findViewById(R.id.yes);
            v1.setText(arrayList.get(i).getTitle());
            v1.setTypeface(tf);

            v2.setText(arrayList.get(i).getSnippet());
            v2.setTypeface(tf);

            if (selectIndex == i) {
                iv.setVisibility(View.VISIBLE);
                currentSelectItem = iv;
            } else {
                iv.setVisibility(View.INVISIBLE);
            }
            return view1;
        }
    }


    /**
     * 搜索poi
     *
     * @param key      关键字
     * @param pageNum  页码
     * @param cityCode 城市代码，或者城市名称
     * @param nearby   是否搜索周边
     */
    void searchPoi(String key, int pageNum, String cityCode, boolean nearby) {
        isPoiSearched = true;
        query = new PoiSearch.Query(key, "", cityCode);
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，
        //POI搜索类型共分为以下20种：汽车服务|汽车销售|
        //汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|
        //住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|
        //金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query.setPageSize(50);// 设置每页最多返回多少条poiitem
        query.setPageNum(pageNum);//设置查询页码
        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        if (nearby)
            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(mCurrentLat,
                    mCurrentLng), 1500));//设置周边搜索的中心点以及半径
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        int index = 0;
        //填充数据，并更新listview
        ArrayList<PoiItem> result = poiResult.getPois();
        if (result.size() > 0) {
            arrayList.clear();
            selectIndex = -1;
            arrayList.addAll(result);
            myHandler.sendEmptyMessage(0x001);
        }
        for (PoiItem item : poiResult.getPois()) {
//            Log.e(ProConfig.TAG, item.toString());
//            Log.e(ProConfig.TAG, item.getDirection());
//            Log.e(ProConfig.TAG, item.getAdName());
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 显示标题栏，即默认状态
     */
    void showTitle() {
        //显示标题栏
        title.setVisibility(View.VISIBLE);
        success.setVisibility(View.VISIBLE);
        searchEt.setVisibility(View.GONE);
        btn.setVisibility(View.GONE);
        frameLayout.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        5));
        mMapView.setVisibility(View.VISIBLE);
        onSearch = false;
        closeKeyboard(this);
    }

    /**
     * 隐藏标题栏，即进行搜索
     */
    void hideTitle() {
        //显示搜索框
        title.setVisibility(View.GONE);
        success.setVisibility(View.GONE);
        searchEt.setVisibility(View.VISIBLE);
        btn.setVisibility(View.VISIBLE);
        frameLayout.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        50));
        mMapView.setVisibility(View.GONE);
        onSearch = true;
    }

    /**
     * 强制关闭软键盘
     */
    public void closeKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            //如果开启
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            //关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
        }
    }
}
