package lbs.ctl.lbs.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import lbs.ctl.lbs.R;
import lbs.ctl.lbs.luce.CellType;
import lbs.ctl.lbs.luce.LuceCellInfo;


/**
 * Created by on 16/5/15.
 */
public class LacDataAdapter extends CommonAdapter<LuceCellInfo> {

    private List<LuceCellInfo> datas;
    public LacDataAdapter(Context context, List<LuceCellInfo> datas, int layoutId) {
        super(context, datas, layoutId);
        this.datas=datas;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.get(mContext, convertView, parent, R.layout.cell_listview_item, position);
        LuceCellInfo cellInfo = datas.get(position);
        ((TextView)holder.getView(R.id.dilog_cell_lac)).setText("LAC_SID:"+String.valueOf(cellInfo.getLac_sid()));
        ((TextView)holder.getView(R.id.dilog_cell_ci)).setText("CI_NID:"+String.valueOf(cellInfo.getCi_nid()));
        ((TextView)holder.getView(R.id.dilog_cell_bid)).setText("BID:"+String.valueOf(cellInfo.getBid()));

        return holder.getConvertView();
    }

    @Override
    public void convert(ViewHolder holder, LuceCellInfo cellInfo) {

        holder.setText(R.id.dilog_cell_lac, String.valueOf(cellInfo.getLac_sid()))
                .setText(R.id.dilog_cell_ci, String.valueOf(cellInfo.getCi_nid()))
                .setText(R.id.dilog_cell_bid, String.valueOf(cellInfo.getBid()));
    }
}
