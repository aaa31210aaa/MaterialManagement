package ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.example.administrator.materialmanagement.R;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utile.BaseActivity;
import utile.DateUtils;
import utile.DialogUtil;
import utile.PortIpAddress;
import utile.SharedPrefsUtil;
import utile.ShowToast;

import static com.example.administrator.materialmanagement.Login.ONE_CODE;
import static utile.PortIpAddress.CODE;
import static utile.PortIpAddress.MESSAGE;
import static utile.PortIpAddress.SUCCESS_CODE;

public class OutLibraryStepTwo extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.title_name_right)
    TextView title_name_right;
    @BindView(R.id.rwbh_tv)
    TextView rwbh_tv;
    @BindView(R.id.cktkrq_tv)
    TextView cktkrq_tv;
    @BindView(R.id.dls_lss)
    TextView dls_lss;
    @BindView(R.id.dls_lss_tv)
    TextView dls_lss_tv;
    @BindView(R.id.ckrq_tv)
    TextView ckrq_tv;
    private String tag = "";
    private String url = "";
    private String dataKey = "";

    private int CPCHOOSE_CODE = 10;
    private String cp_name = "";
    public static Activity instanceStepTwo;

    private String dealersid = "";
    private String dlslss = "";
    private String cksj = "";
    private TimePickerView pvTime;
    private Intent intent_data;
    private String errStr = "连接失败，请检查网络";
    private boolean isTrue = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_library_step_two);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
        instanceStepTwo = this;
        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        title_name_right.setText(R.string.xyb);

        if (SharedPrefsUtil.getValue(this, "userInfo", "usertype", "").equals(ONE_CODE)) {
            dls_lss.setText("代理商");
        } else {
            dls_lss.setText("零售商");
        }

        dealersid = intent.getStringExtra("bean.dealersid");
        dlslss = intent.getStringExtra("bean.dealersname");
        dls_lss_tv.setText(dlslss);
        ckrq_tv.setText(DateUtils.dateToStrHour());
        initTimePicker();

        if (tag.equals("ck")) {
            title_name.setText(R.string.ckrw);
            cktkrq_tv.setText(R.string.ckrq);
            url = PortIpAddress.OutLibraryStepTwo();
            dataKey = "bean.outdate";
        } else if (tag.equals("tk")) {
            title_name.setText(R.string.tkrw);
            cktkrq_tv.setText(R.string.tkrq);
            url = PortIpAddress.TkStepTwo();
            dataKey = "bean.refunddate";
        } else {
            title_name.setText(R.string.tiaojirw);
            cktkrq_tv.setText(R.string.tiaojidate);
            url = PortIpAddress.TiaoJiLibraryStepTwo();
            dataKey = "bean.taskdate";
        }
    }

    @OnClick(R.id.back)
    void Back() {
        finish();
    }

    /**
     * 下一步
     */
    @OnClick(R.id.title_name_right)
    void NextStep() {

//        Intent intent = new Intent(OutLibraryStepTwo.this, OutLibraryResult.class);
//        intent.putExtra("tag", tag);
//        startActivity(intent);
        mConnect();
    }


    @OnClick(R.id.ckrq_tv)
    void Date() {
        if (pvTime != null) {
            pvTime.show();
        }
    }

    private void initTimePicker() {//Dialog 模式下，在底部弹出

        pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
//                Toast.makeText(OutLibraryStepTwo.this, getTime(date), Toast.LENGTH_SHORT).show();
                ckrq_tv.setText(getTime(date));
            }
        })
                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                    @Override
                    public void onTimeSelectChanged(Date date) {
                        Log.i("pvTime", "onTimeSelectChanged");
                    }
                })
                .setType(new boolean[]{true, true, true, true, false, false})   //展示几个选项
                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .build();

        Dialog mDialog = pvTime.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTime.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
            }
        }
    }


    private String getTime(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
        return format.format(date);
    }


    private void mConnect() {
        dialog = DialogUtil.createLoadingDialog(OutLibraryStepTwo.this, R.string.loading);
        OkGo.<String>get(url)
                .tag(this)
                .cacheKey("outlibrary_step_two")
                .cacheMode(CacheMode.FIRST_CACHE_THEN_REQUEST)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
                .params("bean.dealersid", dealersid)
                .params(dataKey, ckrq_tv.getText().toString())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String jsonStr = response.body().toString();
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                String taskid = jsonObject.getString("taskid");
                                intent_data = new Intent(OutLibraryStepTwo.this, OutLibraryResult.class);
                                intent_data.putExtra("bean.dealersid", dealersid);
                                intent_data.putExtra("dealersname", dlslss);
                                intent_data.putExtra("outdate", ckrq_tv.getText().toString());
                                intent_data.putExtra("tag", tag);
                                intent_data.putExtra("taskid", taskid);
                            } else {
                                ShowToast.showShort(OutLibraryStepTwo.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        errStr = "网络连接失败，请重试";
                        isTrue = true;
                    }

                    @Override
                    public void onCacheSuccess(Response<String> response) {
                        super.onCacheSuccess(response);
                        try {
                            String jsonStr = response.body().toString();
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                String taskid = jsonObject.getString("taskid");
                                intent_data = new Intent(OutLibraryStepTwo.this, OutLibraryResult.class);
                                intent_data.putExtra("bean.dealersid", dealersid);
                                intent_data.putExtra("dealersname", dlslss);
                                intent_data.putExtra("outdate", ckrq_tv.getText().toString());
                                intent_data.putExtra("tag", tag);
                                intent_data.putExtra("taskid", taskid);
                            } else {
                                ShowToast.showShort(OutLibraryStepTwo.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        errStr = "";
                        isTrue = false;
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        if (isTrue) {
                            ShowToast.showShort(OutLibraryStepTwo.this, errStr);
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dialog.dismiss();
                        startActivity(intent_data);
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == CPCHOOSE_CODE) {
                Bundle bundle = data.getExtras();
                cp_name = bundle.getString("cp_name");
            }
        }
    }
}
