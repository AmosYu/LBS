package lbs.ctl.lbs.luce;

import java.util.ArrayList;


public class ParseWifi {
    private ArrayList<WifiInfo> wifiInfos;
    public ParseWifi(String wifiMsg){
        wifiInfos = new ArrayList<>();
        String[] wlanGroup = wifiMsg.split(";");
        for(int i=0;i<wlanGroup.length;i++){
            try {
                String[] temp = wlanGroup[i].split(",");
                if (temp.length < 6) continue;
                WifiInfo wifiInfo = new WifiInfo();
                wifiInfo.setMac(temp[0]);
                if(temp[3].contains("1")){
                    wifiInfo.setKeyType("加密型");
                }
                else{
                    wifiInfo.setKeyType("开放型");
                }
                wifiInfo.setName(temp[4].replaceAll("\"", ""));
                wifiInfo.setRssi("-" + temp[5]);
                wifiInfo.setId(wifiInfos.size()+1);
                wifiInfos.add(wifiInfo);
            }catch(Exception ex){}
        }
    }

    public ArrayList<WifiInfo> getWifiInfos() {
        return wifiInfos;
    }
}
