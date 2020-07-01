package com.dataexpo.dataexpozkgate.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.dataexpo.dataexpozkgate.BascActivity;
import com.dataexpo.dataexpozkgate.R;
import com.dataexpo.dataexpozkgate.comm.DBUtils;
import com.dataexpo.dataexpozkgate.comm.FileUtils;
import com.dataexpo.dataexpozkgate.comm.Utils;
import com.dataexpo.dataexpozkgate.model.CoolCarVo;
import com.dataexpo.dataexpozkgate.model.FindResult;
import com.dataexpo.dataexpozkgate.model.TestResult;
import com.dataexpo.dataexpozkgate.net.HttpCallback;
import com.dataexpo.dataexpozkgate.net.HttpService;
import com.dataexpo.dataexpozkgate.net.URLs;
import com.dataexpo.dataexpozkgate.views.ConfigDialog;
import com.dataexpo.dataexpozkgate.views.LoginDialog;
import com.google.gson.Gson;
import com.zkteco.android.constant.SdkException;
import com.zkteco.android.device.Device;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;

/**
 * 中控闸机头   配合扫码头使用
 */
public class ScanShowActivity extends BascActivity implements View.OnClickListener, View.OnTouchListener {
    private static final String TAG = ScanShowActivity.class.getSimpleName();
    private Context mContext;
    private TextView tv_last;
    private TextView tv_qrcode_warning;
    private TextView tv_welcome;
    private TextView tv_expoid;
    private TextView tv_name;
    private TextView tv_code;
    private TextView tv_company;
    private TextView tv_role;
    private TextView tv_number;
    private TextView tv_check_model;
    private String qrcode = "";
    private String qrcode_last = "";
    private EditText et_code;
    private long perTime_qrcode = 0L;
    HashMap<Integer, Integer> soundMap = new HashMap<>();
    private SoundPool soundPool;

    private Button btn_log;
    private TextView tv_log;
    private TextView tv_count;

    private String[] values;

    //中控设备对象
    private Device mDevice;
    private volatile boolean isOpenDevice;

    private boolean running = true;
    private volatile int open = 1;
    private LockThread lockThread = null;

    //检查模式  0是离线模式  1是在线模式
    private int checkModel = 1;
    int showCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_scan_show);
        DBUtils.getInstance().create(mContext);
        initView();
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(mContext, R.raw.rescan, 1)); //播放的声音文件

        isOpenDevice = false;
        mDevice = new Device(getApplicationContext());
        mDevice.debugeInformation(true);

        IntentFilter intentFilter = new IntentFilter(com.zkteco.android.constant.Const.ACTION_USB_PERMISSION);
        registerReceiver(usbBroadcastReceiver, intentFilter);

        initData();
    }

    private void initData() {
        DBUtils.getInstance().listAll();
        showCount = DBUtils.getInstance().countToDay();
        Log.i(TAG, "today count: " + showCount);
        tv_count.setText(showCount + "");
    }

    private final BroadcastReceiver usbBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (com.zkteco.android.constant.Const.ACTION_USB_PERMISSION.equals(intent.getAction())) {
                Log.e(TAG, "receive");
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    try {
                        if (mDevice.openDevice()) {
                            Log.e(TAG, "open ok");
                            isOpenDevice = true;
                            Toast.makeText(getApplicationContext(), "Open device successful!", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Log.e(TAG, "open fail");
                            Toast.makeText(getApplicationContext(), "Open device failed, stop operate and check, or " +
                                    "Demo will crash", Toast
                                    .LENGTH_LONG).show();
                        }
                    } catch (SdkException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (!isOpenDevice) {
            try {
                if (mDevice.openDevice()) {
                    isOpenDevice = true;

                    //mDevice.setWatchdogTime(1000);
                    Toast.makeText(this, "Open device successful!", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(this, "Open device failed, stop operate and check, or Demo will crash", Toast
                            .LENGTH_LONG).show();
                }
            } catch (SdkException e) {
                Log.e(TAG, "Open UsbDevice failed");
                Toast.makeText(this, "Open device failed, stop operate and check, or Demo will crash", Toast
                        .LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        if (lockThread == null) {
            lockThread = new LockThread();
            lockThread.start();
        }
        Log.e(TAG, "onStart");
    }

    private void initView() {
        //findViewById(R.id.btn_offline_model_back).setOnClickListener(this);
        findViewById(R.id.tv_offline_check_in).setOnClickListener(this);
        tv_qrcode_warning = findViewById(R.id.tv_offline_qrcode_scan_warning);
        tv_last = findViewById(R.id.tv_offline_last_scan_value);
        et_code = findViewById(R.id.et_qrcode);
        tv_welcome = findViewById(R.id.tv_offline_qrcode_welcome);
        tv_expoid = findViewById(R.id.tv_expo_id);

        tv_name = findViewById(R.id.tv_expo_name);
        tv_name.setOnTouchListener(this);
        tv_code = findViewById(R.id.tv_expo_code);
        tv_code.setOnTouchListener(this);
        tv_company = findViewById(R.id.tv_expo_company);
        tv_company.setOnTouchListener(this);
        tv_role = findViewById(R.id.tv_expo_role);
        tv_role.setOnTouchListener(this);
        tv_number = findViewById(R.id.tv_expo_number);
        tv_number.setOnTouchListener(this);
        tv_count = findViewById(R.id.tv_count);
        tv_count.setOnTouchListener(this);
        tv_check_model = findViewById(R.id.tv_check_model);

        tv_log = findViewById(R.id.tv_log);
        btn_log = findViewById(R.id.btn_log);
        btn_log.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        et_code.requestFocus();
    }

    private void qrcodeScanEnd() {
        String scanValue = et_code.getText().toString().trim().replaceAll("\n", "");

        if (TextUtils.isEmpty(scanValue)) {
            //scanError(tv_qrcode_warning, R.string.null_scan);
            et_code.setText("");
            return;
        }

        //设置通码，当扫描结果是通码时，直接通行
        if ("YW000000".equals(scanValue)) {
            long dateTime = new Date().getTime();// - 60*60*24*1000;
            String date = Utils.formatTime(dateTime, "yyyy-MM-dd_HH:mm:ss");
            DBUtils.getInstance().insertData("通码", date, "", "", "", "");
            FileUtils.saveRecord("通码", date, "", "", "", "");
            open = 0;
            return;
        }

        if (!checkAES128CBC(scanValue)) {
            //scanError(tv_qrcode_warning, R.string.scan_value_error);
            final AlertDialog.Builder normalDialog =
                    new AlertDialog.Builder(mContext);
            normalDialog.setMessage("扫描内容异常！");
            normalDialog.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            normalDialog.setPositiveButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            // 显示
            normalDialog.show();
            tv_expoid.setText("");
            tv_name.setText("");
            tv_code.setText("");
            tv_company.setText("");
            tv_role.setText("");
            tv_number.setText("");
            tv_welcome.setVisibility(View.INVISIBLE);
            //tv_welcome.setText("");
            qrcode = "";
            playSound();
            et_code.setText("");
            return;
        }

//        if (((new Date().getTime()) - perTime_qrcode < 2000) && qrcode.equals(qrcode_last)) {
//            scanError(tv_qrcode_warning, R.string.repeat_scan);
//            et_code.setText("");
//            return;
//        }



        tv_qrcode_warning.setText("");
        //long dateTime = new Date().getTime();
        //String date = Utils.formatTime(dateTime, "yyyy-MM-dd_HH:mm:ss");
        //tv_welcome.setText(date);

        tv_expoid.setText(values[0]);
        tv_name.setText(values[1]);
        tv_code.setText(values[4]);
        tv_company.setText(values[2]);
        tv_role.setText(values[3]);
        tv_number.setText(values[5]);

        et_code.setText("");

        offlineCheckIn(values[1], values[2], values[3], values[4], values[0]);
        values = null;
    }

    private boolean checkAES128CBC(String str) {
        //获取第一个 ： 之后的内容进行解码
        String[] sp = str.split(":");
        if (sp.length != 2) {
            return false;
        }
        String decode = Utils.decrypt(sp[1]);
        //Log.i(TAG, "decode is " + decode);
        if (decode == null) {
            return false;
        }
        values = decode.split("\\|");
        //Log.i(TAG, "values.length " + values.length);
        if (values.length == 6 && ("2020A".equals(values[0]) || "2020B".equals(values[0]))) {
            qrcode = values[4];
            return true;
        }
        return false;
    }

    private void playSound() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    soundPool.play(soundMap.get(1), 1, // 左声道音量
                            1, // 右声道音量
                            1, // 优先级，0为最低
                            0, // 循环次数，0无不循环，-1无永远循环
                            1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
                    );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //Log.i(TAG, " event:" + event.toString());
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            //if ("".equals(tv_code.getText().toString().trim())) {
            qrcodeScanEnd();
            //}
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_log:
                //日志显示收放
                if (tv_log.getVisibility() == View.VISIBLE) {
                    tv_log.setVisibility(View.INVISIBLE);
                } else {
                    tv_log.setVisibility(View.VISIBLE);
                }

                break;
//            case R.id.btn_offline_model_back:
//                finish();
//                break;

            case R.id.tv_offline_check_in:
//                if (!checkInput()) {
//                    break;
//                }
//                toCheckIn();

                break;
            default:
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.tv_expo_name:
                case R.id.tv_expo_code:
                case R.id.tv_expo_company:
                case R.id.tv_expo_role:
                case R.id.tv_expo_number:
                case R.id.tv_count:
                    final LoginDialog dialog = new LoginDialog(ScanShowActivity.this);
                    dialog.setOnClickBottomListener(new LoginDialog.OnClickBottomListener() {
                        @Override
                        public void onPositiveClick() {
                            Log.i(TAG, dialog.getInput() + "-----------------------");

                            if ("159632".equals(dialog.getInput())) {
                                final ConfigDialog configDialog = new ConfigDialog(ScanShowActivity.this, checkModel == 1);

                                configDialog.setOnClickBottomListener(new ConfigDialog.OnClickBottomListener() {
                                    @Override
                                    public void onPositiveClick() {
                                        Log.i(TAG, configDialog.isCheck() + "-----------------------");
                                        if (configDialog.isCheck()) {
                                            checkModel = 1;
                                            tv_check_model.setBackgroundColor(getResources().getColor(R.color.bg_green));
                                        } else {
                                            checkModel = 0;
                                            tv_check_model.setBackgroundColor(getResources().getColor(R.color.bg_color_tiny));
                                        }
                                        configDialog.dismiss();
                                    }

                                    @Override
                                    public void onNegtiveClick() {
                                        configDialog.dismiss();
                                    }
                                });
                                configDialog.show();
                            } else {
                                et_code.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ScanShowActivity.this, "密码错误！！", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegtiveClick() {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    break;
                default:
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbBroadcastReceiver);
        mDevice.closeDevice();
        running = false;
    }

//    private void toCheckIn() {
//        if (DBUtils.getInstance().count(qrcode) > 1) {
//            final AlertDialog.Builder normalDialog =
//                    new AlertDialog.Builder(mContext);
//            normalDialog.setMessage("当日已签到超过2次，是否继续签到！");
//            normalDialog.setNegativeButton("确定",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            offlineCheckIn();
//                            //Log.i(TAG, "delete codesize :" + userInfo.data.euPrintCount);
//                        }
//                    });
//            normalDialog.setPositiveButton("取消",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            tv_expoid.setText("");
//                            tv_name.setText("");
//                            tv_code.setText("");
//                            tv_company.setText("");
//                            tv_role.setText("");
//                            tv_number.setText("");
//                            //tv_welcome.setText("");
//                            qrcode = "";
//                        }
//                    });
//            // 显示
//            normalDialog.show();
//        } else {
//            offlineCheckIn();
//        }
//    }

    private void offlineCheckIn(String name, String company, String role, String code, String expoid) {
        CoolCarVo vo = new CoolCarVo();
        vo.setCode(code);
        vo.setCompany(company);
        vo.setRole(role);
        vo.setExpoId(expoid);
        vo.setName(name);
        vo.setDeviceSerial("testDev0002");
        if ("2020A".equals(expoid)) {
            vo.setType(1);
        } else {
            vo.setType(2);
        }

        //在线模式
        if (checkModel == 1) {
            MyAsyncTask task  = new MyAsyncTask();
            task.execute(vo);
        } else {
            long dateTime = new Date().getTime();// - 60*60*24*1000;
            String date = Utils.formatTime(dateTime, "yyyy-MM-dd_HH:mm:ss");
            DBUtils.getInstance().insertData(name, date, company, role, code, expoid);
            FileUtils.saveRecord(name, date, company, role, code, expoid);
            tv_last.setText(qrcode_last);

            qrcode_last = qrcode;
            tv_count.setText(++showCount + "");

            openGateDoor();
            Toast.makeText(ScanShowActivity.this, "开门", Toast.LENGTH_LONG).show();

            perTime_qrcode = dateTime;
            Log.i(TAG, "requestFocus!!!");
            et_code.requestFocus();
        }

//        long dateTime = new Date().getTime();// - 60*60*24*1000;
//        String date = Utils.formatTime(dateTime, "yyyy-MM-dd_HH:mm:ss");
//        DBUtils.getInstance().insertData(name, date, company, role, code, expoid);
//        FileUtils.saveRecord(name, date, company, role, code, expoid);
//        tv_last.setText(qrcode_last);
//
//        qrcode_last = qrcode;
//        //getCanOpen();
//
//        openGateDoor();
//
//        perTime_qrcode = dateTime;
//        String c = "2020A|杨勇勇|数展|监管机构|12345678987650|2020A";
//        String encode = Utils.encrypt(c);
//        Log.i(TAG, " a  code: " + encode);
    }

    private void getCanOpen() {
        //展会ID 和门禁地址的map
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("euCode", tv_code.getText().toString());
        hashMap.put("deviceKey", "1000");
        hashMap.put("recordTime", Utils.formatTime((new Date()).getTime(), "yyyy-MM-dd HH:mm:ss"));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HttpService.getWithParams(mContext, URLs.gaizhuangche, hashMap, new HttpCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "网络异常，请重新验证", Toast.LENGTH_SHORT).show();
                            }
                        });
                        if (e.getMessage() != null)
                            Log.i(TAG, e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.i(TAG, "onResponse " + response + "  " + id);
                        final FindResult userInfo = new Gson().fromJson(response, FindResult.class);
                        if (userInfo.errcode == 0) {
                            openGateDoor();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_welcome.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            tv_welcome.setText("超过使用次数");
                            tv_welcome.setVisibility(View.VISIBLE);
                            Toast.makeText(mContext, "超过使用次数", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


//        hashMap.put("Expo_id", "10351");
//        hashMap.put("Add", "2");
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                HttpService.getWithParams(mContext, URLs.VerifyExpo, hashMap, new HttpCallback() {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(mContext, "网络异常，请重新验证", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                        if (e.getMessage() != null)
//                            Log.i(TAG, e.getMessage());
//                    }
//
//                    @Override
//                    public void onResponse(String response, int id) {
//                        Log.i(TAG, "onResponse " + response + "  " + id);
//                        openGateDoor();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tv_welcome.setVisibility(View.VISIBLE);
//                            }
//                        });
//                    }
//                });
//            }
//        });
    }

    private void openGateDoor() {
        tv_log.append("scanEnd openGateDoor " );
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ScanShowActivity.this, "set open 0 ", Toast.LENGTH_SHORT).show();
                }
            });
            open = 0;
            //开门
            //mDevice.setLock1(0);
            //Thread.sleep(2000);
            //mDevice.setLock1(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkInput() {
        if (TextUtils.isEmpty(qrcode)) {
            return false;
        }

        //TODO : some code check
        return true;
    }

    private void scanError(TextView v, int msgId) {
        v.setText(msgId);
    }

    public class MyAsyncTask extends AsyncTask<CoolCarVo, String, CoolCarVo> {
        @Override
        protected CoolCarVo doInBackground(CoolCarVo... vos) {
            CoolCarVo vo = vos[0];
            RestTemplate template = new RestTemplate();

            ResponseEntity<TestResult> result = null;
            try {
                result = template.postForEntity("http://reg.dataexpo.com.cn/gate-api/api/checkCodeInsert", vo, TestResult.class);
                //result = template.postForEntity("http://192.168.1.8:8096/api/checkCodeInsert", vo, TestResult.class);
            } catch (Exception e) {
                et_code.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScanShowActivity.this, "服务器接口访问异常！！", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }

            if (result == null) {
                return null;
            }
            TestResult re = result.getBody();
            if (re == null) {
                return null;
            }

            Log.i(TAG, re.getErrcode() + "----- " + re.getData());
            if (re.getErrcode() == 0 && (boolean)re.getData()) {
                return vo;
            } else {
                return null;
            }
//            Log.i("ceshi ", result.getBody().getData() + " " +
//                    result.getBody().getErrcode());
//
//            return  "ok";
        }
        /**
         * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
         * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
         */
        @Override
        protected void onPostExecute(final CoolCarVo result) {
            //btn.setText("线程结束" + result);
            Log.i(TAG, "exec end  " + result);
            et_code.post(new Runnable() {
                @Override
                public void run() {
                    et_code.requestFocus();
                }
            });

            if (result == null) {
                et_code.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScanShowActivity.this, "超过使用次数！！！", Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
            final long dateTime = new Date().getTime();

            tv_last.post(new Runnable() {
                @Override
                public void run() {
                    // - 60*60*24*1000;
                    String date = Utils.formatTime(dateTime, "yyyy-MM-dd_HH:mm:ss");
                    DBUtils.getInstance().insertData(result.getName(), date, result.getCompany(), result.getRole(), result.getCode(), result.getExpoId());
                    FileUtils.saveRecord(result.getName(), date, result.getCompany(), result.getRole(), result.getCode(), result.getExpoId());
                    tv_last.setText(qrcode_last);
                    tv_count.setText(++showCount + "");
                    Toast.makeText(ScanShowActivity.this, "开门", Toast.LENGTH_LONG).show();
                }
            });

            qrcode_last = qrcode;

            openGateDoor();

            perTime_qrcode = dateTime;
        }
        //该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
        @Override
        protected void onPreExecute() {
            //btn.setText("开始执行异步线程");
        }
        @Override
        protected void onProgressUpdate(String... values) {

        }
    }

    class LockThread extends Thread {
        @Override
        public void run() {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(ScanShowActivity.this, "LockThread run ", Toast.LENGTH_SHORT).show();
//                }
//            });
            while (running) {
                try {
                    if (open == 0) {

                        mDevice.setLock1(0);
                        sleep(2000);
                        open = 1;
                        mDevice.setLock1(1);

                    } else {
                        sleep(50);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
