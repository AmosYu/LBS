package lbs.ctl.lbs;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.pm.ActivityInfo;
import android.os.Vibrator;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MyLocationData;

import java.util.LinkedList;
import java.util.List;

import lbs.ctl.lbs.BaiduMap.LocationService;

/**
 * Created by CTL on 2017/10/24.
 */

public class MyApplication extends Application {
    private List<Activity> activityList = new LinkedList<Activity>();
    private static MyApplication instance;
    public LocationService locationService;
    public MyLocationData locData=null;//位置信息
    public Vibrator mVibrator;
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }
    public static MyApplication getInstance()
    {
        return instance;
    }

    public MyLocationData getLocData() {
        return locData;
    }

    public void addActivity(Activity activity)
    {
        activityList.add(activity);
    }
    public void exit()
    {
        for(Activity activity:activityList)
        {
            activity.finish();
        }
//        DbAcessImpl.getDbInstance(getApplicationContext()).closeDb();
        System.exit(0);
    }
    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        System.gc();
    }
}
