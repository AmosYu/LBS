package lbs.ctl.lbs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import lbs.ctl.lbs.InfoWindowHolder;
import lbs.ctl.lbs.bluetooth.BluetoothConn;
import lbs.ctl.lbs.bluetooth.BluetoothState;
import lbs.ctl.lbs.database.DbAcessImpl;
import lbs.ctl.lbs.luce.AllCellInfo;
import lbs.ctl.lbs.luce.CellType;
import lbs.ctl.lbs.luce.LuceCellInfo;
import lbs.ctl.lbs.luce.RevThread;
import lbs.ctl.lbs.luce.WifiInfo;
import lbs.ctl.lbs.utils.Gps2BaiDu;
import lbs.ctl.lbs.utils.LocationPoint;
import lbs.ctl.lbs.BaiduMap.LocationService;

import lbs.ctl.lbs.R;

import static lbs.ctl.lbs.luce.CellType.CDMA;
import static lbs.ctl.lbs.luce.CellType.GSM_M;
import static lbs.ctl.lbs.luce.CellType.GSM_U;
import static lbs.ctl.lbs.luce.CellType.LTE;
import static lbs.ctl.lbs.luce.CellType.WIFI;

public class MainActivity extends Activity implements Observer {
    private ImageView logo_view;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    private LocationService locationService=null;
    private LocationPoint point=null;
    private Context context;
    /**
     * 蓝牙连接控制，数据发送接收功能类
     */
    private BluetoothConn bluetoothConn;
    /**
     * 数据接收线程
     */
    private RevThread revThread;
    /**
     * 数据接收临时存储类
     */
    private AllCellInfo allCellInfo;

    private ProgressDialog progressDialog;

    private View mapRelaView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        MyApplication.getInstance().addActivity(this);
        context=this;

        initCdma();
        initTd();
        initWcdma();
        initListView();
        initTopView();
        initRadioButton();
        initCellSwitchWifi();
        initButton();


        bluetoothConn = new BluetoothConn("LC_", 8000);
        bluetoothConn.addObserver(this);
        allCellInfo = new AllCellInfo(context);
        allCellInfo.addObserver(this);


        initView();
        initMap();
        mapRelaView.setVisibility(View.INVISIBLE);
        GetMyLocation();

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("正在初始化当前任务的轨迹，请稍等！");

        startTaskBtn.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.show();
                initTrackList(AllCellInfo.userMark);
                progressDialog.cancel();

            }
        });

        initMapTopView();
    }

    private void GetMyLocation() {
        new Thread(new Runnable() {
            public void run() {
                locationService = ((MyApplication)context.getApplicationContext()).locationService;
                locationService.registerListener(mListener);
                locationService.setLocationOption(locationService.getDefaultLocationClientOption());
                locationService.start();
            }
        }).start();
    }
    private InfoWindow mInfoWindow;
    private LinearLayout baidumap_infowindow;
    private MarkerOnInfoWindowClickListener markerListener;
    private void initMap() {
        baidumap_infowindow = (LinearLayout) LayoutInflater.from (context).inflate (R.layout.baidu_map_infowindow, null);
        mBaiduMap = mMapView.getMap();
        markerListener = new MarkerOnInfoWindowClickListener ();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if(showBtsType.equals("全部")){
                    return false;
                }
                createInfoWindow(baidumap_infowindow,(LuceCellInfo)  marker.getExtraInfo ().get ("marker"));
                final LatLng ll = marker.getPosition();
                mInfoWindow = new InfoWindow (BitmapDescriptorFactory.fromView (baidumap_infowindow), ll, -47, markerListener);
                mBaiduMap.showInfoWindow(mInfoWindow);
                return true;
            }
        });
    }

    private View maplayoutView;
    private void initView() {
        maplayoutView = findViewById(R.id.main_map_rela);
        mMapView=(MapView) findViewById(R.id.bmapView);
        mapRelaView = findViewById(R.id.main_map_rela);
        logo_view=(ImageView)findViewById(R.id.logo_imageview);
        logo_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,MapFindActivity.class);
                intent.putExtra("point",point);
//                Bundle bundle=new Bundle();
//                bundle.put
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if(locationService!=null)
            locationService.registerListener(mListener);
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
    public static double baiduLatitude = 0.0, baiduLongitude = 0.0;
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                if (location == null)
                    return;
                // 设置定位 位置信息
                ((MyApplication) context.getApplicationContext()).locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius()).direction(100)// 精度范围
                        .latitude(location.getLatitude())// 经度
                        .longitude(location.getLongitude()).build();// 纬度
                Log.e("MainActivity",MyApplication.getInstance().getLocData().latitude+","+MyApplication.getInstance().getLocData().longitude);
                mBaiduMap.setMyLocationData(MyApplication.getInstance().getLocData());
//                LatLng ll = new LatLng(location.getLatitude(),
//                        location.getLongitude());
//                MapStatus.Builder builder = new MapStatus.Builder();
//                builder.target(ll).zoom(18.0f);
//                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                    if (location == null)
                        return;
                    if (location.getAddrStr() != null) {
                        baiduLatitude = location.getLatitude();
                        baiduLongitude = location.getLongitude();
                    } else {
                        baiduLatitude = 0;
                        baiduLongitude = 0;
                    }
                }
                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nerror code : ");
                sb.append(location.getLocType());
                sb.append("\nlatitude : ");
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");
                sb.append(location.getLongitude());
                sb.append("\nradius : ");
                sb.append(location.getRadius());
                sb.append("\nCountryCode : ");
                sb.append(location.getCountryCode());
                sb.append("\nCountry : ");
                sb.append(location.getCountry());
                sb.append("\ncitycode : ");
                sb.append(location.getCityCode());
                sb.append("\ncity : ");
                sb.append(location.getCity());
                sb.append("\nDistrict : ");
                sb.append(location.getDistrict());
                sb.append("\nStreet : ");
                sb.append(location.getStreet());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\nDescribe: ");
                sb.append(location.getLocationDescribe());
                sb.append("\nDirection(not all devices have value): ");
                sb.append(location.getDirection());
                sb.append("\nPoi: ");
                //*****************************
                point=new LocationPoint();
                point.setLat(location.getLatitude());
                point.setLon(location.getLongitude());
                if(location.getAddrStr()!=null){
                    point.setAddress(location.getAddrStr()+" "+location.getLocationDescribe());
                }else{

                }
                Log.e("MainActivity",point.getLat()+","+point.getLon()+","+point.getAddress());
//                NeighborCell.getInstance(context).sendObservableNotice(new NoticeData(MessageType.AUTO_COLLEC_FINISH,""));
                //*********************************
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 单位：米
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    sb.append("\noperationers : ");
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                Log.d("Loc",sb.toString());
            }
        }

    };

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


    @Override
    public void update(Observable observable, final Object data) {
        if (data instanceof CellType) {
            devConTv.post(new Runnable() {
                @Override
                public void run() {
                    if(((CellType)data)==CellType.GPS&&allCellInfo.isDataSaveToDb()){
                        if(allCellInfo.getLatitude()!=0||allCellInfo.getLongitude()!=0) {
                            LatLng latLng = Gps2BaiDu.gpsToBaidu(allCellInfo.getLatitude(), allCellInfo.getLongitude());
                            boolean add = addPointToTrackList(latLng);
                            if(add)
                                addMarkOnMap(latLng);
                        }
                    }
                    updateAllData((CellType) data);
                }
            });
        } else if (data instanceof BluetoothState) {
            BluetoothState state = (BluetoothState) data;
            updateDevConState(state);
            if (data == BluetoothState.CONNECTED) {
                revThread = null;
                revThread = new RevThread(allCellInfo, bluetoothConn, context);
                new Thread(revThread).start();
            } else {
                if (revThread != null) {
                    revThread.isTerminated();
                    revThread = null;
                }
            }
        }
    }

    private void updateDevConState(final BluetoothState state) {
        if (devConTv != null) {
            devConTv.post(new Runnable() {
                @Override
                public void run() {
                    devConTv.setText(state.toString());
                    startTaskBtn.setEnabled(state==BluetoothState.CONNECTED);
                }
            });
        }
    }



    private TextView cdmaSidTv, cdmaNidTv, cdmaBidTv, cdmaArfcnTv, cdmaPnTv;
    private TextView wcdmaLacTv, wcdmaCiTv, wcdmaArfcnTv, wcdmaPscTv, wcdmaRssiTv;
    private TextView tdLacTv, tdCiTv, tdPscTv, tdRssiTv, tdArfcnTv;

    private void initCdma() {
        cdmaSidTv = (TextView) findViewById(R.id.main_cdma_sid);
        cdmaNidTv = (TextView) findViewById(R.id.main_cdma_nid);
        cdmaBidTv = (TextView) findViewById(R.id.main_cdma_bid);
        cdmaArfcnTv = (TextView) findViewById(R.id.main_cdma_arfcn);
        cdmaPnTv = (TextView) findViewById(R.id.main_cdma_pn);
    }

    private void updateCdma(LuceCellInfo luceCellInfo) {
        cdmaSidTv.setText(Integer.toString(luceCellInfo.getLac_sid()));
        cdmaNidTv.setText(Integer.toString(luceCellInfo.getCi_nid()));
        cdmaBidTv.setText(Integer.toString(luceCellInfo.getBid()));
        cdmaArfcnTv.setText(Integer.toString(luceCellInfo.getArfcn()));
        cdmaPnTv.setText(Integer.toString(luceCellInfo.getPn_bsic_pci_psc()));
    }

    private void initWcdma() {
        wcdmaLacTv = (TextView) findViewById(R.id.main_wcdma_lac);
        wcdmaCiTv = (TextView) findViewById(R.id.main_wcdma_ci);
        wcdmaArfcnTv = (TextView) findViewById(R.id.main_wcdma_arfcn);
        wcdmaPscTv = (TextView) findViewById(R.id.main_wcdma_psc);
        wcdmaRssiTv = (TextView) findViewById(R.id.main_wcdma_rssi);
    }

    /**
     * WCDMA参数更新函数，内部涉及LAC，CI的十六进制与十进制显示
     * 和CI的低16bit和28bit显示
     * @param luceCellInfo
     */
    private void updateWcdma(LuceCellInfo luceCellInfo) {
        String lac = "";
        String ci = "";
        if (cellInfoAdapter.isHex()) {
            lac = Integer.toHexString(luceCellInfo.getLac_sid());
            if (cellInfoAdapter.isShortCi()) {
                ci = Integer.toHexString(luceCellInfo.getCi_nid() & 0x0000FFFF);
            } else {
                ci = Integer.toHexString(luceCellInfo.getCi_nid());
            }
        } else {
            lac = Integer.toString(luceCellInfo.getLac_sid());
            if (cellInfoAdapter.isShortCi()) {
                ci = Integer.toString(luceCellInfo.getCi_nid() & 0x0000FFFF);
            } else {
                ci = Integer.toString(luceCellInfo.getCi_nid());
            }
        }
        wcdmaLacTv.setText(lac);
        wcdmaCiTv.setText(ci);
        wcdmaRssiTv.setText(Integer.toString(luceCellInfo.getRssi()));
        wcdmaArfcnTv.setText(Integer.toString(luceCellInfo.getArfcn()));
        wcdmaPscTv.setText(Integer.toString(luceCellInfo.getPn_bsic_pci_psc()));
    }

    private void initTd() {
        tdLacTv = (TextView) findViewById(R.id.main_td_lac);
        tdCiTv = (TextView) findViewById(R.id.main_td_ci);
        tdArfcnTv = (TextView) findViewById(R.id.main_td_arfcn);
        tdPscTv = (TextView) findViewById(R.id.main_td_psc);
        tdRssiTv = (TextView) findViewById(R.id.main_td_rssi);
    }

    /**
     * TD数据更新函数,内部功能与WCDMA相同
     * @param luceCellInfo
     */
    private void updateTd(LuceCellInfo luceCellInfo) {
        String lac = "";
        String ci = "";
        if (cellInfoAdapter.isHex()) {
            lac = Integer.toHexString(luceCellInfo.getLac_sid());
            if (cellInfoAdapter.isShortCi()) {
                ci = Integer.toHexString(luceCellInfo.getCi_nid() & 0x0000FFFF);
            } else {
                ci = Integer.toHexString(luceCellInfo.getCi_nid());
            }
        } else {
            lac = Integer.toString(luceCellInfo.getLac_sid());
            if (cellInfoAdapter.isShortCi()) {
                ci = Integer.toString(luceCellInfo.getCi_nid() & 0x0000FFFF);
            } else {
                ci = Integer.toString(luceCellInfo.getCi_nid());
            }
        }
        tdLacTv.setText(lac);
        tdCiTv.setText(ci);
        tdRssiTv.setText(Integer.toString(luceCellInfo.getRssi()));
        tdArfcnTv.setText(Integer.toString(luceCellInfo.getArfcn()));
        tdPscTv.setText(Integer.toString(luceCellInfo.getPn_bsic_pci_psc()));
    }

    /**
     * GSM,TD_LTE的ListView数据显示源
     */
    private ArrayList<LuceCellInfo> cellList;
    /**
     * WIFI显示列表的数据源
     */
    private ArrayList<WifiInfo> wifiList;
    /**
     * GSM LTE 列表适配器
     */
    private CellInfoAdapter cellInfoAdapter;
    /**
     * WIFI 列表适配器
     */
    private WifiInfoAdapter wifiInfoAdapter;
    private ListView listView;

    private void initListView() {
        cellList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.main_cell_listview);
        cellInfoAdapter = new CellInfoAdapter(context, cellList, R.layout.cell_list_item);
        listView.setAdapter(cellInfoAdapter);
        wifiList = new ArrayList<>();
        wifiInfoAdapter = new WifiInfoAdapter(context, wifiList, R.layout.wifi_item);
    }

    private TextView devConTv;
    private CheckBox hexCheckBox, ciFourByteCheckBox;

    /**
     * 顶端 进制显示的CHECK
     */
    private void initTopView() {
        hexCheckBox = (CheckBox) findViewById(R.id.main_checkbox_hex);
        hexCheckBox.setChecked(cellInfoAdapter.isHex());
        hexCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cellInfoAdapter.setHex(isChecked);
                cellInfoAdapter.notifyDataSetChanged();
            }
        });
        ciFourByteCheckBox = (CheckBox) findViewById(R.id.main_checkbox_ci);
        ciFourByteCheckBox.setChecked(cellInfoAdapter.isShortCi());
        ciFourByteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cellInfoAdapter.setShortCi(isChecked);
                cellInfoAdapter.notifyDataSetChanged();
            }
        });
        devConTv = (TextView) findViewById(R.id.main_dev_con_state);
    }

    private TextView listViewlable, listViewLac, listViewCi, listViewArfcn, listViewBsic, lineSecond, lineFourth;

    private void initCellSwitchWifi() {
        listViewlable = (TextView) findViewById(R.id.main_list_label);
        listViewLac = (TextView) findViewById(R.id.main_list_lac);
        listViewCi = (TextView) findViewById(R.id.main_list_ci);
        listViewArfcn = (TextView) findViewById(R.id.main_list_arfcn);
        listViewBsic = (TextView) findViewById(R.id.main_list_bsic);
        lineSecond = (TextView) findViewById(R.id.main_list_line2);
        lineFourth = (TextView) findViewById(R.id.main_list_line4);
    }


    private CellType currentTypeInListView = GSM_M;
    private RadioGroup radioGroup;

    private void initRadioButton() {
        radioGroup = (RadioGroup) findViewById(R.id.main_radiogroup);
        radioGroup.check(R.id.main_gmobile_rb);
        currentTypeInListView = GSM_M;
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.main_gmobile_rb:
                        currentTypeInListView = GSM_M;
                        switchToCell(GSM_M);
                        break;
                    case R.id.main_gunicom_rb:
                        currentTypeInListView = GSM_U;
                        switchToCell(GSM_U);
                        break;
                    case R.id.main_lte_rb:
                        currentTypeInListView = LTE;
                        switchToCell(LTE);
                        break;
                    case R.id.main_wifi_rb:
                        currentTypeInListView = WIFI;
                        switchToWifi();
                        break;
                }
            }
        });
    }


    private void updateAllData(CellType type) {
        switch (type) {
            case GPS:
//                String positionMsg =
//                showCurrentPositionTV.set
                break;
            case GSM_M:
            case GSM_U:
            case LTE:
                if (currentTypeInListView != WIFI)
                    updateCellListView(currentTypeInListView);
                break;
            case WIFI:
                if (currentTypeInListView == WIFI)
                    updateWifiListView(allCellInfo.getWifiList());
                break;
            case WCDMA:
                updateWcdma(allCellInfo.getWcdmaInfo());
                break;
            case CDMA:
                updateCdma(allCellInfo.getCdmaInfo());
                break;
            case TDSCDMA:
                updateTd(allCellInfo.getTdInfo());
                break;
        }
    }

    private void updateCellListView(CellType cellType) {
        listView.setAdapter(cellInfoAdapter);
        this.cellList.clear();
        this.cellList.addAll(allCellInfo.getCurrentCellInListView(cellType));
        cellInfoAdapter.notifyDataSetChanged();
    }

    private void updateWifiListView(ArrayList<WifiInfo> wifiList) {
        listView.setAdapter(wifiInfoAdapter);
        this.wifiList.clear();
        this.wifiList.addAll(wifiList);
        wifiInfoAdapter.notifyDataSetChanged();
    }

    /**
     * 将列表切换到某一特定制式，
     * 彼岸表头的参数名称，显示或隐藏控件
     * @param cellType
     */
    private void switchToCell(CellType cellType) {
        if(cellType.equals(GSM_M)){
            listViewlable.setText("GSM移动");
        }
        else if(cellType.equals(GSM_U)){
            listViewlable.setText("GSM联通");
        }
        else{
            listViewlable.setText(cellType.toString());
        }
        LinearLayout.LayoutParams lp= new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
        listViewLac.setLayoutParams(lp);

        listViewCi.setVisibility(View.VISIBLE);
        listViewArfcn.setLayoutParams(lp);
        listViewBsic.setVisibility(View.VISIBLE);

        lineSecond.setVisibility(View.VISIBLE);
        lineFourth.setVisibility(View.VISIBLE);
        listViewLac.setText("LAC");
        listViewCi.setText("CI");
        if (cellType == LTE)
            listViewBsic.setText("PCI");
        else
            listViewBsic.setText("BSIC");
        listViewArfcn.setText("ARFCN");
        listView.setAdapter(cellInfoAdapter);
        this.cellList.clear();
        this.cellList.addAll(allCellInfo.getCurrentCellInListView(cellType));
        cellInfoAdapter.notifyDataSetChanged();
    }

    /**
     * 将列表显示形式切换到WIFI状态
     */
    private void switchToWifi() {
        listViewlable.setText(WIFI.toString());
        listViewLac.setText("MAC地址");
        LinearLayout.LayoutParams lp= new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2.0f);
        listViewLac.setLayoutParams(lp);

        listViewCi.setVisibility(View.GONE);
        listViewArfcn.setLayoutParams(lp);
        listViewBsic.setVisibility(View.GONE);
        lineSecond.setVisibility(View.GONE);
        lineFourth.setVisibility(View.GONE);
        listViewArfcn.setText("名称");
        listView.setAdapter(wifiInfoAdapter);
        wifiList.clear();
        wifiList.addAll(allCellInfo.getWifiList());
        wifiInfoAdapter.notifyDataSetChanged();
    }


    private TextView showCurrentPositionTV;
    private Button startTaskBtn,dataMapSwitchBtn,showFindBtsBtn;
    /**
     * 初始换功能按钮
     */
    private void initButton() {

        startTaskBtn = (Button) findViewById(R.id.main_start_task_btn);
        startTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!allCellInfo.isDataSaveToDb()) {
                    allCellInfo.setDataSaveToDb(true);
                    startTaskBtn.setText("停止任务");
                } else {
                    allCellInfo.setDataSaveToDb(false);
                    startTaskBtn.setText("开始任务");
                }
            }
        });

        //轨迹与查询
        dataMapSwitchBtn = (Button) findViewById(R.id.main_show_map_btn);
        dataMapSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataMapSwitchBtn.getText().toString().equals("显示地图")){
                    dataMapSwitchBtn.setText("显示数据");
                    maplayoutView.setVisibility(View.VISIBLE);
                }
                else {
                    dataMapSwitchBtn.setText("显示地图");
                    maplayoutView.setVisibility(View.INVISIBLE);
                }
            }
        });

        showFindBtsBtn = (Button)findViewById(R.id.main_find_bts_btn);
        showFindBtsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context,MapFindActivity.class);
                startActivity(intent);
            }
        });
        showCurrentPositionTV = (TextView)findViewById(R.id.tv_show_position);
    }

    private LinkedList<LatLng>  trackList = new LinkedList<>();
    private LinkedList<Map<String,Object>>  btsList = new LinkedList<>();


    private boolean addPointToTrackList(LatLng point){

        if(trackList.size()==0){
            trackList.add(point);
            return true;
        }
        else{
            if(isEnableAdd(point)) {
                trackList.add(point);
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否能将坐标点添加至轨迹集合，20米内不添加，20米外添加
     * @param latLng
     * @return
     */
    private boolean isEnableAdd(LatLng latLng){
        for(LatLng latLngInList:trackList){
            if(DistanceUtil.getDistance(latLng,latLngInList)<10){
                return false;
            }
        }
        return true;
    }

    //将可显示的点添加至地图上
    private void addMarkOnMap(LatLng latLng){

        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.iconmarka);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap);
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
    }

//    private void addMarkOnMap(LuceCellInfo luceCellInfo,BitmapDescriptor bitmap){
//        LatLng latLng = luceCellInfo.getBaiduPoint();
//        String title = luceCellInfo.getLac_sid()+","+luceCellInfo.getCi_nid()+","+luceCellInfo.getBid();
////        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.iconmarka);
//        //构建MarkerOption，用于在地图上添加Marker
//        OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap).title(title);
//        //定义地图状态
//        MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
//        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
//        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
//        //改变地图状态
//        mBaiduMap.setMapStatus(mMapStatusUpdate);
//        //在地图上添加Marker，并显示
//        mBaiduMap.addOverlay(option);
//    }

    private void initTrackList(String taskName){

        DbAcessImpl db=DbAcessImpl.getDbInstance(context);

        LinkedList<LatLng> list = new LinkedList<>();

        list.addAll(db.selectByFileGetPoint(taskName));

        for(LatLng latLng:list){
            addPointToMap(latLng);
        }
    }


    /**
     * 添加坐标点到地图上
     * @param latLng
     */
    private void addPointToMap(LatLng latLng){
        boolean add = addPointToTrackList(latLng);
        if(add) {
            Message msg = new Message();
            msg.arg1 = 1010;
            msg.obj = latLng;
            handler.sendMessage(msg);
        }
    }


    private Spinner modeSpinner;
    private String showBtsType = "全部";
    private ArrayAdapter<String> typeSpinnerAdapter;
    private void initMapTopView() {

        modeSpinner=(Spinner)findViewById(R.id.spinner_marker_type);
        Resources res = getResources ();
        String[] modes = res.getStringArray(R.array.mode_arrays);
        typeSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, modes);
        typeSpinnerAdapter.setDropDownViewResource(R.layout.drop_down_item);
        modeSpinner.setAdapter(typeSpinnerAdapter);
        modeSpinner.setOnItemSelectedListener(typeLis);

    }

    AdapterView.OnItemSelectedListener typeLis = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i){
                case 0:
                    showBtsType = "全部";
                    break;
                case 1:
                    showBtsType = CellType.GSM_M.toString();
                    break;
                case 2:
                    showBtsType = CellType.GSM_U.toString();
                    break;
                case 3:
                    showBtsType = CellType.WCDMA.toString();
                    break;
                case 4:
                    showBtsType = CellType.CDMA.toString();
                    break;
                case 5:
                    showBtsType = CellType.TDSCDMA.toString();
                    break;
                case 6:
                    showBtsType = CellType.LTE.toString();
                    break;
            }
            if(showBtsType.equals("全部")){
                initTack();
            }else {
                initBts(showBtsType);
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    /**
     * 任务下的所有制定制式的数据，内层集合中每个List中是同LAC的基站
     */
    private LinkedList<ArrayList<LuceCellInfo>> btsInfoList = new LinkedList<>();

    /**
     * 将基站数据添加至区分LAC的btsInfoList中
     * @param luceCellInfo
     */
    private void addBsInfoToList(LuceCellInfo luceCellInfo){
        for(ArrayList<LuceCellInfo> sameLacList:btsInfoList){
            if(luceCellInfo.getBtsType().equals(CDMA.toString())){
                if(sameLacList.get(0).getBid()==luceCellInfo.getBid()){
                    sameLacList.add(luceCellInfo);
                    return;
                }
            }
            else{
                if(sameLacList.get(0).getLac_sid()==luceCellInfo.getLac_sid()){
                    sameLacList.add(luceCellInfo);
                    return;
                }
            }
        }
        ArrayList<LuceCellInfo> newList = new ArrayList<>();
        newList.add(luceCellInfo);
        btsInfoList.add(newList);
    }

    private void  addBtsInfoToMap(){

        int i = 0;
        for(ArrayList<LuceCellInfo> luceCellInfos:btsInfoList){
            for(LuceCellInfo luceCellInfo : luceCellInfos){
                if(!addPointToTrackList(luceCellInfo.getBaiduPoint())){
                    continue;
                }
                Message msg = new Message();
                msg.obj  = luceCellInfo;
                msg.arg1 = 101010;
                msg.arg2 = i;
                handler.sendMessage(msg);
            }
            i++;
        }
    }


    private void initTack(){
        progressDialog.setMessage("正在加载数据请稍等...");
        progressDialog.show();
        new Thread(){
            @Override
            public void run() {
                btsInfoList.clear();
                trackList.clear();
                mBaiduMap.clear();
                initTrackList(AllCellInfo.userMark);
            }
        }.start();
    }
    private void initBts(final String btsType ){
        progressDialog.setMessage("正在加载数据请稍等...");
        progressDialog.show();
        new Thread(){
            public void run() {
                DbAcessImpl db=DbAcessImpl.getDbInstance(context);
                LinkedList<LuceCellInfo> luceCellInfos = new LinkedList<LuceCellInfo>();

                luceCellInfos.addAll(db.selectByNameAndType(AllCellInfo.userMark,btsType));
                btsInfoList.clear();
                trackList.clear();
                mBaiduMap.clear();
                for(LuceCellInfo luceCellInfo:luceCellInfos){
                    addBsInfoToList(luceCellInfo);
                }
                addBtsInfoToMap();
            }
        }.start();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.arg1 == 101010){
                progressDialog.dismiss();
                LuceCellInfo luceCellInfo =(LuceCellInfo) msg.obj;

                LatLng latLng = luceCellInfo.getBaiduPoint();
                String title = luceCellInfo.getLac_sid()+","+luceCellInfo.getCi_nid()+","+luceCellInfo.getBid();
                BitmapDescriptor bitmap = getBitmap(msg.arg2);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap).title(title);
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
                //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                //改变地图状态
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                //在地图上添加Marker，并显示
                Marker marker = (Marker)mBaiduMap.addOverlay(option);
                // 将信息保存
                Bundle bundle = new Bundle ();
                bundle.putSerializable ("marker", luceCellInfo);
                marker.setExtraInfo (bundle);
            }
            else if(msg.arg1==1010){
                progressDialog.dismiss();
                LatLng latLng = (LatLng)msg.obj;
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.iconmarka);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap);
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
                //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                //改变地图状态
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                //在地图上添加Marker，并显示
                mBaiduMap.addOverlay(option);
            }
        }
    };


    private BitmapDescriptor getBitmap(int id){
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p1);
        switch (id%10){
            case 0:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p1);
                break;
            case 1:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p2);
                break;
            case 2:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p3);
                break;
            case 3:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p4);
                break;
            case 4:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p5);
                break;
            case 5:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p6);
                break;
            case 6:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p7);
                break;
            case 7:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p8);
                break;
            case 8:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p9);
                break;
            case 9:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p10);
                break;
        }
        return bitmap;
    }


    private void createInfoWindow(LinearLayout baidumap_infowindow,LuceCellInfo luceCellInfo){
         InfoWindowHolder holder = null;
//        if(baidumap_infowindow.getTag () == null){
             holder = new InfoWindowHolder ();
             holder.tv_title = (TextView) baidumap_infowindow.findViewById (R.id.map_window_title);
             holder.tv_content = (TextView) baidumap_infowindow.findViewById (R.id.map_window_content);

//        }
//        holder = (InfoWindowHolder) baidumap_infowindow.getTag ();
        String title = null;
        StringBuffer sb = new StringBuffer();
        sb.append("邻区\n");
        if(luceCellInfo.getBtsType().equals(CDMA.toString())){
            title = luceCellInfo.getBtsType()+"大区号：("+luceCellInfo.getLac_sid()+","+luceCellInfo.getCi_nid()+")"+"，小区号："+luceCellInfo.getBid()+"，场强："+luceCellInfo.getRssi();
        }
        else{
            title = luceCellInfo.getBtsType()+"，大区号："+luceCellInfo.getLac_sid()+"，小区号："+luceCellInfo.getCi_nid()+"，场强："+luceCellInfo.getRssi();
            DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
            ArrayList<LuceCellInfo> list = new ArrayList<>();
            list.addAll(dbAcess.findOnlySameLacBts(String.valueOf(luceCellInfo.getLac_sid()),luceCellInfo.getBtsType(),luceCellInfo.getLatitude(),luceCellInfo.getLongitude(),AllCellInfo.userMark));
            for(LuceCellInfo nCellinfo:list){
                if(nCellinfo.getCi_nid()!=luceCellInfo.getCi_nid()){
                    sb.append("小区号："+nCellinfo.getCi_nid()+",场强："+nCellinfo.getRssi()+"\n");
                }
            }
            holder.tv_content.setText (sb.toString());
        }
        holder.tv_title.setText (title);
        }
    private final class  MarkerOnInfoWindowClickListener implements InfoWindow.OnInfoWindowClickListener
    {
        @Override
        public void onInfoWindowClick(){
            mBaiduMap.hideInfoWindow();
        }
   }

}