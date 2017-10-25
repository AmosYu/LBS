package lbs.ctl.lbs.luce;

/**
 * Created by yu on 2016/11/9.
 */

public enum CellType {
    GPS,
    GSM_M,
    GSM_U,
    WCDMA,
    CDMA,
    TDSCDMA,
    LTE,
    WIFI;

    private final String[] typeStrs = {"GPS","移动GSM","联通GSM","联通W","电信C","移动TD","移动LTE","WIFI"};
    public String toString(){
        return typeStrs[this.ordinal()];
    }
}
