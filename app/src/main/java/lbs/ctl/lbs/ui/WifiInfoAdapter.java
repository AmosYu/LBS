package lbs.ctl.lbs.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import lbs.ctl.lbs.R;
import lbs.ctl.lbs.luce.WifiInfo;


public class WifiInfoAdapter extends CommonAdapter<WifiInfo> {


    public WifiInfoAdapter(Context context, List<WifiInfo> datas, int layoutId) {
        super(context, datas, layoutId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.get(mContext, convertView, parent, R.layout.wifi_item, position);
        WifiInfo wifiInfo = mDatas.get(position);
        ((TextView)holder.getView(R.id.wifi_id)).setText(Integer.toString(wifiInfo.getId()));
        ((TextView)holder.getView(R.id.wifi_ssid)).setText(wifiInfo.getName());

        ((TextView)holder.getView(R.id.wifi_bssid)).setText(wifiInfo.getMac());
        ((TextView)holder.getView(R.id.wifi_rssi)).setText(wifiInfo.getRssi());


        return holder.getConvertView();
    }

    @Override
    public void convert(ViewHolder holder, WifiInfo wifiInfo) {

        holder.setText(R.id.wifi_id, Integer.toString(wifiInfo.getId()))
                .setText(R.id.wifi_ssid, wifiInfo.getName())
                .setText(R.id.wifi_bssid,wifiInfo.getMac())
                .setText(R.id.wifi_rssi, wifiInfo.getRssi());
    }
}
