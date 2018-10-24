package ui;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.materialmanagement.R;
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
import utile.NetUtils;
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
    private boolean isScanning = true;

    private String tag = "";
    private String postUrl = "";
    private String keyCode = "";
    private String taskid = "";
    private String outdate = "";
    private String resultString = "";

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
        postUrl = intent.getStringExtra("postUrl");
        keyCode = intent.getStringExtra("keyCode");
        taskid = intent.getStringExtra("taskid");
        outdate = intent.getStringExtra("outdate");

        sm_num_tv.setText(String.valueOf(sm_num));
        beepManager = new BeepManager(this);
        StatusBarUtils.transparencyBar(this);
        mDatas = new ArrayList<>();
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
    @OnClick(R.id.submit)
    void Submit() {
        //通过当前任务的经销商id 和时间 先创建当前的进度任务

        //有网的时候遍历本地数据库并提交当前这个任务的数据   通过经销商id 时间 去查询
        if (NetUtils.isConnected(this)) {
            barCodeList = new ArrayList<>();
            //通过经销商id和时间 遍历出存放本地数据库中的码值
            Cursor cursor = sqldb.rawQuery("select * from out_storage", null);
            while (cursor.moveToNext()) {
                OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                bean.setCkrq(cursor.getString(cursor.getColumnIndex("Date")));
                bean.setBm(cursor.getString(cursor.getColumnIndex("Barcode")));
                barCodeList.add(bean);
            }
            Log.e("TAG", barCodeList.size() + "");
            //提交完本地数据成功之后  将当前本地数据库中的数据清空
        }else{
            ShowToast.showShort(this,"请检查网络后提交本地数据");
        }
    }

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
                try {
                    //判断表中是否存在相同数据
                    cursor = sqldb.rawQuery("select * from out_storage where Barcode=? and Date =?", new String[]{resultString, outdate});

                    if (cursor.getCount() > 0) {
                        ShowToast.showShort(this, "本地已存在此条信息");
                    } else {
                        mHandler.sendEmptyMessage(1);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mConnect(resultString);
            }
//            mConnect(resultString);
            mHandler.sendEmptyMessageDelayed(0, 3000);  //避免过快触发影响体验
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
                        sqldb.execSQL("insert into out_storage(Date,Barcode) values(?,?)", new String[]{
                                outdate, resultString
                        });
                        ShowToast.showShort(CustomCaptureActivity.this, "存入数据库成功");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
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


}
