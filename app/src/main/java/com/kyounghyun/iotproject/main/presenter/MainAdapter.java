package com.kyounghyun.iotproject.main.presenter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kyounghyun.iotproject.R;
import com.kyounghyun.iotproject.database.ItemData;
import com.kyounghyun.iotproject.main.model.MainViewHolder;
import com.kyounghyun.iotproject.main.view.MainView;

import java.util.ArrayList;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;

/**
 * Created by kyounghyun on 2017. 1. 15..
 */

public class MainAdapter extends RecyclerView.Adapter<MainViewHolder> {

    ArrayList<ItemData> itemDatas;
    MainView view;

    public MainAdapter(ArrayList<ItemData> itemDatas , MainView view) {
        this.itemDatas = itemDatas;
        this.view = view;
    }

    public void renewDatas(ArrayList<ItemData> itemDatas) {
        this.itemDatas = itemDatas;
        notifyDataSetChanged();
    }


    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_list_item, parent,false);
        MainViewHolder viewHolder = new MainViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {

        holder.idText.setText(String.valueOf(position+1));
        holder.nameText.setText(itemDatas.get(position).identName);
        holder.numText.setText(itemDatas.get(position).identNum);
        holder.statusText.setText(itemDatas.get(position).status);


        holder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.deleteModule(position);
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

    public void setOnlineCheck(BluetoothDevice device){

        Log.i("myTag", ">>> BOND_BONDED : " + BOND_BONDED );
        Log.i("myTag", ">>> BOND_BONDING : " + BOND_BONDING );
        Log.i("myTag", ">>> BOND_NONEa : " + BOND_NONE );
        Log.i("myTag", ">>> check : " + device.getBondState() );

        int index = 0;
        for(int i=0; i<itemDatas.size(); i++){
            if(itemDatas.get(i).identNum.equals(device.getAddress()))
            {
                index = i;
            }
        }

        if(device.getBondState() == BOND_BONDED){
                itemDatas.get(index).status = "online";
                notifyDataSetChanged();

        }
        else if(device.getBondState() == BOND_BONDING)
        {
            itemDatas.get(index).status = "online";
            notifyDataSetChanged();
        }
        else
        {
            itemDatas.get(index).status = "offline";
            notifyDataSetChanged();

        }

    }


    public void setOnline(String address){
        int index = 0;
        for(int i=0; i<itemDatas.size(); i++){
            if(itemDatas.get(i).identNum.equals(address))
            {
                index = i;
            }
        }

        itemDatas.get(index).status = "online";
        notifyDataSetChanged();

    }

    public void setOffline() {

        for (int i = 0; i < itemDatas.size(); i++) {
            if(itemDatas.get(i).status.equals("checking")){
                itemDatas.get(i).status = "offline";
                notifyDataSetChanged();
            }

        }
    }
}
