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
public class CellInfoAdapter extends CommonAdapter<LuceCellInfo> {


    public CellInfoAdapter(Context context, List<LuceCellInfo> datas, int layoutId) {
        super(context, datas, layoutId);

    }

    private boolean hex = false;
    private boolean shortCi = false;

    public boolean isHex() {
        return hex;
    }

    public void setHex(boolean hex) {
        this.hex = hex;
    }

    public boolean isShortCi() {
        return shortCi;
    }

    public void setShortCi(boolean shortCi) {
        this.shortCi = shortCi;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.get(mContext, convertView, parent, R.layout.cell_list_item, position);

        String lac="",ci = "";
        LuceCellInfo cellInfo = mDatas.get(position);
        if(cellInfo.getBtsType().equals(CellType.GSM_M.toString())
                ||cellInfo.getBtsType().equals(CellType.GSM_U.toString())){
            if(hex){
                lac = Integer.toHexString(cellInfo.getLac_sid());
                ci  = Integer.toHexString(cellInfo.getCi_nid());
            }
            else {
                lac = Integer.toString(cellInfo.getLac_sid());
                ci  = Integer.toString(cellInfo.getCi_nid());
            }

        }else if(cellInfo.getBtsType().equals(CellType.LTE.toString())
                ||cellInfo.getBtsType().equals(CellType.WCDMA.toString())
                ||cellInfo.getBtsType().equals(CellType.TDSCDMA.toString())){
            if(hex){
                lac = Integer.toHexString(cellInfo.getLac_sid());
                if(shortCi)
                    ci  = Integer.toHexString(cellInfo.getCi_nid()&0x0000ffff);
                else
                    ci  = Integer.toHexString(cellInfo.getCi_nid());
            }
            else {
                lac = Integer.toString(cellInfo.getLac_sid());
                if(shortCi)
                    ci  = Integer.toString(cellInfo.getCi_nid()&0x0000ffff);
                else
                    ci  = Integer.toString(cellInfo.getCi_nid());
            }

        }


        ((TextView)holder.getView(R.id.cell_list_item_type)).setText(cellInfo.getCellType());
        ((TextView)holder.getView(R.id.cell_list_item_lac)).setText(lac);
        ((TextView)holder.getView(R.id.cell_list_item_ci)).setText(ci);
        ((TextView)holder.getView(R.id.cell_list_item_arfcn)).setText(String.valueOf(cellInfo.getArfcn()));
        ((TextView)holder.getView(R.id.cell_list_item_pbp)).setText(String.valueOf(cellInfo.getPn_bsic_pci_psc()));
        ((TextView)holder.getView(R.id.cell_list_item_rssi)).setText(String.valueOf(cellInfo.getRssi()));

        return holder.getConvertView();
    }

    @Override
    public void convert(ViewHolder holder, LuceCellInfo cellInfo) {

        holder.setText(R.id.cell_list_item_type, cellInfo.getCellType())
                .setText(R.id.cell_list_item_lac, String.valueOf(cellInfo.getLac_sid()))
                .setText(R.id.cell_list_item_ci, String.valueOf(cellInfo.getCi_nid()))
                .setText(R.id.cell_list_item_arfcn, String.valueOf(cellInfo.getArfcn()))
                .setText(R.id.cell_list_item_pbp, String.valueOf(cellInfo.getPn_bsic_pci_psc()))
                .setText(R.id.cell_list_item_rssi, String.valueOf(cellInfo.getRssi()));
    }
}
