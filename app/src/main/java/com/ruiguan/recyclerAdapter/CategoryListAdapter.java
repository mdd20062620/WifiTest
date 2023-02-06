package com.ruiguan.recyclerAdapter;

import android.content.Context;
import android.net.wifi.ScanResult;


import com.ruiguan.R;
import com.ruiguan.commonAdapter.ItemClickListener;
import com.ruiguan.commonAdapter.RecyclerCommonAdapter;
import com.ruiguan.commonAdapter.ViewHolder;

import java.util.List;

/**
 * Created by xngly on 2019/4/3.
 */

public class CategoryListAdapter extends RecyclerCommonAdapter<ScanResult> {
    public CategoryListAdapter(Context context, List <ScanResult> strings, int layoutId) {
        super(context, strings, layoutId);
    }

    @Override
    protected void convert(ViewHolder holder, ScanResult item, int position, ItemClickListener itemClickListener) {
       holder.setText(R.id.id_num,item.SSID);
        holder.setText(R.id.id_mac,item.BSSID);
    }

}
