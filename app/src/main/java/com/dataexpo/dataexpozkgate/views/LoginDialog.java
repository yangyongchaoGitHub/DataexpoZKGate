package com.dataexpo.dataexpozkgate.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.dataexpo.dataexpozkgate.R;

public class LoginDialog extends Dialog {
    private String input;
    private EditText et_input;

    public LoginDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_logindialog);
        initView();
    }

    private void initView() {
        et_input = findViewById(R.id.et_input);
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
                    input = et_input.getText().toString();
                    Log.i("LoginDialog ", "input" + input);

                    onClickBottomListener.onPositiveClick();
                }
            }
        });
    }

    public OnClickBottomListener onClickBottomListener;

    public LoginDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
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


    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}
