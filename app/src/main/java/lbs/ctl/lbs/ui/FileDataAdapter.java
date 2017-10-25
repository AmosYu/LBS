package lbs.ctl.lbs.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lbs.ctl.lbs.R;


/**
 * Created by Amy on 2016/10/12.
 */
public class FileDataAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String,Object>> mList=new ArrayList<>();
    public FileDataAdapter(Context context, List<Map<String,Object>> list){
        this.context=context;
        this.mList=list;
        Log.e("adapter",mList.toString());
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.simple_file_data, null);
            viewHolder.filedat_lac = (TextView) convertView.findViewById(R.id.filedat_lac);
            viewHolder.filedat_ci = (TextView) convertView.findViewById(R.id.filedat_ci);
            viewHolder.filedat_bid = (TextView) convertView.findViewById(R.id.filedat_bid);
            viewHolder.filedat_zhishi = (TextView) convertView.findViewById(R.id.filedat_zhishi);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.filedat_lac.setText(mList.get(position).get("lac_sid").toString());
        viewHolder.filedat_ci.setText(mList.get(position).get("ci_nid").toString());
        viewHolder.filedat_bid.setText(mList.get(position).get("bid").toString());
        viewHolder.filedat_zhishi.setText(mList.get(position).get("type").toString());

        return convertView;
    }
    private class ViewHolder{
        private TextView filedat_lac;
        private TextView filedat_ci;
        private TextView filedat_bid;
        private TextView filedat_zhishi;
    }
}
