package ui;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.materialmanagement.R;
import com.google.gson.Gson;
import com.google.zxing.Result;
import com.king.zxing.BeepManager;
import com.king.zxing.CaptureActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bean.OutLibraryHistoryBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utile.CustomDialog;
import utile.DialogUtil;
import utile.NetUtils;
import utile.PortIpAddress;
import utile.SharedPrefsUtil;
import utile.ShowToast;
import utile.StatusBarUtils;

import static com.example.administrator.materialmanagement.MyApplication.sqldb;
import static ui.OutLibraryResult.titleCode;
import static utile.PortIpAddress.CODE;
import static utile.PortIpAddress.MESSAGE;
import static utile.PortIpAddress.SUCCESS_CODE;

public class CustomCaptureActivity extends CaptureActivity {
    @BindView(R.id.sm_num_tv)
    TextView sm_num_tv;
    private BeepManager beepManager;
    private List<OutLibraryHistoryBean> mDatas; //存放码的集合
    private int sm_num = 0; //扫码的数量和
    private int local_num = 0;
    private boolean isScanning = true;

    private String tag = "";
    private String dealersid = "";
    private String dealersname = "";
    private String postUrl = "";
    private String keyCode = "";
    private String taskid = "";
    private String outdate = "";
    private String resultString = "";
    @BindView(R.id.upload)
    ImageView upload;
    @BindView(R.id.local_barcode_size)
    TextView local_barcode_size;
    @BindView(R.id.custom_capture_tv_jxsname)
    TextView custom_capture_tv_jxsname;
    @BindView(R.id.custom_capture_rwdate)
    TextView custom_capture_rwdate;
    @BindView(R.id.sm_result_tv)
    TextView sm_result_tv;
    private static CustomDialog.Builder builder;
    private Dialog myDialog;

    private Dialog dialog;

    private Cursor cursor;
    private List<OutLibraryHistoryBean> barCodeList;

    public BeepManager getBeepManager() {
        return beepManager;
    }

    @Override
    public int getLayoutId() {
        return R.layout.custom_capture_activity;
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        dealersid = intent.getStringExtra("dealersid");
        dealersname = intent.getStringExtra("dealersname");
        postUrl = intent.getStringExtra("postUrl");
        keyCode = intent.getStringExtra("keyCode");
        taskid = intent.getStringExtra("taskid");
        outdate = intent.getStringExtra("outdate");

        custom_capture_tv_jxsname.setText("当前任务经销商名称：" + dealersname);
        custom_capture_rwdate.setText("当前任务创建时间：" + outdate + "点");


        sm_num_tv.setText(String.valueOf(sm_num));
        beepManager = new BeepManager(this);
        StatusBarUtils.transparencyBar(this);
        mDatas = new ArrayList<>();

        barCodeList = new ArrayList<>();
        //通过经销商id和时间 遍历出存放本地数据库中的码值
        try {
            Cursor cursor = sqldb.rawQuery("select * from out_storage where  Dealersid=? and Date=?", new String[]{dealersid, outdate});
            while (cursor.moveToNext()) {
                OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                bean.setBm(cursor.getString(cursor.getColumnIndex("Barcode")));
                barCodeList.add(bean);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        local_barcode_size.setText(barCodeList.size() + "");

        if (tag.equals("tk")) {
            upload.setVisibility(View.GONE);
            local_barcode_size.setVisibility(View.GONE);
        }

        getBeepManager().setPlayBeep(true);
        getBeepManager().setVibrate(true);
    }

    @OnClick(R.id.custom_capture_back)
    void Back() {
        finish();
    }


    /**
     * 离线提交存入本地的码值
     */
    @OnClick(R.id.upload)
    void Submit() {
        //有网的时候遍历本地数据库并提交当前这个任务的数据   通过经销商id 时间 去查询
        if (NetUtils.isConnected(this)) {
            barCodeList = new ArrayList<>();
            //通过经销商id和时间 遍历出存放本地数据库中的码值
            try {
                Cursor cursor = sqldb.rawQuery("select * from out_storage where  Dealersid=? and Date=?", new String[]{dealersid, outdate});

                while (cursor.moveToNext()) {
                    OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                    bean.setBm(cursor.getString(cursor.getColumnIndex("Barcode")));
                    barCodeList.add(bean);
                }

                if (barCodeList.size() > 0) {
                    NewTask();
                } else {
                    ShowToast.showShort(this, "当前任务本地未保存码值");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            ShowToast.showShort(this, "请检查网络后提交本地数据");
        }
    }


    /**
     * 通过当前任务的经销商id 和时间 先创建当前的进度任务
     */
    private void NewTask() {
        dialog = DialogUtil.createLoadingDialog(CustomCaptureActivity.this, R.string.newTask);
        OkGo.<String>get(PortIpAddress.OutLibraryStepTwo())
                .tag(this)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
                .params("bean.dealersid", dealersid)
                .params("bean.outdate", outdate)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String jsonStr = response.body().toString();
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                ShowToast.showShort(CustomCaptureActivity.this, "创建任务成功");
                                mHandler.sendEmptyMessageDelayed(2, 2000);
                            } else {
                                ShowToast.showShort(CustomCaptureActivity.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }


                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ShowToast.showShort(CustomCaptureActivity.this, R.string.connect_err);
                        dialog.dismiss();
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                    }
                });
    }


    /**
     * 提交本地的码值
     */
    private void UpLoadBarcode() {
        Gson gson = new Gson();
        String barcodeJson = gson.toJson(barCodeList);
        dialog = DialogUtil.createLoadingDialog(CustomCaptureActivity.this, R.string.uploading);
        OkGo.<String>get(PortIpAddress.OffLineCk())
                .tag(this)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
                .params(keyCode + ".taskid", taskid)
                .params(keyCode + ".maxcode", barcodeJson)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String jsonStr = response.body().toString();
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                ShowToast.showShort(CustomCaptureActivity.this, "上传成功");
                                //提交完本地数据成功之后  将当前本地数据库中的数据清空
                                sqldb.execSQL("delete from out_storage where Dealersid=? and Date=?", new String[]{dealersid, outdate});
                                //设置提示为0
                                local_barcode_size.setText("0");
                            } else {
                                ShowToast.showShort(CustomCaptureActivity.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ShowToast.showShort(CustomCaptureActivity.this, R.string.connect_err);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dialog.dismiss();
                    }
                });
    }


//    @Override
//    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
//        resultString = rawResult.getText();
//        resultString = resultString.replace(titleCode, "MIN");
//        if (isBeepSoundAndVibrate()) {
//            beepManager.playBeepSoundAndVibrate();
//        }
//        sm_result_tv.setText(resultString);
//
//        builder = new CustomDialog.Builder(this);
//        builder.setTitle("扫码提示");
//        builder.setMessage("扫码结果：" + resultString);
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (isScanning) {  //不让数据重复添加
//                    isScanning = false;
//                    beepManager.setPlayBeep(false);
//                    beepManager.setVibrate(false);
//
//
//                    if (NetUtils.isConnected(CustomCaptureActivity.this)) {
//                        mConnect(resultString);
//                    } else {
//                        if (tag.equals("ck")) {
//                            try {
//                                //判断表中是否存在相同数据
//                                cursor = sqldb.rawQuery("select * from out_storage where Dealersid=? and Barcode=? and Date =?", new String[]{dealersid, resultString, outdate});
//
//                                if (cursor.getCount() > 0) {
//                                    ShowToast.showShort(CustomCaptureActivity.this, "本地已存在此条信息");
//                                } else {
//                                    mHandler.sendEmptyMessage(1);
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            ShowToast.showShort(CustomCaptureActivity.this, R.string.connect_err);
//                        }
//                    }
////            mConnect(resultString);
//                    mHandler.sendEmptyMessageDelayed(0, 3000);  //避免过快触发影响体验
//
//                }
//                sm_result_tv.setText("");
//                dialog.dismiss();
//            }
//        });
//
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                sm_result_tv.setText("");
//                dialog.dismiss();
//                if (handler != null) {
//                    handler.restartPreviewAndDecode(); // 实现多次扫描
//                }
//            }
//        });
//        myDialog = builder.create();
//        myDialog.setCanceledOnTouchOutside(false);
//        myDialog.setCancelable(false);
//        myDialog.show();
//    }

    //    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_HOME:
//                if (myDialog.isShowing())
//                    myDialog.dismiss();
//                break;
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }


    /**
     * 重写handleDecode   进行连续扫码
     */
    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        resultString = rawResult.getText();

        if (isBeepSoundAndVibrate()) {
            beepManager.playBeepSoundAndVibrate();
        }

//        mHandler.sendEmptyMessageDelayed(0, 3000);
//        beepManager.playBeepSoundAndVibrate();

        if (isScanning) {  //不让数据重复添加
            isScanning = false;
            beepManager.setPlayBeep(false);
            beepManager.setVibrate(false);
            resultString = resultString.replace(titleCode, "MIN");

            if (NetUtils.isConnected(this)) {
                mConnect(resultString);
            } else {
                if (tag.equals("ck")) {
                    try {
                        //判断表中是否存在相同数据
                        cursor = sqldb.rawQuery("select * from out_storage where Dealersid=? and Barcode=? and Date =?", new String[]{dealersid, resultString, outdate});

                        if (cursor.getCount() > 0) {
                            ShowToast.showShort(this, "本地已存在此条信息");
                        } else {
                            mHandler.sendEmptyMessage(1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShowToast.showShort(CustomCaptureActivity.this, R.string.connect_err);
                }
            }
//            mConnect(resultString);
            mHandler.sendEmptyMessageDelayed(0, 3000);  //避免过快触发影响体验
            sm_result_tv.setText(resultString);
        }

//        Intent intent = new Intent();
//        intent.putExtra(KEY_RESULT, resultString);
//        setResult(RESULT_OK, intent);
//        finish();
//        Intent intent = new Intent();
//        intent.putStringArrayListExtra(KEY_RESULT, (ArrayList<String>) mDatas);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(0);
        if (cursor != null) {
            cursor.close();
        }
    }


    Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0:
                    isScanning = true;
                    beepManager.setPlayBeep(true);
                    beepManager.setVibrate(true);
                    if (handler != null) {
                        handler.restartPreviewAndDecode(); // 实现多次扫描
                    }
                    break;
                case 1:
                    try {
                        sqldb.execSQL("insert into out_storage(Dealersid,Date,Barcode) values(?,?,?)", new String[]{
                                dealersid, outdate, resultString
                        });
                        local_num++;
                        local_barcode_size.setText((local_num + barCodeList.size()) + "");
                        ShowToast.showShort(CustomCaptureActivity.this, "存入本地数据库成功");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    UpLoadBarcode();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 提交编码
     *
     * @param result
     */
    private void mConnect(String result) {
        OkGo.<String>get(postUrl)
                .tag(this)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
                .params(keyCode + ".taskid", taskid)
                .params(keyCode + ".maxcode", result)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                sm_num++;
                                sm_num_tv.setText(String.valueOf(sm_num));
                                OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                                bean.setBm(jsonObject.getString(keyCode + ".maxcode"));
                                mDatas.add(bean);
                            } else {
                                ShowToast.showShort(CustomCaptureActivity.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ShowToast.showShort(CustomCaptureActivity.this, R.string.connect_err);
                    }
                });
    }


    /**
     * 闪光灯关闭
     */
    private void offFlash() {
        Camera camera = getCameraManager().getOpenCamera().getCamera();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
    }

    /**
     * 闪光灯开启
     */
    public void openFlash() {
        Camera camera = getCameraManager().getOpenCamera().getCamera();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
    }

    private void clickFlash(View v) {
        if (v.isSelected()) {
            offFlash();
            v.setSelected(false);
        } else {
            openFlash();
            v.setSelected(true);
        }
    }

    public void OnClick(View v) {
        switch (v.getId()) {
            case R.id.ivLeft:
                onBackPressed();
                break;
            case R.id.ivFlash:
                clickFlash(v);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myDialog != null && myDialog.isShowing())
            myDialog.dismiss();
    }
}
