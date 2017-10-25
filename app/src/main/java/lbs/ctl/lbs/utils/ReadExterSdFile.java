package lbs.ctl.lbs.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lbs.ctl.lbs.database.DbAcessImpl;
import lbs.ctl.lbs.luce.CellType;
import lbs.ctl.lbs.luce.ParseCdma;
import lbs.ctl.lbs.luce.ParseGps;
import lbs.ctl.lbs.luce.ParseGsm;
import lbs.ctl.lbs.luce.ParseTd;
import lbs.ctl.lbs.luce.ParseTddLte;
import lbs.ctl.lbs.luce.ParseWcdma;


/**
 * Created by yu on 2015/12/17.
 */
public class ReadExterSdFile {

    private ArrayList<File> dirList = null;

    private Context context;

    public ReadExterSdFile(Context context) {
        this.context = context;
        String path =  Environment.getExternalStorageDirectory().getPath()+ File.separator;
        List list = getSdDirList(path);
        if (list != null) {
            dirList = new ArrayList<>();
            dirList.addAll(list);
        }
    }






    private List<File> getSdDirList (String extSdPath) {
            List<File> dataList = new ArrayList<>();
            File file = new File(extSdPath);
            File[] fileArray = file.listFiles();
            if (fileArray == null) return dataList;
            for (int i = 0; i < fileArray.length; i++) {
                if (!fileArray[i].isDirectory()) {
                    continue;
                }
                if (!isNumeric(fileArray[i].getName())) {
                    continue;
                }
                dataList.add(fileArray[i]);
            }
            return dataList;
        }

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public ArrayList<File> getDirList() {
        return dirList;
    }



//    public void addDirToSelectList(String name){
//        for(File dir:dirList){
//            if(!dir.getName().compareTo(name))
//        }
//        if(!selectDirList.contains(file)){
//            selectDirList.add(file);
//        }
//    }

    private boolean terminate = true;

    public void startImport(final String userMark, final ArrayList<String> dirNameList, final boolean isDelete, final Handler handler) {
        terminate = false;
        new Thread() {
            @Override
            public void run() {
                ArrayList<File> selectDirList = new ArrayList<File>();
                for(String dirName:dirNameList){
                    for(File file:dirList){
                        if(file.getName().equals(dirName)){
                            selectDirList.add(file);
                        }
                    }
                }
                DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
                int j = 1;
                for (int i = 0; i < selectDirList.size(); i++) {
                    File[] files = selectDirList.get(i).listFiles();

                    for (File readFile : files) {

                        try {
                            ParseGps dataGps = null;
                            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(readFile)));
                            for (String line = br.readLine(); line != null; line = br.readLine()) {
                                try {
                                    String head = line.substring(0, 7);
                                    String msg = line.substring(7);
                                    if (head.contains("U_GSM")) {
                                        ParseGsm unicomGsm = new ParseGsm(msg, CellType.GSM_U, dataGps.getLatitude(), dataGps.getLongitude(), userMark,dataGps.getGpsTime());
                                        if (dataGps != null && dataGps.isGpsIsActive()) {
                                            dbAcess.saveInDb(unicomGsm.getLuceCellInfos(), false);
                                        }
                                    } else if (head.contains("M_GSM")) {
                                        ParseGsm mobileGsm = new ParseGsm(msg, CellType.GSM_M, dataGps.getLatitude(), dataGps.getLongitude(), userMark,dataGps.getGpsTime());
                                        if (dataGps != null && dataGps.isGpsIsActive()) {
                                            dbAcess.saveInDb(mobileGsm.getLuceCellInfos(), false);
                                        }
                                    } else if (head.contains("<CDMA >")) {
                                        ParseCdma dataCdma = new ParseCdma(msg, dataGps.getLatitude(), dataGps.getLongitude(), userMark,dataGps.getGpsTime());
                                        if (dataGps != null && dataGps.isGpsIsActive())
                                            dbAcess.saveInDb(dataCdma.getLuceCellInfos(), true);

                                    } else if (head.contains("GPS")) {
                                        dataGps = new ParseGps(msg);

                                    } else if (head.contains("WCDMA")) {
                                        ParseWcdma dataWcdma = new ParseWcdma(msg, dataGps.getLatitude(), dataGps.getLongitude(), userMark,dataGps.getGpsTime());
                                        if (dataGps != null && dataGps.isGpsIsActive())
                                            dbAcess.saveInDb(dataWcdma.getLuceCellInfos(), false);
                                    } else if (head.contains("TCDMA")) {
                                        ParseTd dataTd = new ParseTd(msg, dataGps.getLatitude(), dataGps.getLongitude(), userMark,dataGps.getGpsTime());
                                        if (dataGps != null && dataGps.isGpsIsActive())
                                            dbAcess.saveInDb(dataTd.getLuceCellInfos(), false);
                                    } else if (head.contains("WIFI")) {
//                            dataWlan = new DataWlan(msg);

                                    } else if (head.contains("TDLTE")) {
                                        ParseTddLte dataLte = new ParseTddLte(msg, dataGps.getLatitude(), dataGps.getLongitude(), userMark,dataGps.getGpsTime());
                                        if (dataGps != null && dataGps.isGpsIsActive())
                                            dbAcess.saveInDb(dataLte.getLuceCellInfos(), false);
                                    }
                                } catch (Exception ex) {

                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Message msg = new Message();
                        msg.arg2 = 2000;
                        msg.obj = "已导入"+(i+1)+"个目录,共"+j+"个文件";
                        handler.sendMessage(msg);
                        if(isDelete) readFile.delete();
                        j++;
                    }
                    if(isDelete){
                        File file = new File(selectDirList.get(i).getPath());
                        file.delete();
                    }
                }
                Message msg = new Message();
                msg.arg2 = 2000;
                msg.arg1 = 2001;
                msg.obj = "导入完成";
                handler.sendMessage(msg);
            }
        }.start();
    }

    private void deleteAllFiles(File root){
        File files[]=root.listFiles();
        if(files!=null){
            for (File f:files){
                if(f.isDirectory()){// 判断是否为文件夹  
                    deleteAllFiles(f);
                    try{
                    f.delete();
                    }catch(Exception ex){
                    }
                }else{
                    if(f.exists()){// 判断是否存在  
                        deleteAllFiles(f);
                        try{
                            f.delete();
                        }catch(Exception e){
                        }
                    }
                }
            }
        }
    }
}
