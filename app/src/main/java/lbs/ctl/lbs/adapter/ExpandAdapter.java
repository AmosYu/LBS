package lbs.ctl.lbs.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.ArrayList;


import lbs.ctl.lbs.R;

/**
 * Created by yu on 17/10/24.
 */

public class ExpandAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> list;
    private int currentItem = -1; //用于记录点击的 Item 的 position，是控制 item 展开的核心

    public ExpandAdapter(Context context, ArrayList<String> list) {
        super();
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.layout_expand, parent, false);
            holder = new ViewHolder();
            holder.showArea = (RelativeLayout) convertView.findViewById(R.id.layout_showArea);
            holder.tvTasKname = (TextView) convertView.findViewById(R.id.tv_task_name);
            holder.viewDelete = (LinearLayout) convertView.findViewById(R.id.view_delete);
            holder.viewExport = (LinearLayout) convertView.findViewById(R.id.view_export);
            holder.viewUpload = (LinearLayout) convertView.findViewById(R.id.view_upload);
            holder.viewDetail = (LinearLayout) convertView.findViewById(R.id.view_detail);

            holder.hideArea = (LinearLayout) convertView.findViewById(R.id.layout_hideArea);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String taskName = list.get(position);

        // 注意：我们在此给响应点击事件的区域（我的例子里是 showArea 的线性布局）添加Tag，为了记录点击的 position，我们正好用 position 设置 Tag
        holder.showArea.setTag(position);

        holder.tvTasKname.setText(taskName);


        //根据 currentItem 记录的点击位置来设置"对应Item"的可见性（在list依次加载列表数据时，每加载一个时都看一下是不是需改变可见性的那一条）
        if (currentItem == position) {
            holder.hideArea.setVisibility(View.VISIBLE);
        } else {
            holder.hideArea.setVisibility(View.GONE);
        }

        holder.showArea.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //用 currentItem 记录点击位置
                int tag = (Integer) view.getTag();
                if (tag == currentItem) { //再次点击
                    currentItem = -1; //给 currentItem 一个无效值
                } else {
                    currentItem = tag;
                }
                //通知adapter数据改变需要重新加载
                notifyDataSetChanged(); //必须有的一步
            }
        });

        holder.viewDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        holder.viewUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        holder.viewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        holder.viewExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        return convertView;
    }

    private static class ViewHolder {
        private RelativeLayout showArea;

        private TextView tvTasKname;

        private LinearLayout viewDetail;

        private LinearLayout viewUpload;

        private LinearLayout viewDelete;

        private LinearLayout viewExport;

        private LinearLayout hideArea;
    }
}
