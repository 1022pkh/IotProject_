package com.kyounghyun.iotproject.main.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.kyounghyun.iotproject.R;

/**
 * Created by kyounghyun on 2017. 1. 15..
 */

public class MainViewHolder extends RecyclerView.ViewHolder {

    public TextView idText;
    public TextView nameText;
    public TextView statusText;
    public TextView removeBtn;



    public MainViewHolder(View itemView) {
        super(itemView);

        idText = (TextView)itemView.findViewById(R.id.idNum);
        nameText = (TextView)itemView.findViewById(R.id.moduleName);
        statusText = (TextView)itemView.findViewById(R.id.moduleStatus);
        removeBtn = (TextView)itemView.findViewById(R.id.removeBtn);
    }


}
