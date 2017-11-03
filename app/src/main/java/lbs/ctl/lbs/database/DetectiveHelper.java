package lbs.ctl.lbs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yuxingyu on 16/5/23.
 */
public class DetectiveHelper extends SQLiteOpenHelper {

    private static final String DB_NAME ="LUCE.db";

    private static final int VERSION = 1;

    private  static DetectiveHelper detectiveHelper = null;

    private  DetectiveHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }


    public static DetectiveHelper getInstance(Context context){
        if(detectiveHelper==null){
            detectiveHelper = new DetectiveHelper(context);
        }
        return detectiveHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table userMark(mark text,upload Integer)");
        db.execSQL("create table backupData(" +
                "LAC_SID Integer," +
                "CI_NID Integer," +
                "BID Integer," +
                "PN_PSC_PCI_BSIC Integer," +
                "latitude Double," +
                "longitude Double," +
                "arfcn Integer," +
                "rssi Integer," +
                "mode Integer, " +
                "plmn Integer," +
                "time text," +
                "address text," +
                "btsType text," +
                "userRemark text,"+"baiduLatitude Double,"+"baiduLongitude Double"+")");
        db.execSQL("create table userData(" +"CELL_TYPE text,"+
                "LAC_SID Integer," +
                "CI_NID Integer," +
                "BID Integer," +
                "PN_PSC_PCI_BSIC Integer," +
                "latitude Double," +
                "longitude Double," +
                "arfcn Integer," +
                "rssi Integer," +
                "mode Integer, " +
                "plmn Integer," +
                "time text," +
                "address text," +
                "btsType text," +
                "userRemark text,"+"baiduLatitude Double,"+"baiduLongitude Double"+")");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS backup");
        onCreate(db);
    }
}
