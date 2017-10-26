package lbs.ctl.lbs.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import lbs.ctl.lbs.BaiduMap.BaiduMapUtil;
import lbs.ctl.lbs.BaiduMap.ConvexHull;
import lbs.ctl.lbs.BaiduMap.LocationService;
import lbs.ctl.lbs.R;
import lbs.ctl.lbs.WebServiceConn;
import lbs.ctl.lbs.luce.LuceCellInfo;
import lbs.ctl.lbs.utils.AlrDialog_Show;
import lbs.ctl.lbs.utils.GPSUtils;
import lbs.ctl.lbs.utils.LocationPoint;
import lbs.ctl.lbs.utils.NumCheck;
import lbs.ctl.lbs.utils.Point;
import lbs.ctl.lbs.utils.dataCode;

public class MapFindActivity extends AppCompatActivity {
    private Context context;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    private LocationService locationService=null;
    private LocationPoint point=null;

    private Spinner mncSpinner;
    private String[] modes={"中国移动","中国联通","中国电信2G","中国电信4G"};
    private ArrayAdapter mncAdapter;

    private TextView lac_text;
    private TextView cell_text;
    private LinearLayout bid_liner;
    /**
     * 查询按钮
     */
    private Button qure_Btn;

    private Button daohang_Btn;
    private Button clear_Btn;
    private Button pos_Btn;
    private EditText lac_edit;
    private EditText cell_edit;
    private EditText bid_edit;
    private CheckBox hex_mode;

    int mode_index=0;
    String mcc="460";
    static String mnc="00";
    int mode=0;//网络类型 0移动 1联通 2电信
    int net=0;//制式 0:2g 、1:3g 、2:4g

    String lac="";
    static String cellid="";
    String sid="";
    String nid="";
    String bid="";
    public static boolean hex_flg = false;//十六进制

    WebServiceConn conn;
    BaiduMapUtil baiduMapUtil;
    public static List<OverlayOptions> showList=new ArrayList<>();
    private int[] color={0xffff0000,0xff00ff00,0xff0000ff,0xffffff00,0xff00ffff,0xffff00ff};
    private List<LuceCellInfo> luceCellInfos=new ArrayList<>();
    private LatLng end_latLng=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapfind_layout);

        MyApplication.getInstance().addActivity(this);
        context=this;
        conn=new WebServiceConn(context,0);
        Intent intent=getIntent();
        point = (LocationPoint) intent.getSerializableExtra("point");

        initView();
        initMap();
//        GetMyLocation();
    }
//
//    private void GetMyLocation() {
//        new Thread(new Runnable() {
//            public void run() {
//                locationService = ((MyApplication)context.getApplicationContext()).locationService;
//                locationService.registerListener(mListener);
//                locationService.setLocationOption(locationService.getDefaultLocationClientOption());
//                locationService.start();
//            }
//        }).start();
//    }

    private void initMap() {
        mBaiduMap = mMapView.getMap();
        baiduMapUtil=new BaiduMapUtil(context,mBaiduMap);
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final Button button = new Button(context);
                button.setBackgroundResource(R.drawable.popup);
                button.setTextColor(Color.BLACK);
                final LatLng ll = marker.getPosition();
                Bundle bundle=marker.getExtraInfo();
                final String info= (String) bundle.getSerializable("info");
                button.setText(info);
                InfoWindow mInfoWindow = new InfoWindow(button, ll, -47);
                mBaiduMap.showInfoWindow(mInfoWindow);
                return true;
            }
        });
    }

    private void initView() {
        mMapView=(MapView) findViewById(R.id.bmapView);
        initModeSpinner();
        lac_text=(TextView)findViewById(R.id.lac_text_id);
        cell_text=(TextView)findViewById(R.id.cell_text_id);
        lac_edit = (EditText)findViewById(R.id.lac_str);
        cell_edit = (EditText)findViewById(R.id.cellid_str);
        bid_edit = (EditText)findViewById(R.id.Bid_str);
        bid_liner=(LinearLayout)findViewById(R.id.bid_liner);

        hex_mode = (CheckBox)findViewById(R.id.Hex);
        hex_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked)
                {
                    //editext只显示数字和小数点
                    lac_edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                    cell_edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                    bid_edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                else
                {
                    //editext显示文本
                    lac_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                    cell_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                    bid_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }
        });
        qure_Btn = (Button) findViewById(R.id.qure_btn);
        qure_Btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //基站查询
                boolean start_flg=false;
                start_flg=GetRequestParam();
                if(start_flg==true)
                {
                    new Thread(new Runnable() {
                        public void run() {
                            Request_Gps();
                        }
                    }).start();
                }
                //左侧弹出多个基站的查询数据
//                show_dialog();
            }
        });

        daohang_Btn=(Button)findViewById(R.id.daohang_btn);
        daohang_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng star=new LatLng(point.getLat(),point.getLon());
                baiduMapUtil.launchNavigator(point.getAddress(),star,end_latLng);
            }
        });

        clear_Btn = (Button) findViewById(R.id.clear_btn);
        clear_Btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mBaiduMap!=null)
                {
                    mBaiduMap.clear();
                    showList.clear();
                    luceCellInfos.clear();
                }
            }
        });


        //当前位置
        pos_Btn = (Button) findViewById(R.id.pos_btn);
        pos_Btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                baiduMapUtil.addMarker(point.getLat(),point.getLon(),0,point.getAddress());
            }
        });
    }
    private void initModeSpinner(){
        mncSpinner = (Spinner)findViewById(R.id.set_mnc_mode);
        mncAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,modes);
        //设置下拉列表的风格
        mncAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mncSpinner.setAdapter(mncAdapter);
        mncSpinner.setSelection(0,true);
        //添加事件Spinner事件监听
        mncSpinner.setOnItemSelectedListener(dModeSelectLis);
    }
    private AdapterView.OnItemSelectedListener dModeSelectLis = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // btsCtrl.setDetectMode(position);
            mode_index=position;
            if(mode_index==2)
            {
                lac_text.setText("SID");
                cell_text.setText("NID");
                bid_liner.setVisibility(View.VISIBLE);
            }
            else
            {
                lac_text.setText("LAC");
                cell_text.setText("CELL");
                bid_liner.setVisibility(View.GONE);

            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    private void Request_Gps()//查询经纬度
    {
        String[] parameters=new String[3];
        String[] get_msg=new String[2];
        get_msg[0]="";
        get_msg[1]="";
        LuceCellInfo luceCellInfo=new LuceCellInfo();
        for(int i=0;i<3;i++)
        {
            parameters[i]="";
        }
        if(mode== dataCode.cdma)
        {
            parameters[0]=sid;
            parameters[1]=bid;
            parameters[2]=nid;
            luceCellInfo.setLac_sid(Integer.valueOf(sid));
            luceCellInfo.setCi_nid(Integer.valueOf(nid));
            luceCellInfo.setBid(Integer.valueOf(bid));
        }
        else
        {
            parameters[0]=lac;
            parameters[1]=cellid;
            luceCellInfo.setLac_sid(Integer.valueOf(lac));
            luceCellInfo.setCi_nid(Integer.valueOf(cellid));
        }
        luceCellInfos.add(luceCellInfo);
        Local_Qur();
    }
    private void Local_Qur()
    {
        new FindPos(0).execute();
    }
    /**
     * 查询基站位置
     */
    private class FindPos extends AsyncTask<Object, Void, Object> {
        private int i=0;
        private FindPos(int i){
            this.i=i;
        }

        @Override
        protected Object doInBackground(Object... params) {
            JSONObject object=null;
            //查询轨迹
            if (conn.getToken()) {
                try {
                    if(mode==dataCode.cdma)
                    {
                        object=conn.submitMsg(conn.FindPos(mcc,mnc,sid,nid,bid));
                    }else {
                        object=conn.submitMsg(conn.FindPos(mcc,mnc,lac,cellid));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return object;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onPostExecute(Object object) {
            JSONObject object1=(JSONObject) object;
            if (conn.getResult(object1)==0){
                try {
                    JSONArray array=object1.getJSONArray("datalist");
                    List<Point> list=new ArrayList<>();
                    List<String> list_rssi=new ArrayList<>();
                    List<LatLng> latLngs=new ArrayList<>();
                    for (int i=0;i<array.length();i++){
                        JSONObject jsonObject= (JSONObject) array.opt(i);
                        Point point=new Point();
                        String lon=jsonObject.getString("lon");
                        String lat=jsonObject.getString("lat");
                        point.setLat(lat);
                        point.setLon(lon);
                        final double[] latlon= GPSUtils.wgs2bd(Double.valueOf(lat),Double.valueOf(lon));
                        if (jsonObject.has("acc")){
                            if (!(jsonObject.getString("acc").equals("")||jsonObject.getString("acc")==null)){
                                point.setAcc(jsonObject.getString("acc"));
                            }
                        }
                        if (jsonObject.has("rssi")){
                            if (!(jsonObject.getString("rssi").equals("")||jsonObject.getString("rssi")==null)){
                                point.setRssi(jsonObject.getString("rssi"));
                            }
                        }
                        if (jsonObject.has("addr")){
                            if (!(jsonObject.getString("addr").equals("")||jsonObject.getString("addr")==null)){
                                point.setAddr(jsonObject.getString("addr"));
                            }
                        }
                        double lat1=Double.valueOf(lat);
                        double lon1=Double.valueOf(lon);
                        if (jsonObject.has("acc")){
                            if (!((int)lat1==0&&(int)lon1==0)){
                                end_latLng=new LatLng(latlon[0],latlon[1]);
                                baiduMapUtil.addMarker(latlon[0],latlon[1],3,"第三方数据");
                            }
                        }
                        else if (jsonObject.has("rssi")){
                            //按强度渐变添加覆盖物
                            if (!((int)lat1==0&&(int)lon1==0)){
                                baiduMapUtil.addMarker(latlon[0],latlon[1],4,Math.abs(Double.valueOf(point.getRssi()))+"");
                            }
                            list_rssi.add(point.getRssi());
                            list.add(point);
                        }
                    }
                    int max_rssi=(int) Math.abs(Double.valueOf(list_rssi.get(0)));
                    int min_rssi=(int) Math.abs(Double.valueOf(list_rssi.get(0)));
                    for (int i=0;i<list_rssi.size()-1;i++){
                        //按强度渐变添加覆盖物
                        double current=Double.valueOf(list_rssi.get(i));
                        double next=Double.valueOf(list_rssi.get(i+1));
                        if (Math.abs(current)>max_rssi){
                            max_rssi=(int) Math.abs(current);
                        } else if (Math.abs(current)<min_rssi){
                            min_rssi=(int) Math.abs(current);
                        }
                    }
                    //根据最大小值对每个数算一个百分比，对百分比的区域进行比较，然后区分不同的颜色
                    Log.e("rssi",max_rssi+"   "+min_rssi+"");
                    for (int i=0;i<list.size();i++){
                        Point point= list.get(i);
                        double lat=Double.valueOf(point.getLat());
                        double lon=Double.valueOf(point.getLon());
                        if ((int)lat==0&&(int)lon==0){
                            list.remove(point);
                            i--;
                        }
                    }
                    //垃框查询
                    if (list.size()>=3){
                        ConvexHull convexHull=new ConvexHull(list);
                        List<Point> list_po=convexHull.calculateHull();
                        for (int i=0;i<list_po.size();i++){
                            Point point=list_po.get(i);
                            final double[] latlon= GPSUtils.wgs2bd(Double.valueOf(point.getLat()),Double.valueOf(point.getLon()));
                            LatLng latLng=new LatLng(latlon[0],latlon[1]);
                            latLngs.add(latLng);
                        }
                        if (showList.size()==0){
                            baiduMapUtil.draw_find(latLngs);
                        }else if (showList.size()==1||showList.size()>1){
                            //红：0xffff0000 绿：0xff00ff00 蓝：0xff0000ff 黄色：0xffffff00 青色：0xff00ffff 品红：0xffff00ff
                            OverlayOptions polygonOption=null;
                            if (showList.size()<7){
                                polygonOption = new PolygonOptions()
                                        .points(latLngs)
                                        .stroke(new Stroke(5, 0xff00ffff))
                                        .fillColor(color[showList.size()-1]);//只改变覆盖区域的颜色
                            }else {
                                polygonOption = new PolygonOptions()
                                        .points(latLngs)
                                        .stroke(new Stroke(5, 0xff00ffff))
                                        .fillColor(color[showList.size()-7]);
                            }
                            showList.add(polygonOption);
                            baiduMapUtil.add_more_overlay(showList);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("温馨提示");
                builder.setMessage("查询不到该基站的位置信息");
                builder.setPositiveButton("确定", null);
                builder.show();
            }
        }
    }
    private boolean GetRequestParam() {
        boolean start_flg = false;
        mcc = "460";
        mnc = "00";
        lac = lac_edit.getText().toString().trim();
        cellid = cell_edit.getText().toString().trim();
        sid = lac_edit.getText().toString().trim();
        nid = cell_edit.getText().toString().trim();
        bid = bid_edit.getText().toString().trim();
        if(mode_index== dataCode.cdma)
        {
//            lac_text.setText("SID:");
            if(net==2)
            {
                mnc = "11";
            }
            else
            {
                mnc = "03";
            }
        }
        if (net == 0)
        {
            mode = mode_index;
            if (mode_index == dataCode.china_mobile) {
                mnc = "00";
            } else if (mode_index == dataCode.china_unicom) {
                mnc = "01";
            }
        }
        else if (net == 1)//3g
        {
            if (mode_index == dataCode.china_mobile) {
                mnc = "00";
                mode = dataCode.td_swcdma;
            } else if (mode_index == dataCode.china_unicom) {
                mode = dataCode.wcdma;
                mnc = "01";
            }
        }
        else if (net == 2)//4g
        {
            if (mode_index == dataCode.china_mobile) {
                mode = dataCode.tdd_lte;
                mnc = "00";
            } else if (mode_index == dataCode.china_unicom) {
                mode = dataCode.fdd_lte;
                mnc = "01";
            } else {
                mode = dataCode.fdd_lte;
                mnc = "01";
            }

        }

        if(NumCheck.isNumeric(mcc)==true)
        {
            if(NumCheck.isNumeric(mnc)==true)
            {
                if(hex_mode.isChecked())
                {
                    hex_flg = true;
                }
                else
                {
                    hex_flg = false;
                }
                if(mode==dataCode.cdma)//电信
                {
                    if("".equals(sid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置SID!","确定");
                    }
                    else if("".equals(nid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置NID!","确定");
                    }
                    else if("".equals(bid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置BID!","确定");
                    }
                    else
                    {

                        if(hex_flg==true)//十六进制
                        {
                            boolean x_flg=false;
                            //检查x_flg是否为16进制  如果是则转换为10进制
                            x_flg=NumCheck.isTrueHexDigit(sid);
                            if(x_flg==true)
                            {
                                long value = Long.parseLong(sid,16);
                                sid= Long.toString(value);
                                x_flg=NumCheck.isTrueHexDigit(nid);
                                if(x_flg==true)
                                {
                                    value = Long.parseLong(nid,16);
                                    nid= Long.toString(value);
                                    x_flg=NumCheck.isTrueHexDigit(bid);
                                    if(x_flg==true)
                                    {
                                        value = Long.parseLong(bid,16);
                                        bid= Long.toString(value);
                                        start_flg=true;
                                    }
                                    else
                                    {
                                        AlrDialog_Show.alertDialog(context,"提示","请设置正确格式BID!","确定");
                                    }
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式NID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式SID!","确定");
                            }
                        }
                        else//十进制
                        {
                            if(NumCheck.isNumeric(bid)==true)
                            {
                                if(NumCheck.isNumeric(nid)==true)
                                {
                                    if(NumCheck.isNumeric(sid)==true)
                                    {
                                        start_flg=true;
                                    }
                                    else
                                    {
                                        AlrDialog_Show.alertDialog(context,"提示","请设置正确格式SID!","确定");
                                    }
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式NID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式BID!","确定");
                            }
                        }
                    }
                }
                else
                {
                    if("".equals(lac))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置LAC!","确定");
                    }
                    else if("".equals(cellid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置CELLID!","确定");
                    }
                    else
                    {
                        if(hex_flg==true)
                        {
                            if(NumCheck.isTrueHexDigit(lac)==true)
                            {
                                long value = Long.parseLong(lac,16);
                                lac= Long.toString(value);
                                if(NumCheck.isTrueHexDigit(cellid)==true)
                                {
                                    value = Long.parseLong(cellid,16);
                                    cellid= Long.toString(value);
                                    start_flg=true;
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式CELLID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式LAC!","确定");
                            }
                        }
                        else
                        {
                            if(NumCheck.isNumeric(lac)==true)
                            {
                                if(NumCheck.isNumeric(cellid)==true)
                                {
                                    start_flg=true;
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式CELLID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式LAC!","确定");
                            }
                        }
                    }
                }
            }
            else
            {
                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式mnc!","确定");
            }
        }
        else
        {
            AlrDialog_Show.alertDialog(context,"提示","请设置正确格式mcc!","确定");
        }
        Log.v("sid1", sid);
        Log.v("nid1", nid);
        Log.v("bid1", bid);
        return start_flg;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
//        if(locationService!=null)
//            locationService.registerListener(mListener);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK&& event.getAction() == KeyEvent.ACTION_DOWN)
        {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.title_dialog_exit_confirm))
                    .setMessage(getString(R.string.message_confirm_to_exit))
                    .setPositiveButton(android.R.string.ok,
                            new android.content.DialogInterface.OnClickListener()
                            {
                                public void onClick(final DialogInterface dialog, final int which)
                                {
                                    MyApplication.getInstance().exit();
                                }
                            }).setNegativeButton(android.R.string.cancel, null).show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
//    private BDLocationListener mListener = new BDLocationListener() {
//
//        @Override
//        public void onReceiveLocation(BDLocation location) {
//            // TODO Auto-generated method stub
//            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
//                if (location == null)
//                    return;
//                // 设置定位 位置信息
//                ((MyApplication) context.getApplicationContext()).locData = new MyLocationData.Builder()
//                        .accuracy(location.getRadius()).direction(100)// 精度范围
//                        .latitude(location.getLatitude())// 经度
//                        .longitude(location.getLongitude()).build();// 纬度
//                Log.e("MainActivity",MyApplication.getInstance().getLocData().latitude+","+MyApplication.getInstance().getLocData().longitude);
//                mBaiduMap.setMyLocationData(MyApplication.getInstance().getLocData());
////                LatLng ll = new LatLng(location.getLatitude(),
////                        location.getLongitude());
////                MapStatus.Builder builder = new MapStatus.Builder();
////                builder.target(ll).zoom(18.0f);
////                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//
//                StringBuffer sb = new StringBuffer(256);
//                sb.append("time : ");
//                /**
//                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
//                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
//                 */
//                sb.append(location.getTime());
//                sb.append("\nerror code : ");
//                sb.append(location.getLocType());
//                sb.append("\nlatitude : ");
//                sb.append(location.getLatitude());
//                sb.append("\nlontitude : ");
//                sb.append(location.getLongitude());
//                sb.append("\nradius : ");
//                sb.append(location.getRadius());
//                sb.append("\nCountryCode : ");
//                sb.append(location.getCountryCode());
//                sb.append("\nCountry : ");
//                sb.append(location.getCountry());
//                sb.append("\ncitycode : ");
//                sb.append(location.getCityCode());
//                sb.append("\ncity : ");
//                sb.append(location.getCity());
//                sb.append("\nDistrict : ");
//                sb.append(location.getDistrict());
//                sb.append("\nStreet : ");
//                sb.append(location.getStreet());
//                sb.append("\naddr : ");
//                sb.append(location.getAddrStr());
//                sb.append("\nDescribe: ");
//                sb.append(location.getLocationDescribe());
//                sb.append("\nDirection(not all devices have value): ");
//                sb.append(location.getDirection());
//                sb.append("\nPoi: ");
//                //*****************************
//                point=new LocationPoint();
//                point.setLat(location.getLatitude());
//                point.setLon(location.getLongitude());
//                if(location.getAddrStr()!=null){
//                    point.setAddress(location.getAddrStr()+" "+location.getLocationDescribe());
//                }else{
//
//                }
//                Log.e("MainActivity",point.getLat()+","+point.getLon()+","+point.getAddress());
////                NeighborCell.getInstance(context).sendObservableNotice(new NoticeData(MessageType.AUTO_COLLEC_FINISH,""));
//                //*********************************
//                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
//                    for (int i = 0; i < location.getPoiList().size(); i++) {
//                        Poi poi = (Poi) location.getPoiList().get(i);
//                        sb.append(poi.getName() + ";");
//                    }
//                }
//                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
//                    sb.append("\nspeed : ");
//                    sb.append(location.getSpeed());// 单位：km/h
//                    sb.append("\nsatellite : ");
//                    sb.append(location.getSatelliteNumber());
//                    sb.append("\nheight : ");
//                    sb.append(location.getAltitude());// 单位：米
//                    sb.append("\ndescribe : ");
//                    sb.append("gps定位成功");
//                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
//                    // 运营商信息
//                    sb.append("\noperationers : ");
//                    sb.append(location.getOperators());
//                    sb.append("\ndescribe : ");
//                    sb.append("网络定位成功");
//                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
//                    sb.append("\ndescribe : ");
//                    sb.append("离线定位成功，离线定位结果也是有效的");
//                } else if (location.getLocType() == BDLocation.TypeServerError) {
//                    sb.append("\ndescribe : ");
//                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
//                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//                    sb.append("\ndescribe : ");
//                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
//                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//                    sb.append("\ndescribe : ");
//                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
//                }
//                Log.d("Loc",sb.toString());
//            }
//        }
//
//    };
}
