package com.dataexpo.dataexpozkgate.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.dataexpo.dataexpozkgate.R;

public class ConfigDialog extends Dialog {
    private boolean check;
    CheckBox checkBox;
    private Context mContext;

    public ConfigDialog(Context context, boolean check) {
        super(context);
        this.check = check;
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_configdialog);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.layout_configdialog, null);

        checkBox = findViewById(R.id.check_on_line);

        checkBox.setChecked(check);

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickBottomListener!= null) {
                    onClickBottomListener.onNegtiveClick();
                }
            }
        });

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickBottomListener!= null) {
                    check = checkBox.isChecked();
                    Log.i("ConfigDialog ", "input" + check);

                    onClickBottomListener.onPositiveClick();
                }
            }
        });
    }

    public OnClickBottomListener onClickBottomListener;

    public ConfigDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }

    public interface OnClickBottomListener{
        /**
         * 点击确定按钮事件
         */
        public void onPositiveClick();
        /**
         * 点击取消按钮事件
         */
        public void onNegtiveClick();
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
