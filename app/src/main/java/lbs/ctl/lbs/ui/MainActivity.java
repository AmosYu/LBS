package lbs.ctl.lbs.ui;

import android.Manifest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import lbs.ctl.lbs.R;
import lbs.ctl.lbs.bluetooth.BluetoothConn;
import lbs.ctl.lbs.bluetooth.BluetoothState;
import lbs.ctl.lbs.database.DbAcessImpl;
import lbs.ctl.lbs.luce.AllCellInfo;
import lbs.ctl.lbs.luce.CellType;
import lbs.ctl.lbs.luce.LuceCellInfo;
import lbs.ctl.lbs.luce.RevThread;
import lbs.ctl.lbs.luce.WifiInfo;
import lbs.ctl.lbs.utils.LocationPoint;
import lbs.ctl.lbs.BaiduMap.LocationService;

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
        initGpsAndLog();
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
        mMapView.setVisibility(View.INVISIBLE);
        GetMyLocation();
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

    private void initMap() {
        mBaiduMap = mMapView.getMap();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
    }

    private void initView() {
        mMapView=(MapView) findViewById(R.id.bmapView);

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

    private TextView latitudeTv, longitudeTv;

    private void initGpsAndLog() {
        latitudeTv = (TextView) findViewById(R.id.main_latitude_tv);
        longitudeTv = (TextView) findViewById(R.id.main_longitude_tv);

    }

    private void updateGps() {
        latitudeTv.setText("纬度：" + allCellInfo.getLatitude());
        longitudeTv.setText("经度：" + allCellInfo.getLongitude());
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
            }
        });
        ciFourByteCheckBox = (CheckBox) findViewById(R.id.main_checkbox_ci);
        ciFourByteCheckBox.setChecked(cellInfoAdapter.isShortCi());
        ciFourByteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cellInfoAdapter.setShortCi(isChecked);
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
                updateGps();
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


    private Button saveDataBtn, offlineMapBtn, queryDataBtn, importBtn;
    private Button attentionBtn, uploadBtn, exportBtn, exitBtn;
    private TextView filaNameTv;

    /**
     * 初始换功能按钮
     */
    private void initButton() {
        exitBtn = (Button)findViewById(R.id.main_exit_btn);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("程序退出")
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(final DialogInterface dialog,final int which) {
                                        MyApplication.getInstance().exit();
                                    }
                                }).setNegativeButton(android.R.string.cancel, null).show();
            }
        });
        //注意事项
        attentionBtn = (Button) findViewById(R.id.main_attention_btn);
        attentionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, MapActivity.class);
//                startActivity(intent);
            }
        });
        //数据上传
        uploadBtn = (Button) findViewById(R.id.main_upload_btn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("类型", "上传");
                intent.setClass(MainActivity.this, FileImportActivity.class);
                startActivity(intent);
            }
        });
        //离线地图
        offlineMapBtn = (Button) findViewById(R.id.main_offlinemap_btn);
        offlineMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(context, OffLineActivity.class);
//                startActivity(intent);
            }
        });
        filaNameTv = (TextView) findViewById(R.id.main_filename_tv);
        //数据保存
        // mark==文件名==用户标记的地点
        saveDataBtn = (Button) findViewById(R.id.main_data_save_file_btn);
        saveDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!allCellInfo.isDataSaveToDb()) {
                    final EditText input = new EditText(context);
                    input.setMaxLines(1);
                    String userMark = allCellInfo.getCurrentTime();
                    input.setText(userMark);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("添加文件名")
                            .setMessage("请在下框中输入文件名，数据保存于此文件中，文件名只能包含是中文、字母、数字。\r\n默认情况采用当前时间命名。")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(input)
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String mark = input.getText().toString();
                            if (mark.length() == 0) {
                                mark = allCellInfo.getCurrentTime();
                            }
                            DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
                            dbAcess.insertMark(mark);
                            allCellInfo.setUserMark(mark);
                            allCellInfo.setDataSaveToDb(true);
                            saveDataBtn.setText("停止保存");
                            filaNameTv.setText("当前数据保存于文件" + allCellInfo.getUserMark() + "中,如需停止保存数据请点击停止保存按钮");
                        }
                    });
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("停止保存数据")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    builder.setPositiveButton("停止", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            allCellInfo.setDataSaveToDb(false);
                            saveDataBtn.setText("保存数据");
                            filaNameTv.setText("数据保存文件名：当前数据未保存，如需保存数据请点击数据保存按钮");
                        }
                    });
                    builder.show();
                }
            }
        });

        //轨迹与查询
        queryDataBtn = (Button) findViewById(R.id.main_query_btn);
        queryDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(context, FindDataActivity.class);
                startActivity(intent);
            }
        });
        //导出按钮
        exportBtn = (Button) findViewById(R.id.main_export_btn);
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("类型", "导出");
                intent.setClass(MainActivity.this, FileImportActivity.class);
                startActivity(intent);
            }
        });

        //导入按钮
        importBtn = (Button) findViewById(R.id.main_import_btn);
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("类型", "导入");
                intent.setClass(MainActivity.this, FileImportActivity.class);
                startActivity(intent);
            }
        });

    }

}
