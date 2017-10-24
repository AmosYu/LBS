package lbs.ctl.lbs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;

import lbs.ctl.lbs.util.LocationPoint;
import lbs.ctl.lbs.BaiduMap.LocationService;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    private LocationService locationService=null;
    private LocationPoint point=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyApplication.getInstance().addActivity(this);
        context=this;

        initView();
        initMap();
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
}
