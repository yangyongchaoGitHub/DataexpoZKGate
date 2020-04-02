package com.dataexpo.dataexpozkgate.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dataexpo.dataexpozkgate.BascActivity;
import com.dataexpo.dataexpozkgate.R;
import com.dataexpo.dataexpozkgate.comm.DBUtils;
import com.dataexpo.dataexpozkgate.listener.OnItemClickListener;
import com.dataexpo.dataexpozkgate.model.Code;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ScanRecordActivity extends BascActivity implements View.OnClickListener, OnItemClickListener {
    private final String TAG = ScanRecordActivity.class.getSimpleName();
    private Context mContext;
    private TextView tv_total;
    private TextView tv_total_today;
    private List<Code> codes;
    private CodeRecordAdapter dateAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_scan_record);
        initView();
        initData();
    }

    private void initData() {
//        codes = DBUtils.getInstance().listAll();
//        tv_total.setText(String.valueOf(codes.size()));
//        tv_total_today.setText(String.valueOf(codes.size()));
//        dateAdapter = new CodeRecordAdapter();
//        recyclerView.setAdapter(dateAdapter);
//
//        dateAdapter.setItemClickListener(this);
//        dateAdapter.setData(codes);
    }

    private void initView() {
        findViewById(R.id.btn_scan_record_back).setOnClickListener(this);
        findViewById(R.id.tv_scan_record_back_scan).setOnClickListener(this);
        tv_total = findViewById(R.id.tv_scan_record_total_value);
        tv_total_today = findViewById(R.id.tv_scan_record_today_value);
        recyclerView = findViewById(R.id.recycler_scan_record);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        findViewById(R.id.btn_scan_record_save).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan_record_back:
            case R.id.tv_scan_record_back_scan:
                this.finish();
                break;

            case R.id.btn_scan_record_save:
                final ZLoadingDialog zdialog = new ZLoadingDialog(mContext);
                zdialog.setLoadingBuilder(Z_TYPE.CIRCLE)//设置类型
                        .setLoadingColor(Color.BLACK)//颜色
                        .setHintText("Loading...")
                        .setCanceledOnTouchOutside(false)
                        .show();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
                long dateTime = new Date().getTime();
                String date = simpleDateFormat.format(dateTime);
                String fileName = date + ".txt";
                File file = new File("/sdcard/ScanRecord/");
                if (!file.exists()) {
                    file.mkdirs();
                }
                file = new File(file, fileName);
                try {
                    FileWriter fw = new FileWriter(file, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter printWriter = new PrintWriter(bw);
                    for (Code code : codes) {
                        String strContent = code.eucode + " " + code.printtime + "\r\n";
                        printWriter.println(strContent);
                    }
                    printWriter.close();
                    bw.close();
                    fw.close();
                    zdialog.cancel();
                    Toast.makeText(mContext, "导出成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    zdialog.cancel();
                    Toast.makeText(mContext, "导出失败", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    public void onItemClick(View view, final int position) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        normalDialog.setMessage("是否删除该数据?不可恢复!");
        normalDialog.setPositiveButton("删除",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBUtils.getInstance().delData(codes.get(position).id);
                        codes.remove(position);
                        tv_total.setText(String.valueOf(codes.size()));
                        tv_total_today.setText(String.valueOf(codes.size()));
                        dateAdapter.notifyDataSetChanged();
                        Log.i(TAG, "delete codesize :" + codes.size());
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: cancel button
                    }
                });
        // 显示
        normalDialog.show();
    }

    private static class FaceUserHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView tv_eucode;
        private TextView tv_printtime;
        private TextView tv_delete;

        public FaceUserHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tv_eucode = itemView.findViewById(R.id.tv_item_record_eucode);
            tv_printtime = itemView.findViewById(R.id.tv_item_record_ctime);
            tv_delete = itemView.findViewById(R.id.btn_item_record_delete);
        }
    }

    public class CodeRecordAdapter extends RecyclerView.Adapter<FaceUserHolder> implements View.OnClickListener {
        private List<Code> mList;
        private OnItemClickListener mItemClickListener;

        public void setData(List<Code> list) {
            mList = list;
        }

        @Override
        public void onClick(View v) {
//            if (mItemClickListener != null) {
//                mItemClickListener.onItemClick(v, (Integer) v.getTag());
//            }
        }

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public FaceUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_record, parent, false);
            FaceUserHolder viewHolder = new FaceUserHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull FaceUserHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
        }

        @Override
        public void onBindViewHolder(@NonNull FaceUserHolder holder, final int position) {
            holder.itemView.setTag(position);
            // 添加数据
            holder.tv_eucode.setText(mList.get(position).eucode);
            holder.tv_printtime.setText(mList.get(position).printtime);

            holder.tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener!= null) {
                        mItemClickListener.onItemClick(v, position);
                    }
                }
            });

            Log.i(TAG, "size " + mList.size());
            //渲染奇数行
            if ((position & 0x01) == 0) {
                holder.itemView.setBackgroundColor(Color.parseColor("#FF7CAFF7"));
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE);
            }
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(TAG, " event:" + event.toString());

        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
