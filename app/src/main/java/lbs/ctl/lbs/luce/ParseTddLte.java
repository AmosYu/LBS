package lbs.ctl.lbs.luce;

import java.util.ArrayList;

/**
 * TDD-LTE数据格式解析
 */
public class ParseTddLte {
    private ArrayList<LuceCellInfo> luceCellInfos;
    public ParseTddLte(String tddMsg, double latitude, double longitude, String userMark, String time){
        luceCellInfos = new ArrayList<>();
        String[] msgSplit = tddMsg.split(",");
        if(msgSplit.length>=16)
        {
            LuceCellInfo luceCellInfoMain = new LuceCellInfo();
            int lteRssi = Integer.parseInt(msgSplit[0]);

            int lteArfcn = Integer.parseInt(msgSplit[6]);
            int ltePci = Integer.parseInt(msgSplit[7]);
            luceCellInfoMain.setTime(time);
            int plmn = Integer.parseInt(msgSplit[9]+msgSplit[10]);

            try
            {
                int ci = Integer.parseInt(msgSplit[11],16);
                luceCellInfoMain.setCi_nid(ci);
            }
            catch(NumberFormatException e12)
            {
            }
            try
            {
                int tac = Integer.parseInt(msgSplit[12],16);
                luceCellInfoMain.setLac_sid(tac);

            }
            catch(NumberFormatException e12)
            {
            }
            luceCellInfoMain.setCellType("主小区");
            luceCellInfoMain.setBtsType(CellType.LTE.toString());
            luceCellInfoMain.setPlmn(plmn);
            luceCellInfoMain.setLatitude(latitude);
            luceCellInfoMain.setLongitude(longitude);
            luceCellInfoMain.setUserRemark(userMark);
            luceCellInfoMain.setPn_bsic_pci_psc(ltePci);
            luceCellInfoMain.setArfcn(lteArfcn);
            luceCellInfoMain.setRssi(lteRssi);
            luceCellInfos.add(luceCellInfoMain);
//            addLuceCellInfo(luceCellInfoMain);

//            lteRand = msgSplit[13];

//            int number = (msgSplit.length-16)/5;

//            for(int i=0;i<number;i++)
//            {
//                try {
//                    LuceCellInfo luceCellInfo = new LuceCellInfo();
//                    HashMap<String, Object> map = new HashMap<String, Object>();
//                    int arfcn = Integer.parseInt(msgSplit[16+i*5].replaceAll("\\(", ""));
//                    int pci = Integer.parseInt(msgSplit[16+i*5+1]);
//                    int rssi = Integer.parseInt(msgSplit[16+i*5+4].replaceAll("\\)", ""));
//                    luceCellInfo.setBtsType(CellType.LTE.toString());
//                    luceCellInfo.setPlmn(plmn);
//                    luceCellInfo.setCellType("邻区");
//                    luceCellInfo.setLatitude(latitude);
//                    luceCellInfo.setLongitude(longitude);
//                    luceCellInfo.setUserRemark(userMark);
//                    luceCellInfo.setPn_bsic_pci_psc(pci);
//                    luceCellInfo.setArfcn(arfcn);
//                    luceCellInfo.setRssi(rssi);
//                    luceCellInfo.setTime(time);
//                    luceCellInfos.add(luceCellInfo);
////                    addLuceCellInfo(luceCellInfo);
//                } catch (ArrayIndexOutOfBoundsException ex){
//
//                }
//
//            }
        }
    }

    public ArrayList<LuceCellInfo> getLuceCellInfos() {
        return luceCellInfos;
    }
}
