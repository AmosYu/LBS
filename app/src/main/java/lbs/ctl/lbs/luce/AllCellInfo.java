package lbs.ctl.lbs.luce;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;

/**
 * Created by yu on 2016/11/10.
 */

public class AllCellInfo extends Observable {

    private Context context;
    private ArrayList<LuceCellInfo> gsm_mList;
    private ArrayList<LuceCellInfo> gsm_uList;
    private ArrayList<LuceCellInfo> tddLteList;
    private ArrayList<WifiInfo> wifiList;
    private LuceCellInfo wcdmaInfo;
    private LuceCellInfo cdmaInfo;
    private LuceCellInfo tdInfo;
    private double latitude = 0.0;
    private  double longitude = 0.0;
    private String userMark = "test";//文件名=mark=用户标识的地址
    private boolean gpsIsActive = false;//判断路测采集器是否已获取到GPS定位信号 true已定位 false 未定位

    private CellType cellType;

    public AllCellInfo(Context context) {
        this.context = context;
        gsm_mList = new ArrayList<>();
        gsm_uList = new ArrayList<>();
        tddLteList= new ArrayList<>();
        wifiList  =  new ArrayList<>();
    }

    public void setGps(double lat,double lon,boolean active){
        latitude = lat;
        longitude = lon;
        gpsIsActive = active;
        noticeObservers(CellType.GPS);
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    public CellType getCellType() {
        return cellType;
    }

    public ArrayList<LuceCellInfo> getGsm_mList() {
        return gsm_mList;
    }

    public void setGsm_mList(ArrayList<LuceCellInfo> gsm_mList) {
        this.gsm_mList.clear();
        this.gsm_mList.addAll(gsm_mList);
        noticeObservers(CellType.GSM_M);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getUserMark() {
        return userMark;
    }

    public void setUserMark(String userMark) {
        this.userMark = userMark;
    }

    private boolean dataSaveToDb = false;

    public boolean isDataSaveToDb() {
        return dataSaveToDb;
    }

    public void setDataSaveToDb(boolean dataSaveToDb) {
        this.dataSaveToDb = dataSaveToDb;
    }

    public boolean isGpsIsActive() {
        return gpsIsActive;
    }

    public ArrayList<LuceCellInfo> getGsm_uList() {
        return gsm_uList;
    }

    public void setGsm_uList(ArrayList<LuceCellInfo> gsm_uList) {
        this.gsm_uList.clear();
        this.gsm_uList.addAll(gsm_uList);
        noticeObservers(CellType.GSM_U);
    }

    public ArrayList<LuceCellInfo> getTddLteList() {
        return tddLteList;
    }

    public void setTddLteList(ArrayList<LuceCellInfo> tddLteList) {
        this.tddLteList.clear();
        this.tddLteList.addAll(tddLteList);
        noticeObservers(CellType.LTE);
    }

    public ArrayList<WifiInfo> getWifiList() {
        return wifiList;
    }

    public void setWifiList(ArrayList<WifiInfo> wifiList) {
        this.wifiList.clear();
        this.wifiList.addAll(wifiList);
        noticeObservers(CellType.WIFI);
    }

    public LuceCellInfo getWcdmaInfo() {
        return wcdmaInfo;
    }

    public void setWcdmaInfo(LuceCellInfo wcdmaInfo) {
        this.wcdmaInfo = wcdmaInfo;
        noticeObservers(CellType.WCDMA);
    }

    public LuceCellInfo getCdmaInfo() {
        return cdmaInfo;
    }

    public void setCdmaInfo(LuceCellInfo cdmaInfo) {
        this.cdmaInfo = cdmaInfo;
        noticeObservers(CellType.CDMA);
    }

    public LuceCellInfo getTdInfo() {
        return tdInfo;
    }

    public static String getCurrentTime(){
        SimpleDateFormat formatter   =   new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate   =   new Date(System.currentTimeMillis());//获取当前时间
        String timeStr   =   formatter.format(curDate);
        return timeStr;
    }

    public void setTdInfo(LuceCellInfo tdInfo) {
        this.tdInfo = tdInfo;
        noticeObservers(CellType.TDSCDMA);
    }

    public void noticeObservers(CellType cellType){
        setChanged();
        notifyObservers(cellType);
    }

    public ArrayList<LuceCellInfo> getCurrentCellInListView(CellType cellType){
        ArrayList<LuceCellInfo> list =new ArrayList<>();
        switch (cellType){
            case GSM_M:
                list.addAll(gsm_mList);
                break;
            case GSM_U:
                list.addAll(gsm_uList);
                break;
            case LTE:
                list.addAll(tddLteList);
                break;
        }
        return list;
    }
}
