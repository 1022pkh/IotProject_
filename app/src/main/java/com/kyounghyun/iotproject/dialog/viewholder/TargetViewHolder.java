package com.kyounghyun.iotproject.dialog.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kyounghyun.iotproject.R;

/**
 * Created by kyounghyun on 2017. 1. 15..
 */

public class TargetViewHolder extends RecyclerView.ViewHolder {

    public TextView idText;
    public TextView nameText;
    public CheckBox checkBox;

    public TargetViewHolder(View itemView) {
        super(itemView);

        idText = (TextView)itemView.findViewById(R.id.device_address);
        nameText = (TextView)itemView.findViewById(R.id.device_name);
        checkBox = (CheckBox)itemView.findViewById(R.id.targetSetCheck);
    }


}
