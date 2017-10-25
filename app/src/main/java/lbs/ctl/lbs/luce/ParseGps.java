package lbs.ctl.lbs.luce;

import java.math.BigDecimal;

/**
 * GPS数据格式解析类
 */
public class ParseGps {
    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean gpsIsActive = false;

    private String gpsTimeYmd = "";
    private String gpsTimeHms = "";
    public ParseGps(String gpsMsg) {
        if (gpsMsg.length() < 10) return;

        String splitGpsStr[] = gpsMsg.split("[$]");
        if (splitGpsStr.length >= 3) {
            String gpggaStr[] = splitGpsStr[1].split(",");
            String gprmcStr[] = splitGpsStr[2].split(",");
            if (gpggaStr.length >= 10) {
                if (gpggaStr[1].length() >= 6) {
                    int hour = Integer.parseInt(gpggaStr[1].substring(0, 2)) + 8;
                    gpsTimeHms = String.valueOf(hour)
                            + gpggaStr[1].substring(2, 4)
                            + gpggaStr[1].substring(4, 6);
                }
                if (gpggaStr[2].length() >= 8 && gpggaStr[4].length() >= 9) {
                    double latDu = Double.parseDouble(gpggaStr[2].substring(0, 2));
                    double latFen = Double.parseDouble(gpggaStr[2].substring(2)) / 60;
                    double lngDu = Double.parseDouble(gpggaStr[4].substring(0, 3));
                    double lngFen = Double.parseDouble(gpggaStr[4].substring(3)) / 60;
                    BigDecimal lat = new BigDecimal(latDu + latFen);
                    latitude = lat.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                    BigDecimal lng = new BigDecimal(lngDu + lngFen);
                    longitude = lng.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
            }
            if (gprmcStr.length >= 10) {
                if (gprmcStr[2].contains("A")) {
                    gpsIsActive = true;
                } else {
                    gpsIsActive = false;
                }
                if (gprmcStr[9].length() == 6) {
                    gpsTimeYmd = gprmcStr[9].substring(4)
                            + gprmcStr[9].substring(2, 4)
                            + gprmcStr[9].substring(0, 2);
                }
            }
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public boolean isGpsIsActive() {
        return gpsIsActive;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getGpsTime(){
        return "20"+gpsTimeYmd+gpsTimeHms;
    }
}
