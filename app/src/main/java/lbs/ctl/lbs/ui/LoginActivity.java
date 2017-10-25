package lbs.ctl.lbs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lbs.ctl.lbs.R;
import lbs.ctl.lbs.adapter.ExpandAdapter;
import lbs.ctl.lbs.database.DbAcessImpl;
import lbs.ctl.lbs.utils.Utils;

public class LoginActivity extends Activity {
    private Context context;

    private ListView filenameListView;
    private ArrayList<String> filenameList;
    private ExpandAdapter filenameAdapter;

    private Button newTaskTopBtn;
    private Button newTaskCenterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        context=this;
        MyApplication.getInstance().addActivity(this);
        initView();
        initData();
        bindAdapter();
    }


    private void initData() {
        DbAcessImpl db=DbAcessImpl.getDbInstance(context);
        filenameList.addAll(db.loadMark());
    }









    private void bindAdapter() {
        filenameAdapter=new ExpandAdapter(context,filenameList);
        filenameListView.setAdapter(filenameAdapter);

        filenameListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final String mark = filenameList.get(position);
                new AlertDialog.Builder(context)
                        .setTitle("删除文件？")
                        .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
                                dbAcess.deleteUserdata(mark);
                                filenameList.remove(position);
                                filenameAdapter.notifyDataSetChanged();
                            }
                        }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();


                return false;
            }
        });

//        filenameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//               String filename = filenameList.get(position);
//            }
//        });
    }

    private void initView() {
        filenameList=new ArrayList<>();
        filenameListView=(ListView) findViewById(R.id.task_listview);
        newTaskCenterBtn = (Button) findViewById(R.id.new_task_center_btn);
        newTaskCenterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        newTaskTopBtn = (Button)findViewById(R.id.new_task_top_btn);
        newTaskTopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTask(context);
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
                if(filenameList.contains(taskName)){
                    Utils.alertDialog(context,"温馨提示","此任务已存在","确定");
                    return;
                }
                DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
                dbAcess.insertMark(taskName);
                filenameList.add(taskName);
                filenameAdapter.notifyDataSetChanged();
                Intent intent = new Intent();
                intent.setClass(context, MainActivity.class);
                startActivity(intent);
            }
        }).show();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
