package com.kyounghyun.iotproject.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kyounghyun.iotproject.R;
import com.kyounghyun.iotproject.application.ApplicationController;
import com.kyounghyun.iotproject.database.DbOpenHelper;
import com.kyounghyun.iotproject.database.ItemData;
import com.kyounghyun.iotproject.dialog.presenter.TargetAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by KyoungHyun on 16. 5. 1..
 */
public class DialogTargetList extends Dialog {

    @BindView(R.id.targetList)
    RecyclerView targetList;


    LinearLayoutManager mLayoutManager;
    ArrayList<ItemData> itemDatas;
    TargetAdapter mAdapter;

    DbOpenHelper mDbOpenHelper;


    public DialogTargetList(Context context) {
        super(context);
    }

    public DialogTargetList(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_target);

        ButterKnife.bind(this);

        mDbOpenHelper = ApplicationController.getInstance().mDbOpenHelper;

        itemDatas = new ArrayList<ItemData>();
        mAdapter = new TargetAdapter(itemDatas);
        targetList.setAdapter(mAdapter);

        //각 item의 크기가 일정할 경우 고정
        targetList.setHasFixedSize(true);

        // layoutManager 설정
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        targetList.setLayoutManager(mLayoutManager);

        targetList.setOverScrollMode(View.OVER_SCROLL_NEVER);



        itemDatas = mDbOpenHelper.DbMainSelect();
        mAdapter.renewDatas(itemDatas);
    }


}

