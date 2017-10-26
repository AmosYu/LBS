package lbs.ctl.lbs.luce;

import android.content.Context;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;

import java.io.IOException;
import java.math.BigDecimal;

import lbs.ctl.lbs.ui.MainActivity;
import lbs.ctl.lbs.bluetooth.BluetoothConn;
import lbs.ctl.lbs.bluetooth.BluetoothState;
import lbs.ctl.lbs.database.DbAcessImpl;
import lbs.ctl.lbs.utils.Gps2BaiDu;


public class RevThread implements Runnable {

    private BluetoothConn blueConn;

    private AllCellInfo allCellInfo;

    private volatile boolean terminated = true;

    private Context context;

    DbAcessImpl dbAcess;

    public RevThread(AllCellInfo allCellInfo, BluetoothConn blueConn, Context context) {
        this.blueConn = blueConn;
        this.allCellInfo = allCellInfo;
        dbAcess = DbAcessImpl.getDbInstance(context);
        this.context = context;
    }

    public void run() {
        terminated = false;
        while(!terminated){
            String revMsg;
            try {
                revMsg = blueConn.revMsg();
                if (revMsg == null) continue;
                Log.i("接收", revMsg);
                if(revMsg.length() < 12) continue;
                parseData(revMsg);
            } catch (IOException e) {
                if(!terminated) {
                    terminated = true;
                    blueConn.setState(BluetoothState.ABORTED);
                }
                blueConn.closeConn();
            }
        }
        blueConn.closeConn();
    }

    private double latitude,longitude;

    /**
     * 处理接收数据，数据先后顺序中，GPS位置排第一，其它数据紧随其后
     * @param revMsg
     */
    private void parseData(String revMsg){
        if (revMsg.length()<12) return;
        int cmd = -1;
        try {
            cmd = Integer.parseInt(revMsg.substring(0, 4));
        }catch (NumberFormatException nfe){
            return;
        }
        CellType cellType = null;
        String dataMsg = revMsg.substring(12);
        switch (cmd){
            case 2://移动G
                ParseGsm parseGm = new ParseGsm(dataMsg,CellType.GSM_M,latitude,longitude,allCellInfo.getUserMark(),AllCellInfo.getCurrentTime());
                allCellInfo.setGsm_mList(parseGm.getLuceCellInfos());
                dbAcess.saveInDbA(parseGm.getLuceCellInfos(),false);
                break;
            case 3://联通G
                ParseGsm parseGu = new ParseGsm(dataMsg,CellType.GSM_U,latitude,longitude,allCellInfo.getUserMark(),AllCellInfo.getCurrentTime());
                allCellInfo.setGsm_uList(parseGu.getLuceCellInfos());
                dbAcess.saveInDbA(parseGu.getLuceCellInfos(),false);
                break;
            case 4://WIFI
                ParseWifi parseWifi = new ParseWifi(dataMsg);
                allCellInfo.setWifiList(parseWifi.getWifiInfos());
                break;
            case 5://WCDMA
                ParseWcdma parseWcdma = new ParseWcdma(dataMsg,latitude,longitude,allCellInfo.getUserMark(),AllCellInfo.getCurrentTime());
                if(parseWcdma.getLuceCellInfos().size()>0)
                    allCellInfo.setWcdmaInfo(parseWcdma.getLuceCellInfos().get(0));
                dbAcess.saveInDbA(parseWcdma.getLuceCellInfos(),false);
                break;
            case 6://CDMA
                ParseCdma parseCdma = new ParseCdma(dataMsg,latitude,longitude,allCellInfo.getUserMark(),AllCellInfo.getCurrentTime());
                if(parseCdma.getLuceCellInfos().size()>0)
                    allCellInfo.setCdmaInfo(parseCdma.getLuceCellInfos().get(0));
                dbAcess.saveInDbA(parseCdma.getLuceCellInfos(),true);
                break;
            case 7://TDSCDMA
                ParseTd parseTd = new ParseTd(dataMsg,latitude,longitude,allCellInfo.getUserMark(),AllCellInfo.getCurrentTime());
                if(parseTd.getLuceCellInfos().size()>0)
                    allCellInfo.setTdInfo(parseTd.getLuceCellInfos().get(0));
                dbAcess.saveInDbA(parseTd.getLuceCellInfos(),false);
                break;
            case 8://GPS
                ParseGps parseGps = new ParseGps(dataMsg);
                if(parseGps.isGpsIsActive()){
                    latitude = parseGps.getLatitude();
                    longitude = parseGps.getLongitude();
                }else {
                    if(MainActivity.baiduLongitude!=0&& MainActivity.baiduLatitude!=0){
                        LatLng point = Gps2BaiDu.baiduToGps(MainActivity.baiduLatitude,MainActivity.baiduLongitude);
                        BigDecimal lat =  new BigDecimal(point.latitude);
                        BigDecimal longi =  new BigDecimal(point.longitude);
                        latitude = lat.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                        longitude = longi.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                    else {
                        latitude = 0;
                        longitude= 0;
                    }
                }
                allCellInfo.setGps(latitude,longitude,parseGps.isGpsIsActive());
                break;
            case 9://TDLTE
                ParseTddLte parseTdLte = new ParseTddLte(dataMsg,latitude,longitude,allCellInfo.getUserMark(),AllCellInfo.getCurrentTime());
                allCellInfo.setTddLteList(parseTdLte.getLuceCellInfos());
                dbAcess.saveInDbA(parseTdLte.getLuceCellInfos(),false);
                break;
            default:
        }

    }

    public void terminated(){
        terminated = true;
    }
    public boolean isTerminated() {
        return terminated;
    }

}
