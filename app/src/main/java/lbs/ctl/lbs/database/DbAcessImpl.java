package lbs.ctl.lbs.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lbs.ctl.lbs.luce.AllCellInfo;
import lbs.ctl.lbs.luce.CellType;
import lbs.ctl.lbs.luce.LuceCellInfo;
import lbs.ctl.lbs.utils.Gps2BaiDu;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;


public class DbAcessImpl implements DbAccess {

    private static final String TAG = "DbAcessImpl";
    private DetectiveHelper dHelper;

    private static DbAcessImpl dbInstance;

    private SQLiteDatabase dbWrite;
    private SQLiteDatabase dbRead;
    private  DbAcessImpl(Context context) {
        dHelper = DetectiveHelper.getInstance(context);
        dbRead = dHelper.getReadableDatabase();
        dbWrite = dHelper.getWritableDatabase();
    }
    public static DbAcessImpl getDbInstance(Context context){
        if(dbInstance == null){
            dbInstance = new DbAcessImpl(context);
        }
        return  dbInstance;
    }

    @Override
    public synchronized void  insertCellMapInfo(LuceCellInfo luceCellInfo, int type) {
        String tabName = null;
        if(type == USER_COLLECTION) {
            tabName = USER_DATA;
        }
        else if(type == AUTO_COLLECTION){
            tabName = BACKUP_DATA;
        }
//        SQLiteDatabase db = dHelper.getWritableDatabase();
        dbWrite.insert(tabName, null, getContentValues(luceCellInfo));
//        db.close();
    }

    @Override
    public boolean cellInDbBackup(LuceCellInfo luceCellInfo, boolean isCdma) {
//        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean result = false;
        if(isCdma){
            cursor = dbRead.query(BACKUP_DATA,new String[]{LAC_SID,CI_NID,BID},
                        LAC_SID+"=? and "+CI_NID+"=? and "+BID+"=?",
                        new String[]{String.valueOf(luceCellInfo.getLac_sid()),
                                String.valueOf(luceCellInfo.getCi_nid()),
                                String.valueOf(luceCellInfo.getBid())},
                        null,null,null);
        }
        else{
            cursor = dbRead.query(BACKUP_DATA,new String[]{LAC_SID,CI_NID},
                        LAC_SID+"=? and "+CI_NID+"=? ",
                        new String[]{String.valueOf(luceCellInfo.getLac_sid()),
                                String.valueOf(luceCellInfo.getCi_nid())},
                        null,null,null);
        }
        if(cursor!=null){
            if(cursor.getCount()>0) {
                result = true;
            }
        }
        cursor.close();
//        db.close();
        return result;
    }
    @Override
    public boolean cellInDbUserdata(LuceCellInfo luceCellInfo,boolean isCdma) {
        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean result = false;
        if(isCdma){
            cursor = dbRead.query(USER_DATA,new String[]{LAC_SID,CI_NID,BID,USER_REMARK},
                    LAC_SID+"=? and "+CI_NID+"=? and "+BID+"=? and "+USER_REMARK+"=?",
                    new String[]{String.valueOf(luceCellInfo.getLac_sid()),
                            String.valueOf(luceCellInfo.getCi_nid()),
                            String.valueOf(luceCellInfo.getBid()),
                            luceCellInfo.getUserRemark()},
                    null,null,null);
        }
        else{
            cursor = dbRead.query(USER_DATA,new String[]{LAC_SID,CI_NID},
                    LAC_SID+"=? and "+CI_NID+"=? and "+USER_REMARK+"=?",
                    new String[]{String.valueOf(luceCellInfo.getLac_sid()),
                            String.valueOf(luceCellInfo.getCi_nid()),luceCellInfo.getUserRemark()},
                    null,null,null);
        }
        if(cursor!=null){
            if(cursor.getCount()>0) {
                result = true;
            }
        }
        cursor.close();
//        db.close();
        return result;
    }

    public boolean cellInDbUserdataS(LuceCellInfo luceCellInfo,boolean isCdma) {
        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean result = false;
        if(isCdma){
            cursor = dbRead.query(USER_DATA,new String[]{LAC_SID,CI_NID,BID,USER_REMARK,LATITUDE,LONGITUDE},
                    LAC_SID+"=? and "+CI_NID+"=? and "+BID+"=? and "+USER_REMARK+"=? and "+LATITUDE+"=? and "+LONGITUDE+"=? ",
                    new String[]{String.valueOf(luceCellInfo.getLac_sid()),
                            String.valueOf(luceCellInfo.getCi_nid()),
                            String.valueOf(luceCellInfo.getBid()),
                            luceCellInfo.getUserRemark(),
                            String.valueOf(luceCellInfo.getLatitude()),
                            String.valueOf(luceCellInfo.getLongitude())},
                    null,null,null);
        }
        else{
            cursor = dbRead.query(USER_DATA,new String[]{LAC_SID,CI_NID},
                    LAC_SID+"=? and "+CI_NID+"=? and "+USER_REMARK+"=?and "+LATITUDE+"=? and "+LONGITUDE+"=? ",
                    new String[]{String.valueOf(luceCellInfo.getLac_sid()),
                            String.valueOf(luceCellInfo.getCi_nid()),
                            luceCellInfo.getUserRemark(),
                            String.valueOf(luceCellInfo.getLatitude()),
                            String.valueOf(luceCellInfo.getLongitude())},
                    null,null,null);
        }
        if(cursor!=null){
            if(cursor.getCount()>0) {
                result = true;
            }
        }
        cursor.close();
//        db.close();
        return result;
    }




    @Override
    public void deleteCellMapInfo(LuceCellInfo luceCellInfo, boolean isCdma,int type) {

    }

    @Override
    public synchronized void updateBackup(LuceCellInfo luceCellInfo, boolean isCdma) {
//        SQLiteDatabase db = dHelper.getWritableDatabase();
        if(isCdma) {
            dbWrite.update(BACKUP_DATA, getContentValues(luceCellInfo),
                    LAC_SID + "=? and " + CI_NID + "=? and " + BID + "=? and "+RSSI + "<?",
                    new String[]{String.valueOf(luceCellInfo.getLac_sid()),
                            String.valueOf(luceCellInfo.getCi_nid()),
                            String.valueOf(luceCellInfo.getBid()),
                            String.valueOf(luceCellInfo.getRssi())});
        }
        else{
            dbWrite.update(BACKUP_DATA, getContentValues(luceCellInfo),
                    LAC_SID + "=? and " + CI_NID + "=? and "+RSSI + "<?" ,
                    new String[]{String.valueOf(luceCellInfo.getLac_sid()),
                            String.valueOf(luceCellInfo.getCi_nid()),
                            String.valueOf(luceCellInfo.getRssi())});
        }
//        db.close();

    }

    @Override
    public synchronized void insertMark(String mark) {
//        SQLiteDatabase db = dHelper.getWritableDatabase();
        Cursor cursor = dbWrite.query(USER_MARK,new String[]{MARK}, MARK+"=?",
                new String[]{mark}, null,null,null);
        if(cursor.getCount()==0){
            ContentValues cv = new ContentValues();
            cv.put(MARK,mark);
            dbWrite.insert(USER_MARK,null,cv);
        }
        cursor.close();
//        dbWrite.close();
    }

    @Override
    public ArrayList<String> loadMark() {
        ArrayList<String> markList = new ArrayList<>();
//        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = dbRead.query(USER_MARK,null,null,null,null,null,null);
        while (cursor.moveToNext()){
            String mark = cursor.getString(cursor.getColumnIndex(MARK));
            markList.add(mark);
        }
//        db.close();
        return markList;
    }

    public void upDateMark(String mark, int upload){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MARK,mark);
        contentValues.put("upload",upload);
        dbWrite.update(USER_MARK,contentValues,MARK+"=?",new String[]{mark});
    }
    public ArrayList<String> loadUploadMark(){
        ArrayList<String> markList = new ArrayList<>();
        Cursor cursor = dbRead.query(USER_MARK,null,null,null,null,null,null);
        while (cursor.moveToNext()){
            String mark = cursor.getString(cursor.getColumnIndex(MARK));
            int upload = cursor.getInt(cursor.getColumnIndex("upload"));
            if(upload != 1)
                markList.add(mark);
        }
        return markList;
    }

    @Override
    public synchronized void updateUserdata(LuceCellInfo luceCellInfo, boolean isCdma) {
//        SQLiteDatabase db = dHelper.getWritableDatabase();
        if(isCdma) {
            dbWrite.update(USER_DATA, getContentValues(luceCellInfo),
                    LAC_SID + "=? and " + CI_NID + "=? and " + BID + "=? and "+RSSI + "<?  and "+USER_REMARK+"=?",
                    new String[]{String.valueOf(luceCellInfo.getLac_sid()),
                            String.valueOf(luceCellInfo.getCi_nid()),
                            String.valueOf(luceCellInfo.getBid()),
                            String.valueOf(luceCellInfo.getRssi()),
                            luceCellInfo.getUserRemark()});
        }
        else{
            dbWrite.update(USER_DATA, getContentValues(luceCellInfo),
                    LAC_SID + "=? and " + CI_NID + "=? and "+RSSI + "<? and "+USER_REMARK+"=?" ,
                    new String[]{String.valueOf(luceCellInfo.getLac_sid()),
                            String.valueOf(luceCellInfo.getCi_nid()),
                            String.valueOf(luceCellInfo.getRssi()),
                            luceCellInfo.getUserRemark()});
        }
//        db.close();

    }


    @Override
    public void deleteAllData() {
//        SQLiteDatabase db = dHelper.getWritableDatabase();
        dbWrite.delete(BACKUP_DATA,null,null);
//        dbW.close();
    }

    @Override
    public int startExportDb(ArrayList<String> markList, boolean isDelete) {
        return 0;
    }

    public synchronized void deleteUserdata(String mark){
//        SQLiteDatabase db = dHelper.getWritableDatabase();


            dbWrite.delete(USER_MARK, MARK + "=?", new String[]{mark});
            dbWrite.delete(USER_DATA,USER_REMARK+"=?",new String[]{mark});
            Log.i(TAG, "删除的MARK----"+mark);

//        db.close();
    }


    public int startExportDb(ArrayList<String> markList, boolean isdelete, Handler handler) {

        SdCardOperate sdCardOperate = new SdCardOperate(getDirPath());

        if(!sdCardOperate.sdCardIsReady()) return -1;

//        SQLiteDatabase db = dHelper.getReadableDatabase();
        int sum = 0;
        for(String mark:markList){
//            Log.i(TAG, "MARK ----" +mark);
            Cursor cursor = dbRead.query(USER_DATA,null, USER_REMARK+"=?",
                    new String[]{mark},null,null,null,null);
//            Cursor cursor = db.query(USER_DATA,null, null,
//                    null,null,null,null,null);
            String fileMsg = getFileHead();
            while (cursor.moveToNext()){
                String cellType = cursor.getString(cursor.getColumnIndex(CELL_TYPE));
                int lac = cursor.getInt(cursor.getColumnIndex(LAC_SID));
                int ci  = cursor.getInt(cursor.getColumnIndex(CI_NID));
                int bid = cursor.getInt(cursor.getColumnIndex(BID));
                int arfcn = cursor.getInt(cursor.getColumnIndex(ARFCN));
                int pppb = cursor.getInt(cursor.getColumnIndex(PN_PSC_PCI_BSIC));
                int rssi = cursor.getInt(cursor.getColumnIndex(RSSI));
                double latitute = cursor.getDouble(cursor.getColumnIndex(LATITUDE));
                double longitute = cursor.getDouble(cursor.getColumnIndex(LONGITUDE));
                String time = cursor.getString(cursor.getColumnIndex(TIME));
//                String address = cursor.getString(cursor.getColumnIndex(ADDRESS));
                String userAddress = cursor.getString(cursor.getColumnIndex(USER_REMARK));
                String btsType = cursor.getString(cursor.getColumnIndex(BTS_TYPE));

                fileMsg = fileMsg+cellType+","+lac+","+ci+","+bid+","+latitute+","+longitute+","+arfcn+","+pppb+","+
                        rssi+","+time+","+userAddress+","+btsType+"\r\n";
                sdCardOperate.writeMsgToFile(fileMsg,mark);
//                Log.i(TAG, "导出的数据：----" +fileMsg);
                sum++;
                if(handler!=null){
                    Message msg = new Message();
                    msg.arg2 = 1000;
                    msg.obj = "成功导出"+sum+"条基站数据";
                    handler.sendMessage(msg);
                }
            }
        }

//        if(isdelete) deleteUserdata(markList);
        Message msg = new Message();
        msg.arg2 = 1000;
        msg.arg1 = 1001;
        msg.obj = "导出完成,导出文件位于本机默认存储空间的基站数据文件夹内。默认存储空间位置见注意事项";
        handler.sendMessage(msg);
        return sum;
    }

    /**
     * 根据文件名查询所有数据
     * @param filename
     * @return
     */
    public List<Map<String,Object>> selectByFile(String filename){
        List<Map<String,Object>> list=new ArrayList<>();
//        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = dbRead.rawQuery("select LAC_SID,CI_NID,BID,CELL_TYPE,latitude,longitude,address,arfcn,rssi,time,btsType from userData where userRemark=?",
                new String[]{filename});
        Log.e("mytag",cursor.getCount()+"");
        while(cursor.moveToNext()){
            Map<String,Object> map=new HashMap<>();
            map.put("lac_sid",cursor.getInt(0));
            map.put("ci_nid",cursor.getInt(1));
            map.put("bid",cursor.getInt(2));
            map.put("type",cursor.getString(3));
            map.put("latitude",cursor.getDouble(4));
            map.put("longitude",cursor.getDouble(5));
            map.put("address",cursor.getString(6));
            map.put("arfcn",cursor.getInt(7));
            map.put("rssi",cursor.getInt(8));
            map.put("time",cursor.getString(9));
            map.put("zhishi",cursor.getString(10));
            list.add(map);
        }
//        db.close();
        return list;
    }

    /**
     * 根据文件名查询所有数据
     * @param filename
     * @return
     */
    public LinkedList<LatLng> selectByFileGetPoint(String filename){
        LinkedList<LatLng> list=new LinkedList<>();
        Cursor cursor = dbRead.rawQuery("select LAC_SID,CI_NID,BID,CELL_TYPE,latitude,longitude,address,arfcn,rssi,time,btsType from userData where userRemark=?",
                new String[]{filename});
        Log.e("mytag",cursor.getCount()+"");
        while(cursor.moveToNext()){
            LatLng latLng = Gps2BaiDu.gpsToBaidu(cursor.getDouble(4),cursor.getDouble(5));
            list.add(latLng);
        }
        return list;
    }


    /**
     * 根据文件名和类型进行查询
     * @param filename
     * @param type
     * @return
     */
    public List<Map<String,Object>> selectByNameAndType(String filename, String type){
        List<Map<String,Object>> list=new ArrayList<>();
//        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = dbRead.rawQuery("select LAC_SID,CI_NID,BID,CELL_TYPE,latitude,longitude,address,arfcn,rssi,time,btsType from userData where userRemark=? AND btsType=?",
                new String[]{filename,type});
        while (cursor.moveToNext()){
            Map<String,Object> map=new HashMap<>();
            map.put("lac_sid",cursor.getInt(0));
            map.put("ci_nid",cursor.getInt(1));
            map.put("bid",cursor.getInt(2));
            map.put("type",cursor.getString(3));
            map.put("latitude",cursor.getDouble(4));
            map.put("longitude",cursor.getDouble(5));
            map.put("address",cursor.getString(6));
            map.put("arfcn",cursor.getInt(7));
            map.put("rssi",cursor.getInt(8));
            map.put("time",cursor.getInt(9));
            map.put("zhishi",cursor.getString(10));
            list.add(map);
        }
//        db.close();
        return list;
    }
    //从手机文件中查询数据
    @Override
    public ArrayList<String> FindPos(String mnc, String lac, String cellid) {
        ArrayList<String> list = new ArrayList<>();
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "db" + File.separator ,"db.db");
        if (file.exists()){
            SQLiteDatabase rizhi = openOrCreateDatabase(file,null);
            Cursor cursor = rizhi.rawQuery("select lat,lon from cellinfo where mnc =? and lac =? and ci =? " ,
                    new String[]{mnc, lac, cellid});

            while (cursor.moveToNext()){
                double latitude = cursor.getDouble(0);
                double longitude = cursor.getDouble(1);
                list.add(latitude + "");
                list.add(longitude + "");
            }
        }
        return list;
    }
    /**
     * 查询本地数据库
     * @param lac_sid
     * @param ci_nid
     * @param type
     * @return
     */
    public List<LuceCellInfo> findBtsUseId(String lac_sid, String ci_nid, CellType type){
        List<LuceCellInfo> list=new ArrayList<>();
        String cellType = type.toString();
        Cursor cursor = dbRead.rawQuery("select LAC_SID,CI_NID,BID,CELL_TYPE,latitude,longitude,address,arfcn,rssi,time,btsType from userData where LAC_SID=? AND CI_NID=? AND CELL_TYPE=?",
                new String[]{lac_sid,ci_nid,cellType});
        while (cursor.moveToNext()){
            LuceCellInfo lci = new LuceCellInfo();
            lci.setLac_sid(cursor.getInt(0));
            lci.setCi_nid(cursor.getInt(1));
            lci.setBid(cursor.getInt(2));
            lci.setCellType(cursor.getString(3));
            lci.setLatitude(cursor.getDouble(4));
            lci.setLongitude(cursor.getDouble(5));
            lci.setAddress(cursor.getString(6));
            lci.setArfcn(cursor.getInt(7));
            lci.setRssi(cursor.getInt(8));
            lci.setTime(String.valueOf(cursor.getInt(9)));
            lci.setBtsType(cursor.getString(10));
            list.add(lci);
        }
        return list;
    }
    /**
     * 查询本地数据库
     * @param lac_sid
     * @param ci_nid
     * @param type
     * @return
     */
    public List<LuceCellInfo> findOnlySameLacBts(String lac_sid,String ci_nid,CellType type){
        List<LuceCellInfo> list=new ArrayList<>();
        String cellType = type.toString();
        Cursor cursor = dbRead.rawQuery("select LAC_SID,CI_NID,BID,CELL_TYPE,latitude,longitude,address,arfcn,rssi,time,btsType from userData where LAC_SID=? AND CI_NID!=? AND CELL_TYPE=?",
                new String[]{lac_sid,ci_nid,cellType});
        while (cursor.moveToNext()){
            LuceCellInfo lci = new LuceCellInfo();
            lci.setLac_sid(cursor.getInt(0));
            lci.setCi_nid(cursor.getInt(1));
            lci.setBid(cursor.getInt(2));
            lci.setCellType(cursor.getString(3));
            lci.setLatitude(cursor.getDouble(4));
            lci.setLongitude(cursor.getDouble(5));
            lci.setAddress(cursor.getString(6));
            lci.setArfcn(cursor.getInt(7));
            lci.setRssi(cursor.getInt(8));
            lci.setTime(String.valueOf(cursor.getInt(9)));
            lci.setBtsType(cursor.getString(10));
            list.add(lci);
        }
        return list;
    }


    /**
     * 查询本地数据库
     * @param lac_sid
     * @param ci_nid
     * @return
     */
    public List<LuceCellInfo> findBtsUseId(String lac_sid, String ci_nid, String bid){
        List<LuceCellInfo> list=new ArrayList<>();
        String cellType = CellType.CDMA.toString();
        Cursor cursor = dbRead.rawQuery("select LAC_SID,CI_NID,BID,CELL_TYPE,latitude,longitude,address,arfcn,rssi,time,btsType from userData where LAC_SID=? AND CI_NID=? BID=? ANDAND CELL_TYPE=?",
                new String[]{lac_sid,ci_nid,bid,cellType});
        while (cursor.moveToNext()){
            LuceCellInfo lci = new LuceCellInfo();
            lci.setLac_sid(cursor.getInt(0));
            lci.setCi_nid(cursor.getInt(1));
            lci.setBid(cursor.getInt(2));
            lci.setCellType(cursor.getString(3));
            lci.setLatitude(cursor.getDouble(4));
            lci.setLongitude(cursor.getDouble(5));
            lci.setAddress(cursor.getString(6));
            lci.setArfcn(cursor.getInt(7));
            lci.setRssi(cursor.getInt(8));
            lci.setTime(String.valueOf(cursor.getInt(9)));
            lci.setBtsType(cursor.getString(10));
            list.add(lci);
        }
        return list;
    }



    public void select() {

        Cursor cursor = dbRead.query(USER_DATA,null,null,null,null,null,null);
        while (cursor.moveToNext()){
            int lac = cursor.getInt(cursor.getColumnIndex(LAC_SID));
            int ci  = cursor.getInt(cursor.getColumnIndex(CI_NID));
            int bid = cursor.getInt(cursor.getColumnIndex(BID));
            int arfcn = cursor.getInt(cursor.getColumnIndex(ARFCN));
            int pppb = cursor.getInt(cursor.getColumnIndex(PN_PSC_PCI_BSIC));
            int plmn = cursor.getInt(cursor.getColumnIndex(PLMN));
            int mode = cursor.getInt(cursor.getColumnIndex(MODE));
            int rssi = cursor.getInt(cursor.getColumnIndex(RSSI));
            double latitute = cursor.getDouble(cursor.getColumnIndex(LATITUDE));
            double longitute = cursor.getDouble(cursor.getColumnIndex(LONGITUDE));
            String time = cursor.getString(cursor.getColumnIndex(TIME));
            String address = cursor.getString(cursor.getColumnIndex(ADDRESS));
            String userAddress = cursor.getString(cursor.getColumnIndex(USER_REMARK));
            String type = cursor.getString(cursor.getColumnIndex(BTS_TYPE));
            Log.i(TAG, "select: "+LAC_SID+":"+lac+","+CI_NID+":"+ci+","+BID+":"+bid+","
                    +ARFCN+":"+arfcn+","+PN_PSC_PCI_BSIC+":"+pppb+","+PLMN+":"+plmn+","+RSSI+":"+rssi+","
                    +MODE+":"+mode+","+LATITUDE+":"+latitute+","+LONGITUDE+":"+longitute+","
                    +TIME+":"+time+","+ADDRESS+":"+address+","+USER_REMARK+":"+userAddress+","+BTS_TYPE+":"+type+"一条结束");
        }
        ;

    }



    private ContentValues getContentValues(LuceCellInfo luceCellInfo){
        ContentValues insertData = new ContentValues();
        insertData.put(CELL_TYPE,luceCellInfo.getCellType());
        insertData.put(LAC_SID,luceCellInfo.getLac_sid());
        insertData.put(CI_NID,luceCellInfo.getCi_nid());
        insertData.put(BID,luceCellInfo.getBid());
        insertData.put(LATITUDE,luceCellInfo.getLatitude());
        insertData.put(LONGITUDE,luceCellInfo.getLongitude());
        insertData.put(PN_PSC_PCI_BSIC,luceCellInfo.getPn_bsic_pci_psc());
        insertData.put(MODE,luceCellInfo.getMode());
        insertData.put(PLMN,luceCellInfo.getPlmn());
        insertData.put(RSSI,luceCellInfo.getRssi());
        insertData.put(ARFCN,luceCellInfo.getArfcn());
        insertData.put(TIME,luceCellInfo.getTime());
        insertData.put(ADDRESS,luceCellInfo.getAddress());
        insertData.put(USER_REMARK,luceCellInfo.getUserRemark());
        insertData.put(BTS_TYPE,luceCellInfo.getBtsType());
        return insertData;
    }

    private String getFileHead(){
        return  "小区类型"+","+LAC_SID+","+CI_NID+","+BID+","+"纬度"+","+"经度"+","+"频点"+","+
                "扰码或识别码"+","+"场强"+","+"时间"+","+"备注"+","+"基站类型"+"\r\n";
    }

    public static String getDirPath(){
        return Environment.getExternalStorageDirectory().getPath()+ File.separator+"基站数据";
    }
    public void closeDb(){
        if(dbRead!=null)dbRead.close();
        if(dbWrite!=null)dbWrite.close();
    }

    public void saveInDb(ArrayList<LuceCellInfo> cellInfos, boolean isCdma ){
        for(LuceCellInfo luceCellInfo:cellInfos){
            if(luceCellInfo.getLac_sid()==0||luceCellInfo.getCi_nid()==0) return;
            if(!cellInDbUserdata(luceCellInfo,isCdma)){
                insertCellMapInfo(luceCellInfo, DbAccess.USER_COLLECTION);
            }
            else{
                updateUserdata(luceCellInfo,isCdma);
            }
//            select();
        }
    }
    public void saveInDbA(ArrayList<LuceCellInfo> cellInfos, boolean isCdma ){
        for(LuceCellInfo luceCellInfo:cellInfos){
//            if(luceCellInfo.getCellType().equals("主小区"))
            if(luceCellInfo.getLatitude()==0.0||luceCellInfo.getLongitude()==0.0)
                continue;
            if((luceCellInfo.getLac_sid()!=0&&luceCellInfo.getLac_sid()!=65535)&&(luceCellInfo.getCi_nid()!=0))
                insertCellMapInfo(luceCellInfo, DbAccess.USER_COLLECTION);

        }
    }
}
