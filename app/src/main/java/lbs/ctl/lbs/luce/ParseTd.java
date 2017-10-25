package lbs.ctl.lbs.luce;

import java.util.ArrayList;

/**
 * TDSCDMA 数据格式解析
 */
public class ParseTd {
    private ArrayList<LuceCellInfo> luceCellInfos;
    public ParseTd(String tdMsg, double latitude, double longitude, String userMark, String time){
        luceCellInfos = new ArrayList<>();
        String[] msgSplit = tdMsg.split(",");
        if(msgSplit.length!=8) return;
        int plmn = Integer.parseInt(msgSplit[0]+msgSplit[1]);
        int lac = Integer.parseInt(msgSplit[2]);
        int psc = Integer.parseInt(msgSplit[4]);
        int cellid = Integer.parseInt(msgSplit[3]);
        int arfcn = Integer.parseInt(msgSplit[5]);
        int rssi= Integer.parseInt(msgSplit[6]);
        LuceCellInfo luceCellInfo = new LuceCellInfo();
        luceCellInfo.setCellType("主小区");
        luceCellInfo.setBtsType(CellType.TDSCDMA.toString());
        luceCellInfo.setPlmn(46000);
        luceCellInfo.setLatitude(latitude);
        luceCellInfo.setLongitude(longitude);
        luceCellInfo.setUserRemark(userMark);
        luceCellInfo.setArfcn(arfcn);
        luceCellInfo.setPn_bsic_pci_psc(psc);
        luceCellInfo.setRssi(rssi);
        luceCellInfo.setLac_sid(lac);
        luceCellInfo.setCi_nid(cellid);
        luceCellInfo.setPlmn(plmn);
        luceCellInfo.setTime(time);
        luceCellInfos.add(luceCellInfo);
    }

    public ArrayList<LuceCellInfo> getLuceCellInfos() {
        return luceCellInfos;
    }
}
