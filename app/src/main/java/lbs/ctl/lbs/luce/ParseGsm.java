package lbs.ctl.lbs.luce;

import java.util.ArrayList;


/**
 * GSM数据格式解析
 */
public class ParseGsm {
    private ArrayList<LuceCellInfo> luceCellInfos;
    public ParseGsm(String gsmMsg, CellType cellType, double latitude, double longitude, String userMark, String time) {
        luceCellInfos = new ArrayList<>();
        if (gsmMsg.length() < 6) return;
        int plmn = Integer.parseInt(gsmMsg.substring(0, 5));
        gsmMsg = gsmMsg.substring(5);
        for (int i = 0, j = 0; i < 7; i++, j = j + 25) {
            LuceCellInfo luceCellInfo = new LuceCellInfo();
            luceCellInfo.setTime(time);
            try {
                int bsic = Integer.valueOf(gsmMsg.substring(1 + j, 3 + j), 8);
                luceCellInfo.setPn_bsic_pci_psc(bsic);
            } catch (NumberFormatException e) {

            }
            catch (ArrayIndexOutOfBoundsException ex){

            }
            try {
                int lac = Integer.valueOf((gsmMsg.substring(3 + j, 7 + j)).replaceAll(" ", ""), 16);
                luceCellInfo.setLac_sid(lac);
            } catch (NumberFormatException e) {

            }
            catch (ArrayIndexOutOfBoundsException ex){

            }
            try {
                int ci = Integer.valueOf((gsmMsg.substring(7 + j, 11 + j)).replaceAll(" ", ""), 16);
                luceCellInfo.setCi_nid(ci);
            } catch (NumberFormatException e) {

            }
            catch (ArrayIndexOutOfBoundsException ex){

            }
            try {
                int arfcn = Integer.parseInt(gsmMsg.substring(11 + j, 15 + j).replaceAll(" ",""));
                luceCellInfo.setArfcn(arfcn);
            } catch (NumberFormatException ex) {

            }
            catch (ArrayIndexOutOfBoundsException ex){

            }
            try {
                int rssi = Integer.parseInt(gsmMsg.substring(15 + j, 19 + j).replaceAll(" ",""));
                luceCellInfo.setRssi(rssi);
            } catch (NumberFormatException ex) {
            }
            catch (ArrayIndexOutOfBoundsException ex){

            }
            luceCellInfo.setBtsType(cellType.toString());
            luceCellInfo.setPlmn(plmn);
            if (i == 0) {
                luceCellInfo.setCellType("主小区");
            } else {
                luceCellInfo.setCellType("邻区");
            }
            luceCellInfo.setUserRemark(userMark);
            luceCellInfo.setLatitude(latitude);
            luceCellInfo.setLongitude(longitude);
            if (i == 0) {
                luceCellInfo.setCellType("主小区");
//                luceCellInfos.add(luceCellInfo);
            } else {
                luceCellInfo.setCellType("邻区");
            }

            luceCellInfos.add(luceCellInfo);
//            luceCellInfos.add(luceCellInfo);
        }
    }

    public ArrayList<LuceCellInfo> getLuceCellInfos() {
        return luceCellInfos;
    }
}
