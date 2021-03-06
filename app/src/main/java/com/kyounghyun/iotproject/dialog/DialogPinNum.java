package com.kyounghyun.iotproject.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kyounghyun.iotproject.R;
import com.kyounghyun.iotproject.application.ApplicationController;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by KyoungHyun on 16. 5. 1..
 */
public class DialogPinNum extends Dialog {

    @BindView(R.id.inputPinEdit)
    EditText inputPinEdit;
    @BindView(R.id.dialog_complete)
    Button dialog_complete;
    @BindView(R.id.dialog_cancel)
    Button dialog_cancel;

    private View.OnClickListener completeBtnEvent;

    public DialogPinNum(Context context, int themeResId, View.OnClickListener completeBtnEvent, View.OnClickListener cancelBtnEvent) {
        super(context, themeResId);
        this.completeBtnEvent = completeBtnEvent;
        this.cancelBtnEvent = cancelBtnEvent;
    }

    public DialogPinNum(Context context, View.OnClickListener completeBtnEvent, View.OnClickListener cancelBtnEvent) {
        super(context);
        this.completeBtnEvent = completeBtnEvent;
        this.cancelBtnEvent = cancelBtnEvent;
    }

    private View.OnClickListener cancelBtnEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pinset);

        ButterKnife.bind(this);
        dialog_complete.setOnClickListener(completeBtnEvent);
        dialog_cancel.setOnClickListener(cancelBtnEvent);



        String temp = ApplicationController.connectInfo.getString("pinNum", "");


        if(temp == null || temp.equals("")){
            ;
        }
        else{
            inputPinEdit.setText(temp);
        }

    }

    @Override
    public void cancel() {
//        super.cancel();
    }

    public String getPinNum(){

        String pinNum = inputPinEdit.getText().toString();
        return pinNum;
    }

}

