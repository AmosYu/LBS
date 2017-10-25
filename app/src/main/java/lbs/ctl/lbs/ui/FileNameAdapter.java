package lbs.ctl.lbs.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import lbs.ctl.lbs.R;


/**
 * Created by Amy on 2016/10/12.
 */
public class FileNameAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> mList=new ArrayList<>();
    public FileNameAdapter(Context context, ArrayList<String> list){
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
            convertView = LayoutInflater.from(context).inflate(R.layout.filename_item, null);
            viewHolder.filename_id = (TextView) convertView.findViewById(R.id.filename_id_Tv);
            viewHolder.filename = (TextView) convertView.findViewById(R.id.filename_Tv);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Log.e("position",position+"");
        viewHolder.filename_id.setText((position+1)+"");
        viewHolder.filename.setText(mList.get(position));
        return convertView;
    }
    private class ViewHolder{
        private TextView filename_id;
        private TextView filename;
    }
}
