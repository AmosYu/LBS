package lbs.ctl.lbs;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lbs.ctl.lbs.database.DbAcessImpl;


/**
 * Created by yu on 15-11-25.
 */
public class DataUpload extends Thread {


    private Context context;

    public static volatile boolean terminate = true;
    private ArrayList<String> markList;
    private Handler handler;

    /**
     * @param context
     * @param markList 文件名称列表
     * @param handler  向外传递进度消息
     */
    public DataUpload(Context context, ArrayList<String> markList, Handler handler) {
        this.context = context;
        this.markList = markList;
        this.handler = handler;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    @Override
    public void run() {
        terminate = false;
        WebServiceConn conn = new WebServiceConn(context);
        int timesFailed = 0;
        while (!terminate) {
            delay(2);
            if (conn.getToken()) {//服务器上传之前先取验证token
                timesFailed = 0;
                break;
            }else {

                timesFailed++;      //验证token失败三次停止
                Message msg = new Message();
                msg.arg2 = 3000;
                msg.obj = "服务器连接失败";
                if(timesFailed==3){
                    msg.arg1 = 3001;
                    msg.obj = "服务器连接失败，请您检查网络或核实ip地址和端口号";
                }
                handler.sendMessage(msg);
                if(timesFailed==3) return;
            }
        }
        int number = 0;
        //取目录名称
        DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
        while (!terminate && markList.size() > 0) {

            String mark = markList.get(0);
            markList.remove(0);
            List<Map<String, Object>> mapList = dbAcess.selectByFile(mark);
            String fileMsg = "";
            for (Map<String, Object> map : mapList) {
                fileMsg = fileMsg + map.get("lac_sid").toString() + ","
                        + map.get("ci_nid").toString() + ","
                        + map.get("bid").toString() + ","
                        + map.get("arfcn").toString() + ","
                        + map.get("rssi").toString() + ","
                        + map.get("latitude").toString() + ","
                        + map.get("longitude").toString() + ","
                        + map.get("time").toString() + ","
                        + map.get("zhishi").toString() + ","+","+";";
            }
            int times = 2;
            number++;
            while (times > 0 && !terminate) {//开始提交文件
                times--;
                JSONObject result = conn.submitMsg(conn.createJson(mark, number, fileMsg));
                int code = conn.getResult(result);
                if (code == 0) {//成功 删除
                    dbAcess.upDateMark(mark, 1);
                    Message msg = new Message();
                    msg.arg2 = 3000;
                    msg.obj = " 文件" + mark + "已上传，文件内包含" + mapList.size() + "条基站数据";
                    handler.sendMessage(msg);
                    times = -1;
                } else if (code == 1) { //如果token失效，再次获取token
                    conn.getToken();
                    times = 1;
                    Message msg = new Message();
                    msg.arg2 = 3000;
                    msg.obj = " 文件" + mark + "上传超时";
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.arg2 = 3000;
                    msg.obj = " 文件" + mark + "上传失败";
                    handler.sendMessage(msg);
                }
            }
        }

        Message msg = new Message();
        msg.arg2 = 3000;
        msg.arg1 = 3001;
        msg.obj = "文件上传结束";
        handler.sendMessage(msg);
    }


    private void delay(int sec) {
        try {
            Thread.sleep(1000 * sec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

