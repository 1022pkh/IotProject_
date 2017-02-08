package com.kyounghyun.iotproject.dialog.presenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.kyounghyun.iotproject.R;
import com.kyounghyun.iotproject.application.ApplicationController;
import com.kyounghyun.iotproject.database.ItemData;
import com.kyounghyun.iotproject.dialog.viewholder.TargetViewHolder;

import java.util.ArrayList;

/**
 * Created by kyounghyun on 2017. 1. 15..
 */

public class TargetAdapter extends RecyclerView.Adapter<TargetViewHolder> {

    ArrayList<ItemData> itemDatas;

    public TargetAdapter(ArrayList<ItemData> itemDatasw) {
        this.itemDatas = itemDatas;
    }

    public void renewDatas(ArrayList<ItemData> itemDatas) {
        this.itemDatas = itemDatas;
        notifyDataSetChanged();
    }


    @Override
    public TargetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.target_list_item, parent,false);
        TargetViewHolder viewHolder = new TargetViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TargetViewHolder holder, final int position) {

        holder.nameText.setText(itemDatas.get(position).identName);
        holder.idText.setText(itemDatas.get(position).identNum);

        if(itemDatas.get(position).targetCheck == 0)
            holder.checkBox.setChecked(false);
        else
            holder.checkBox.setChecked(true);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    itemDatas.get(position).targetCheck = 1;

                    ApplicationController.getInstance().mDbOpenHelper.DbUpdate(itemDatas.get(position));
                }
                else{
                    itemDatas.get(position).targetCheck = 0;

                    ApplicationController.getInstance().mDbOpenHelper.DbUpdate(itemDatas.get(position));
                }

                notifyDataSetChanged();
            }
        });


    }

    @Override
    public int getItemCount() {
        if (itemDatas == null) {
            return 0;
        }

        // Add extra view to show the footer view
        return itemDatas.size();
    }

}
