package lbs.ctl.lbs.luce;

import java.util.ArrayList;


/**
 * WCDMA数据格式解析类
 */
public class ParseWcdma {
    private ArrayList<LuceCellInfo> luceCellInfos;
    public ParseWcdma(String wcdma, double latitude, double longitude, String userMark, String time){
        luceCellInfos = new ArrayList<>();
        LuceCellInfo luceCellInfo = new LuceCellInfo();
        String split[] = wcdma.split(",");
        if(split.length==19){
            int plmn  = Integer.parseInt(split[0].replaceAll("\"", "").replaceAll(" ",""));
            int arfcn = Integer.parseInt(split[1]);
            int psc   = Integer.parseInt(split[2]);
//            ecio  = split[3];
//            rscp  = split[4];
            int  rssi  = Integer.parseInt(split[5]);
            luceCellInfo.setCellType("主小区");
            luceCellInfo.setBtsType(CellType.WCDMA.toString());
            luceCellInfo.setPlmn(plmn);
            luceCellInfo.setLatitude(latitude);
            luceCellInfo.setLongitude(longitude);
            luceCellInfo.setArfcn(arfcn);
            luceCellInfo.setPn_bsic_pci_psc(psc);
            luceCellInfo.setRssi(rssi);
            luceCellInfo.setTime(time);
            try{
                int lac = Integer.valueOf(split[6], 16);
                luceCellInfo.setLac_sid(lac);
            }catch(NumberFormatException e){

            }
            try{
                int cid = Integer.valueOf(split[14], 16);
                luceCellInfo.setCi_nid(cid);
            }catch(NumberFormatException e){
            }
            luceCellInfo.setUserRemark(userMark);
//            addLuceCellInfo(luceCellInfo);
            luceCellInfos.add(luceCellInfo);
        }
    }

    public ArrayList<LuceCellInfo> getLuceCellInfos() {
        return luceCellInfos;
    }
}
