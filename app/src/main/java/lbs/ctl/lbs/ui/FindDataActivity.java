package lbs.ctl.lbs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lbs.ctl.lbs.R;
import lbs.ctl.lbs.database.DbAcessImpl;
import lbs.ctl.lbs.luce.CellType;
import lbs.ctl.lbs.utils.Gps2BaiDu;


/**
 * 数据和轨迹查询，与CellMapInfo项目的数据查询相同，
 * 不同之处加入地图显示功能，将每个文件的数据全部加载至地图上，
 * 分辨率为20米意思是，半径20米的范围内只显示一个点。
 */
public class FindDataActivity extends Activity {
    private Context context;

    private ListView filename_ListView;
    private ArrayList<String> filename_list;
    private FileNameAdapter filename_adapter;
    private ListView filedata_ListView;
    private List<Map<String,Object>> filedata_list;
    private FileDataAdapter filedata_adapter;

    private Spinner finddata_spinner_mode;

    private static String filename="";

    private MapView baiduMapView;
    private BaiduMap baiduMap;

    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_find_data);

        context=this;
        MyApplication.getInstance().addActivity(this);
        initView();
        initData();
        bindAdapter();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);

        baiduMapView = (MapView) findViewById(R.id.find_baidu_mapview);
        baiduMap = baiduMapView.getMap();
        baiduMap.setMapStatus(msu);
        baiduMapView.setBackgroundResource(0);
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setMapStatus(msu);
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {//点的标题为经纬度
                //OnInfoWindowClickListener listener = null;
                LatLng pos = marker.getPosition();
                Log.i("123",""+pos.latitude+"@"+pos.longitude);
                //String title=marker.getTitle();
//                if(marker.getTitle()!=null)
//                {
//                    cur_msg=marker.getTitle();
//                }

                return true;
            }
        });
        initRadioGroup();
    }
    private void initRadioGroup(){
        radioGroup = (RadioGroup)findViewById(R.id.find_radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.find_data_rb){
                    baiduMapView.setVisibility(View.INVISIBLE);
                }
                else if(checkedId==R.id.find_map_rb){
                    baiduMapView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initData() {
        DbAcessImpl db=DbAcessImpl.getDbInstance(context);
        filename_list.addAll(db.loadMark());
    }
    private ArrayList<LatLng> latLngList = new ArrayList<>();

    /**
     * 判断词典能够添加到显示列表内，20米内不添加，20米外添加
     * @param latLng
     * @return
     */
    private boolean isEnableAdd(LatLng latLng){
        for(LatLng latLngInList:latLngList){
            if(DistanceUtil.getDistance(latLng,latLngInList)<20){
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param fileDataList
     */
    private void initLatLngList(List<Map<String,Object>> fileDataList ){
        latLngList.clear();
        for(Map<String,Object> map:fileDataList){
            LatLng latLng = Gps2BaiDu.gpsToBaidu((double)map.get("latitude"),(double)map.get("longitude"));
            if(latLngList.size()==0||isEnableAdd(latLng)){
                latLngList.add(latLng);
            }
        }
    }



    //将可显示的点添加至地图上
    private void addMarkOnMap(){
        baiduMap.clear();
        for(LatLng latLng:latLngList){
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.iconmarka);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(latLng)
                    .icon(bitmap);
            //定义地图状态
            MapStatus mMapStatus = new MapStatus.Builder()
                    .target(latLng)
                    .zoom(18)
                    .build();
            //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            //改变地图状态
            baiduMap.setMapStatus(mMapStatusUpdate);
            //在地图上添加Marker，并显示
            baiduMap.addOverlay(option);
        }

    }
    private View selecView;
    private void bindAdapter() {
        filename_adapter=new FileNameAdapter(context,filename_list);
        filename_ListView.setAdapter(filename_adapter);

        filename_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final String mark = filename_list.get(position);
                new AlertDialog.Builder(context)
                        .setTitle("删除文件？")
                        .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
                                dbAcess.deleteUserdata(mark);
                                filename_list.remove(position);
                                filename_adapter.notifyDataSetChanged();
                            }
                        }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();


                return false;
            }
        });

        filedata_adapter=new FileDataAdapter(context,filedata_list);
        filedata_ListView.setAdapter(filedata_adapter);
        filename_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selecView != null){
                    selecView.setBackgroundColor(Color.WHITE);
                }
                selecView = view;
                selecView.setBackgroundResource(R.color.blue3);
                filedata_list.clear();
                DbAcessImpl db=DbAcessImpl.getDbInstance(context);
                filename=filename_list.get(position);
                if(showBtsType.contains("全部")){
                    filedata_list.addAll(db.selectByFile(filename));
                }
                else {
                    filedata_list.addAll(db.selectByNameAndType(filename,showBtsType));
                }
//                filedata_list.addAll(db.selectByFile(filename));
                filedata_adapter.notifyDataSetChanged();
                initLatLngList(filedata_list);
                addMarkOnMap();
            }
        });
        filedata_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context).setTitle("详细信息");
                builder.setMessage("LAC:  "+filedata_list.get(position).get("lac_sid").toString()+"\n"
                        +"CI:    "+filedata_list.get(position).get("ci_nid").toString()+"\n"
                        +"BID:  "+filedata_list.get(position).get("bid").toString()+"\n"
                        +"频点："+filedata_list.get(position).get("arfcn")+"\n"
                        +"强度："+filedata_list.get(position).get("rssi")+"\n"
                        +"经度："+filedata_list.get(position).get("longitude").toString()+"\n"
                        +"纬度："+filedata_list.get(position).get("latitude").toString()+"\n"
                        +"类型："+filedata_list.get(position).get("type").toString()+"\n"
                        +"制式: "+filedata_list.get(position).get("zhishi").toString());
                builder.show();
            }
        });
    }
   private ArrayAdapter<String> typeSpinnerAdapter;
    private void initView() {
        filename_list=new ArrayList<>();
        filedata_list=new ArrayList<>();
        filename_ListView=(ListView) findViewById(R.id.filename_ListView);
        filedata_ListView=(ListView)findViewById(R.id.filedata_ListView);
        finddata_spinner_mode=(Spinner)findViewById(R.id.finddata_spinner_mode);
        Resources res = getResources ();
        String[] modes = res.getStringArray(R.array.mode_arrays);
        typeSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, modes);
        typeSpinnerAdapter.setDropDownViewResource(R.layout.drop_down_item);
        finddata_spinner_mode.setAdapter(typeSpinnerAdapter);
        finddata_spinner_mode.setOnItemSelectedListener(new SpinnerSelectedListener());
    }
    private String showBtsType = "全部";
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position){
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
            DbAcessImpl db=DbAcessImpl.getDbInstance(context);
            filedata_list.clear();
            if(showBtsType.contains("全部")){
                filedata_list.addAll(db.selectByFile(filename));
            }
            else {
                filedata_list.addAll(db.selectByNameAndType(filename,showBtsType));
            }
            filedata_adapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    @Override
    protected void onDestroy() {
        if(baiduMapView!=null)
            baiduMapView.onDestroy();
        super.onDestroy();
    }
}
