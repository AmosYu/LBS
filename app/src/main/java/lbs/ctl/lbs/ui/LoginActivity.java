package lbs.ctl.lbs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import lbs.ctl.lbs.R;
import lbs.ctl.lbs.database.DbAcessImpl;
import lbs.ctl.lbs.luce.AllCellInfo;
import lbs.ctl.lbs.utils.ConfigDialog;
import lbs.ctl.lbs.utils.Utils;

public class LoginActivity extends Activity {
    private Context context;

    private ListView filenameListView;
    private ArrayList<HashMap<String,String>> filenameList;


    private SimpleAdapter simpleAdapter;


    private Button newTaskTopBtn;
    private Button newTaskCenterBtn;


    private Button uploadBtn, exportBtn,offlineMapBtn,importBtn,findBtsBtn;
    private Button config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        context=this;
        MyApplication.getInstance().addActivity(this);

        initView();
        initData();
        bindAdapter();
        initBtn();
//        new MyDialog(context).show();
//        ArrayList<Map<String,Object>> list = new ArrayList<>();
//        new LbsProDialog(context,"1232123",list).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initData() {
        filenameList = new ArrayList<>();
        DbAcessImpl db=DbAcessImpl.getDbInstance(context);
        ArrayList<String> list = new ArrayList<>();
        list.addAll(db.loadMark());

        for(String name:list)
        {
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("NAME",name);
            filenameList.add(map);
        }
    }



    private void initBtn(){
        findBtsBtn = (Button)findViewById(R.id.login_find_bts_btn);
        findBtsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context,MapFindActivity.class);
                startActivity(intent);
            }
        });

        uploadBtn = (Button) findViewById(R.id.login_upload_btn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("类型", "上传");
                intent.setClass(context, FileImportActivity.class);
                startActivity(intent);
            }
        });

        offlineMapBtn = (Button) findViewById(R.id.login_offlinemap_btn);
        offlineMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(context, OffLineActivity.class);
                startActivity(intent);
            }
        });
        exportBtn = (Button) findViewById(R.id.login_export_btn);
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("类型", "导出");
                intent.setClass(context, FileImportActivity.class);
                startActivity(intent);
            }
        });

        //导入按钮
        importBtn = (Button) findViewById(R.id.login_import_btn);
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("类型", "导入");
                intent.setClass(context, FileImportActivity.class);
                startActivity(intent);
            }
        });
    }





    private void bindAdapter() {


        simpleAdapter = new SimpleAdapter(context,filenameList,R.layout.file_name_item,
                new String[]{"NAME"},new int[]{R.id.tv_name_item});
        filenameListView.setAdapter(simpleAdapter);




        filenameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AllCellInfo.userMark = filenameList.get(position).get("NAME").toString();
                Intent intent = new Intent();
                intent.setClass(context,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initView() {
        filenameList=new ArrayList<>();
        filenameListView=(ListView) findViewById(R.id.task_listview);
//        newTaskCenterBtn = (Button) findViewById(R.id.new_task_center_btn);
//        newTaskCenterBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
        newTaskTopBtn = (Button)findViewById(R.id.new_task_top_btn);
        newTaskTopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTask(context);
            }
        });
        config=(Button)findViewById(R.id.main_config_btn);
        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfigDialog mdialog=new ConfigDialog();
                mdialog.showConfig(context);
            }
        });
    }

    private void newTask(final Context context){
        final EditText ed = new EditText(context);
    
        SimpleDateFormat formatter   =   new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate   =   new Date(System.currentTimeMillis());//获取当前时间
        final String timeStr   =   formatter.format(curDate);
        ed.setHint("输入任务名称,不输入默认当前时间");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("新建任务");
        builder.setView(ed);
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String taskName = ed.getText().toString().replaceAll(" ","");
                if(taskName==null||taskName.length()==0){
                    taskName = timeStr;
                }

                if(checkName(taskName)){
                    Utils.alertDialog(context,"温馨提示","此任务已存在","确定");
                    return;
                }
                DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
                dbAcess.insertMark(taskName);
                HashMap<String,String> map = new HashMap<String, String>();
                map.put("NAME",taskName);
                filenameList.add(map);
                simpleAdapter.notifyDataSetChanged();
//                Intent intent = new Intent();
//                intent.setClass(context, MainActivity.class);
//                startActivity(intent);
            }
        }).show();

    }


    private boolean checkName(String name){
        for(HashMap<String,String> map:filenameList){
            if(map.get("NAME").toString().equals(name))
                return true;
        }

        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
