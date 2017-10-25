package lbs.ctl.lbs.BaiduMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.baidu.mapapi.utils.route.BaiduMapRoutePlan;
import com.baidu.mapapi.utils.route.RouteParaOption;

import java.util.List;

import lbs.ctl.lbs.R;

/**
 * Created by CTL on 2017/10/24.
 */

/**
 * 百度地图功能类
 */
public class BaiduMapUtil {
    private BaiduMap mBaiduMap;
    private Context context;
    GeoCoder geoCoder = null;
    public BaiduMapUtil(Context context,BaiduMap map) {
        this.context=context;
        this.mBaiduMap=map;
        geoCoder = GeoCoder.newInstance();
    }

    /**
     * 往地图上添加marker
     * @param lat
     * @param lon
     * @param num 0红色  1 橘色  2 灰色
     */
    private void addMarker(double lat,double lon,int num){
        LatLng point = new LatLng(lat, lon);
        int icon=R.drawable.iconmarka;
        if (num==0){
            icon=R.drawable.iconmarka;
        }else if (num==1){

        }else if (num==2){

        }
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(icon);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(point)
                .zoom(18)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
    }

    /**
     * 往地图上添加一个覆盖物
     * @param latLngs
     */
    private void draw_find(List<LatLng> latLngs){
        OverlayOptions polygonOption = new PolygonOptions()
                .points(latLngs)
                .stroke(new Stroke(5, 0xAA00FF00))
                .fillColor(0xAAFFFF00);
        mBaiduMap.addOverlay(polygonOption);
    }

    /**
     * 添加多个覆盖物到地图
     * @param showList 添加地图的云层
     * 多个覆盖物颜色不同   可以设置每个OverlayOptions的颜色不同
     */
    private void add_more_overlay(final List<OverlayOptions> showList){
        OverlayManager manager = new OverlayManager(mBaiduMap) {
            @Override
            public List<OverlayOptions> getOverlayOptions() {
                return showList;
            }

            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }

            @Override
            public boolean onPolylineClick(Polyline polyline) {
                return true;
            }
        };
        manager.addToMap();//添加到地图
        manager.zoomToSpan();//自动缩放到合适比例
    }
    /**
     *启动百度地图导航
     * @param star_address 起点地址
     * @param star_latLng  起点经纬度
     * @param end_latlng   终点经纬度
     */
    private void launchNavigator(String star_address,LatLng star_latLng,LatLng end_latlng){
        //起点
        String star_add=star_address;
        //终点
        final String[] end_add = {""};
        GeoCoder geoCoder = GeoCoder.newInstance();
        // 设置反地理经纬度坐标,请求位置时,需要一个经纬度
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(end_latlng));
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                end_add[0] =geoCodeResult.getAddress();
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

            }
        });
        RouteParaOption routeParaOption=new RouteParaOption();
        routeParaOption.startPoint(star_latLng);
        routeParaOption.startName(star_add);
        routeParaOption.endPoint(end_latlng);
        routeParaOption.endName(end_add[0]);
        try{
            BaiduMapRoutePlan.openBaiduMapWalkingRoute(routeParaOption,context);
        }catch (Exception e){
            e.printStackTrace();
            showDialog();
        }
    }

    /**
     * 根据地址得到经纬度
     * @param city
     * @param address
     * @return
     */
    private LatLng AddressToLatLng(String city,String address){
        final LatLng[] latLng = {null};
        geoCoder.geocode(new GeoCodeOption().city(city).address(address));
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
//                latLng[0] =geoCodeResult.getLocation();
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                latLng[0] =reverseGeoCodeResult.getLocation();
            }
        });
        return latLng[0];
    }

    /**
     * 根据经纬度得到地址
     * @param latLng
     * @return
     */
    private String LatLngToAddress(LatLng latLng){
        final String[] address = {""};
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                address[0] =geoCodeResult.getAddress();
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

            }
        });
        return address[0];
    }
    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                OpenClientUtil.getLatestBaiduMapApp(context);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }
}
