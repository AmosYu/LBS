package lbs.ctl.lbs.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lbs.ctl.lbs.luce.LuceCellInfo;


/**
 * Created by yuxingyu on 16/5/24.
 */
public interface  DbAccess {

    /**
     * 用户采集
     */
    public static final int USER_COLLECTION = 121212;
    /**
     * 自动采集
     */
    public static final int AUTO_COLLECTION = 131313;
    /**
     * 程序内部数据自动存储
     * @param cellMapInfo
     */
    void insertCellMapInfo(LuceCellInfo cellMapInfo, int type);


    void deleteAllData();

    /**
     * 导出用户选择的数据
     * @param markList 用户选择的数据; 用list中的每个mark
     * @param isDelete 导出后是否删除原有数据

     * @return
     */
    int startExportDb(ArrayList<String> markList, boolean isDelete);
    /**
     * 判断此条数据在数据库中的状态
     * @param cellMapInfo
     * @return
     * -1 不存在；
     * 0 存在并且原数据的rssi大于新数据，不需要更新；
     * 1 存在原数据的rssi小于新数据，需要更新
     */
    boolean cellInDbBackup(LuceCellInfo cellMapInfo, boolean isCdma);
    boolean cellInDbUserdata(LuceCellInfo cellMapInfo, boolean isCdma);
    void deleteCellMapInfo(LuceCellInfo cellMapInfo, boolean isCdma, int type);
    void updateUserdata(LuceCellInfo cellMapInfo, boolean isCdma);
    void updateBackup(LuceCellInfo cellMapInfo, boolean isCdma);

    /**
     * @param mark
     */
    void insertMark(String mark);

    ArrayList<String> loadMark();

    /**
     * 根据文件名查询数据(数据库中存储的是用户地址)
     * @param filename
     * @return
     */
    List<Map<String,Object>> selectByFile(String filename);
    public List<Map<String,Object>> selectByNameAndType(String filename, String type);
    //根据LAC值和CELL值 查询出对应的基站经纬度
    ArrayList<String> FindPos(String mnc,String lac, String cellid);

    /**
     * 当前只为测试使用
     */
//    void select();
    public final String CELL_TYPE = "CELL_TYPE";
    public final String LAC_SID = "LAC_SID";
    public final String CI_NID = "CI_NID";
    public final String BID = "BID";
    public final String LATITUDE = "latitude";
    public final String LONGITUDE = "longitude";
    public final String RSSI = "rssi";
    public final String ARFCN = "arfcn";
    public final String PN_PSC_PCI_BSIC = "PN_PSC_PCI_BSIC";
    public final String PLMN = "plmn";
    public final String MODE = "mode";
    public final String TIME = "time";
    public final String ADDRESS = "address";
    public final String USER_REMARK = "userRemark";
    public final String BTS_TYPE = "btsType";
    public final String BACKUP_DATA="backupData";
    public final String USER_DATA="userData";
    public final String USER_MARK="userMark";
    public final String MARK="mark";


}
