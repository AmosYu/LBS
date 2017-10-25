package lbs.ctl.lbs.luce;

import java.util.ArrayList;


/**
 * CDMA数据格式解析
 */
public class ParseCdma {
    private ArrayList<LuceCellInfo> luceCellInfos;
    public ParseCdma(String cdma, double latitude, double longitude, String userMark, String time){
        luceCellInfos = new ArrayList<>();
        String split[] = cdma.split(",");
        if(split.length==20){
            int sid = Integer.parseInt(split[0]);
            int nid = Integer.parseInt(split[1]);
            int bid = Integer.parseInt(split[2]);
            int pid = Integer.parseInt(split[3]);
            int channel = Integer.parseInt(split[4]);
            int  pn = Integer.parseInt(split[5]);
            int rssi = Integer.parseInt(split[9]);
            LuceCellInfo luceCellInfo = new LuceCellInfo();
            luceCellInfo.setCellType("主小区");
            luceCellInfo.setBtsType(CellType.CDMA.toString());
            luceCellInfo.setLatitude(latitude);
            luceCellInfo.setLongitude(longitude);
            luceCellInfo.setLac_sid(sid);
            luceCellInfo.setCi_nid(nid);
            luceCellInfo.setBid(bid);
            luceCellInfo.setArfcn(channel);
            luceCellInfo.setPn_bsic_pci_psc(pn);
            luceCellInfo.setRssi(rssi);
            luceCellInfo.setPlmn(46003);
            luceCellInfo.setTime(time);
            luceCellInfo.setUserRemark(userMark);
            luceCellInfos.add(luceCellInfo);

        }
    }

    public ArrayList<LuceCellInfo> getLuceCellInfos() {
        return luceCellInfos;
    }
}
