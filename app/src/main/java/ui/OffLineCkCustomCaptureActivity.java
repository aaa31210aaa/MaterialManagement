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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.materialmanagement.R;
import com.google.zxing.Result;
import com.king.zxing.BeepManager;
import com.king.zxing.CaptureActivity;

import java.util.ArrayList;
import java.util.List;

import bean.OutLibraryHistoryBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utile.CustomDialog;
import utile.PortIpAddress;
import utile.ShowToast;
import utile.StatusBarUtils;

import static com.example.administrator.materialmanagement.MyApplication.sqldb;
import static ui.OutLibraryResult.titleCode;

public class OffLineCkCustomCaptureActivity extends CaptureActivity {

    @BindView(R.id.sm_num_tv)
    TextView sm_num_tv;
    private BeepManager beepManager;
    private List<OutLibraryHistoryBean> mDatas; //存放码的集合
    private int sm_num = 0; //扫码的数量和
    //    private int local_num = 0;
    private boolean isScanning = true;

    private String dealersid = "";
    private String dealersname = "";
    private String postUrl = "";
    private String keyCode = "";
    //    private String taskid = "";
    private String outdate = "";
    private String resultString = "";
    //    @BindView(R.id.upload)
//    ImageView upload;
//    @BindView(R.id.local_barcode_size)
//    TextView local_barcode_size;
    @BindView(R.id.custom_capture_tv_jxsname)
    TextView custom_capture_tv_jxsname;
    @BindView(R.id.custom_capture_rwdate)
    TextView custom_capture_rwdate;
    @BindView(R.id.sm_result_tv)
    TextView sm_result_tv;
    @BindView(R.id.manual_input_etv)
    EditText manual_input_etv;
    @BindView(R.id.manual_input_btn)
    Button manual_input_btn;
    @BindView(R.id.manual_etv_clear)
    ImageView manual_etv_clear;
    @BindView(R.id.edit_rl)
    RelativeLayout edit_rl;
    //    @BindView(R.id.open_shrink)
//    ImageView open_shrink;
    private boolean isOpen = false;


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
        return R.layout.activity_off_line_ck_custom_capture;
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        dealersid = intent.getStringExtra("dealersid");
        dealersname = intent.getStringExtra("dealersname");
        outdate = intent.getStringExtra("outdate");

        postUrl = intent.getStringExtra("postUrl");
        keyCode = intent.getStringExtra("keyCode");
//        taskid = intent.getStringExtra("taskid");
        custom_capture_tv_jxsname.setText("当前任务经销商名称：" + dealersname);
        custom_capture_rwdate.setText("当前任务创建时间：" + outdate + "点");

        ManualEdittext();

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

//            if (NetUtils.isConnected(this)) {
//                mConnect(resultString);
//            } else {
            SaveSqlite(resultString);
//            }
//            mConnect(resultString);
            mHandler.sendEmptyMessageDelayed(0, 3000);  //避免过快触发影响体验
            sm_result_tv.setText(resultString);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(0);
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * 去重并存入本地数据库
     */
    private void SaveSqlite(String rel) {
        try {
            //判断表中是否存在相同数据
//                        cursor = sqldb.rawQuery
//                                ("select * from out_storage where UserId=? and DealersName=? and Barcode=? and Date =?",
//                                        new String[]{SharedPrefsUtil.getValue(CustomCaptureActivity.this, "userInfo", "userid", ""),
//                                                dealersname, resultString, outdate});
            cursor = sqldb.rawQuery
                    ("select * from out_storage where UserId=? and UserType=? and DealersId=? and Barcode=? and Date=?", new String[]{
                            PortIpAddress.getUserId(this), PortIpAddress.getUserType(this), dealersid, rel, outdate});

            if (cursor.getCount() > 0) {
                ShowToast.showShort(this, "本地已存在此条信息");
            } else {
//                mHandler.sendEmptyMessage(1);
                try {
                    sqldb.execSQL("insert into out_storage(UserId,UserType,DealersId,DealersName,Date,Barcode) values(?,?,?,?,?,?)", new String[]{
                            PortIpAddress.getUserId(this), PortIpAddress.getUserType(this)
                            , dealersid, dealersname, outdate, rel
                    });
                    sm_num++;
                    sm_num_tv.setText(String.valueOf(sm_num));
                    ShowToast.showShort(OffLineCkCustomCaptureActivity.this, "存入本地数据库成功");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

                    break;
                case 2:
//                    UpLoadBarcode();
                    break;
                default:
                    break;
            }
        }
    };


//    /**
//     * 提交编码
//     *
//     * @param result
//     */
//    private void mConnect(String result) {
//        OkGo.<String>get(postUrl)
//                .tag(this)
//                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
//                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
//                .params(keyCode + ".taskid", taskid)
//                .params(keyCode + ".maxcode", result)
//                .execute(new StringCallback() {
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(response.body().toString());
//                            String err = jsonObject.getString(MESSAGE);
//                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
//                                sm_num++;
//                                sm_num_tv.setText(String.valueOf(sm_num));
//                                OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
//                                bean.setBm(jsonObject.getString(keyCode + ".maxcode"));
//                                mDatas.add(bean);
//                            } else {
//                                ShowToast.showShort(OffLineCkCustomCaptureActivity.this, err);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                        ShowToast.showShort(OffLineCkCustomCaptureActivity.this, R.string.connect_err);
//                    }
//                });
//    }


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

    /**
     * 监听输入框
     */
    private void ManualEdittext() {
        manual_input_etv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NoSpace(manual_input_etv, s, start);
                if (manual_input_etv.getText().toString().length() > 0) {
                    manual_input_btn.setEnabled(true);
                    manual_etv_clear.setVisibility(View.VISIBLE);
                } else {
                    manual_input_btn.setEnabled(false);
                    manual_etv_clear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 手动输入提交
     */
    @OnClick(R.id.manual_input_btn)
    void ManualInput() {
        SaveSqlite(manual_input_etv.getText().toString());
    }

//    @OnClick(R.id.open_shrink)
//    void OpenShrink() {
//        if (isOpen) {
//            SettingEditext(edit_rl, 0);
//        } else {
//            SettingEditext(edit_rl, 100);
//        }
//
//    }


//    private void SettingEditext(final View view, int toLength) {
//        // 步骤1：设置属性数值的初始值 & 结束值
//        ValueAnimator valueAnimator = ValueAnimator.ofInt(view.getLayoutParams().width, toLength);
//        // 步骤2：设置动画的播放各种属性
//        valueAnimator.setDuration(2000);
//        // 步骤3：将属性数值手动赋值给对象的属性:此处是将 值 赋给 按钮的宽度
//        // 设置更新监听器：即数值每次变化更新都会调用该方法
//        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animator) {
//                int currentValue = (Integer) animator.getAnimatedValue();
//                // 获得每次变化后的属性值
//                System.out.println(currentValue);
//                // 输出每次变化后的属性值进行查看
//                view.getLayoutParams().width = currentValue;
//                // 每次值变化时，将值手动赋值给对象的属性
//                // 即将每次变化后的值 赋 给按钮的宽度，这样就实现了按钮宽度属性的动态变化
//                // 步骤4：刷新视图，即重新绘制，从而实现动画效果
//                view.requestLayout();
//            }
//        });
//        // 启动动画
//        valueAnimator.start();
//
//        valueAnimator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                if (isOpen) {
//                    open_shrink.setImageResource(R.drawable.shrink);
//                    isOpen = false;
//                } else {
//                    open_shrink.setImageResource(R.drawable.open);
//                    isOpen = true;
//                }
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//    }


    /**
     * 清空手动输入栏
     */
    @OnClick(R.id.manual_etv_clear)
    void ClearEdittext() {
        manual_input_etv.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myDialog != null && myDialog.isShowing())
            myDialog.dismiss();
    }

}
