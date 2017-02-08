package com.kyounghyun.iotproject.condition;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kyounghyun.iotproject.R;
import com.kyounghyun.iotproject.application.ApplicationController;
import com.kyounghyun.iotproject.dialog.DialogPinNum;
import com.kyounghyun.iotproject.dialog.DialogTargetList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConditionActivity extends AppCompatActivity {


    @BindView(R.id.targetDeviceSet)
    Switch targetDeviceSetBtn;
    @BindView(R.id.pinPwdSet)
    Switch pinPwdSetBtn;
    @BindView(R.id.deviceCountNum)
    TextView countNum;
    @BindView(R.id.minusBtn)
    Button minusBtn;
    @BindView(R.id.plusBtn)
    Button plusBtn;

    DialogTargetList targetSetDialog;
    DialogPinNum pinDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition);


        ButterKnife.bind(this);




        if(ApplicationController.connectInfo.getBoolean("target_check", false))
            targetDeviceSetBtn.setChecked(true);
        else
            targetDeviceSetBtn.setChecked(false);


        if(ApplicationController.connectInfo.getBoolean("pin_check", false))
            pinPwdSetBtn.setChecked(true);
        else
            pinPwdSetBtn.setChecked(false);

        String temp = ApplicationController.connectInfo.getString("countNum", "");


        if(temp == null || temp.equals("")){
            countNum.setText("0");
        }
        else{
            countNum.setText(temp);
        }


        targetDeviceSetBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
//                    Toast.makeText(getApplicationContext(),"ON",Toast.LENGTH_SHORT).show();

                    ApplicationController.editor.putBoolean("target_check", true);
                    ApplicationController.editor.commit();

                    showTargetSetDialog();

                }
                else{
//                    Toast.makeText(getApplicationContext(),"OFF",Toast.LENGTH_SHORT).show();
                    ApplicationController.editor.putBoolean("target_check", false);
                    ApplicationController.editor.commit();
                }
            }
        });

        pinPwdSetBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
//                    Toast.makeText(getApplicationContext(),"ON",Toast.LENGTH_SHORT).show();

                    ApplicationController.editor.putBoolean("pin_check", true);
                    ApplicationController.editor.commit();

                    showInputPinDialog();

                }
                else{
//                    Toast.makeText(getApplicationContext(),"OFF",Toast.LENGTH_SHORT).show();
                    ApplicationController.editor.putBoolean("pin_check", false);
                    ApplicationController.editor.commit();
                }
            }
        });

    }



    @OnClick(R.id.minusBtn)
    public void setMinusCount(){

        int num = Integer.valueOf(countNum.getText().toString());

        if(num < 1)
            Toast.makeText(getApplicationContext(),"0 이하 불가",Toast.LENGTH_SHORT).show();
        else
        {
            countNum.setText(String.valueOf(--num));

            ApplicationController.editor.putString("countNum", String.valueOf(num));
            ApplicationController.editor.commit();
        }

    }

    @OnClick(R.id.plusBtn)
    public void setPlusCount(){

        int num = Integer.valueOf(countNum.getText().toString());
        int max = ApplicationController.getInstance().mDbOpenHelper.DbMainSelect().size();



        if(num >= max)
            Toast.makeText(getApplicationContext(), max +" 초과 불가",Toast.LENGTH_SHORT).show();
        else
        {
            countNum.setText(String.valueOf(++num));

            ApplicationController.editor.putString("countNum", String.valueOf(num));
            ApplicationController.editor.commit();
        }

    }

    public void showTargetSetDialog(){
        WindowManager.LayoutParams loginParams;
        targetSetDialog = new DialogTargetList(this, R.style.DialogTheme);

        targetSetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        targetSetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        targetSetDialog.setCanceledOnTouchOutside(false);


        loginParams = targetSetDialog.getWindow().getAttributes();

        // Dialog 사이즈 조절 하기
        loginParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        loginParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        targetSetDialog.getWindow().setAttributes(loginParams);
        targetSetDialog.show();
    }


    public void showInputPinDialog(){
        WindowManager.LayoutParams loginParams;
        pinDialog = new DialogPinNum(this, R.style.DialogTheme, completeEvent, cancelEvent);

        pinDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        pinDialog.setCanceledOnTouchOutside(false);


        loginParams = pinDialog.getWindow().getAttributes();

        // Dialog 사이즈 조절 하기
        loginParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        loginParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        pinDialog.getWindow().setAttributes(loginParams);
        pinDialog.show();
    }


    private View.OnClickListener completeEvent = new View.OnClickListener() {
        public void onClick(View v) {

            String num = pinDialog.getPinNum();

            if(num.length() < 6)
            {
                Toast.makeText(getApplicationContext(),"6글자로 구성해주세요.",Toast.LENGTH_SHORT).show();
            }
            else{

                ApplicationController.editor.putString("pinNum", num);
                ApplicationController.editor.commit();

                Toast.makeText(getApplicationContext(),"설정 완료!",Toast.LENGTH_SHORT).show();
                pinDialog.dismiss();
            }



        }

    };


    private View.OnClickListener cancelEvent = new View.OnClickListener() {
        public void onClick(View v) {

            pinPwdSetBtn.setChecked(false);
            ApplicationController.editor.putBoolean("pin_check", false);
            ApplicationController.editor.commit();

            pinDialog.dismiss();

        }

    };

}
