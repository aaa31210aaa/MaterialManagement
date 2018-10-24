package ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.administrator.materialmanagement.PreventFleeAdapter;
import com.example.administrator.materialmanagement.R;
import com.king.zxing.CaptureActivity;
import com.king.zxing.Intents;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bean.OutLibraryHistoryBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import utile.BaseActivity;
import utile.DialogUtil;
import utile.DividerItemDecoration;
import utile.PortIpAddress;
import utile.SharedPrefsUtil;
import utile.ShowToast;

import static ui.OutLibraryResult.KEY_TITLE;
import static ui.OutLibraryResult.RC_CAMERA;
import static ui.OutLibraryResult.REQUEST_CODE_SCAN;
import static ui.OutLibraryResult.titleCode;
import static utile.PortIpAddress.CODE;
import static utile.PortIpAddress.MESSAGE;
import static utile.PortIpAddress.SUCCESS_CODE;

/**
 * 防窜
 */
public class PreventFlee extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
//    @BindView(R.id.title_name_right)
//    TextView title_name_right;
    @BindView(R.id.fccx)
    TextView fccx;
    @BindView(R.id.code_etv)
    EditText code_etv;
    @BindView(R.id.code_scavenging)
    Button code_scavenging;

    private Class<?> cls;
    private String title;
    public static final int REQUEST_CODE_SCAN1 = 0X011;
    public static final int REQUEST_CODE_SCAN2 = 0X012;

//    @BindView(R.id.cpmc_tv)
//    TextView cpmc_tv;
//    @BindView(R.id.cpbh_tv)
//    TextView cpbh_tv;
//    @BindView(R.id.bzgg_tv)
//    TextView bzgg_tv;
//    @BindView(R.id.scrq_tv)
//    TextView scrq_tv;
//    @BindView(R.id.cksj_tv)
//    TextView cksj_tv;
//    @BindView(R.id.khmc_tv)
//    TextView khmc_tv;

    private TextView cpmc_tv;
    private TextView cpbh_tv;
    private TextView maxcode_tv;
    private TextView scrq_tv;
    private TextView cksj_tv;
    private TextView khmc_tv;


    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private PreventFleeAdapter adapter;
    private List<OutLibraryHistoryBean> mDatas;
    private View headView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prevent_flee);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
        title_name.setText("防窜查询");
//        title_name_right.setText("查询");
        initRv();
    }


    @OnClick(R.id.back)
    void Back() {
        finish();
    }

    private void initRv() {
        headView = View.inflate(this, R.layout.cx_result_head, null);
        cpmc_tv = headView.findViewById(R.id.cpmc_tv);
        cpbh_tv = headView.findViewById(R.id.cpbh_tv);
        maxcode_tv = headView.findViewById(R.id.maxcode_tv);
        scrq_tv = headView.findViewById(R.id.scrq_tv);
        cksj_tv = headView.findViewById(R.id.cksj_tv);
        khmc_tv = headView.findViewById(R.id.khmc_tv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        if (adapter == null) {
            adapter = new PreventFleeAdapter(R.layout.jxs_item, mDatas);
            adapter.bindToRecyclerView(recyclerView);
            adapter.addHeaderView(headView);
            recyclerView.setAdapter(adapter);
        }
        mDatas = new ArrayList<>();
    }

    private void mConnect() {
        OkGo.<String>get(PortIpAddress.FangCuan())
                .tag(this)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
                .params("mincode", code_etv.getText().toString())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                mDatas.clear();
                                JSONArray jsonArray = jsonObject.getJSONArray("listdata");
                                cpmc_tv.setText(jsonObject.getString("productname"));
                                cpbh_tv.setText(jsonObject.getString("productno"));
                                maxcode_tv.setText(jsonObject.getString("maxcode"));
                                scrq_tv.setText(jsonObject.getString("createtime"));
                                cksj_tv.setText(jsonObject.getString("modifytime"));
                                khmc_tv.setText(jsonObject.getString("dealersname"));
//
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                                    bean.setBm(jsonArray.optJSONObject(i).getString("mincode"));
                                    mDatas.add(bean);
                                }
                                adapter.setNewData(mDatas);
                            } else {
                                ShowToast.showShort(PreventFlee.this, err);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        dialog.dismiss();
                        ShowToast.showShort(PreventFlee.this, R.string.connect_err);
                    }
                });


//        for (int i = 0; i < 10; i++) {
//            OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
//            bean.setJxs("201809010" + i);
//            mDatas.add(bean);
//        }
    }

    @OnClick(R.id.fccx)
    void Search() {
        if (code_etv.getText().toString().trim().equals("")) {
            ShowToast.showShort(this, "请填写大小码进行查询");
        } else {
            dialog = DialogUtil.createLoadingDialog(PreventFlee.this, R.string.loading);
            mConnect();
        }
    }

    /**
     * 扫描
     */
    @OnClick(R.id.code_scavenging)
    void XmScavenging(View v) {
        this.cls = CaptureActivity.class;
        this.title = ((Button) v).getText().toString();
        checkCameraPermissions();
    }

    /**
     * 检测拍摄权限
     */
    @AfterPermissionGranted(RC_CAMERA)
    private void checkCameraPermissions() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            startScan(cls, title);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.permission_camera),
                    RC_CAMERA, perms);
        }
    }

    /**
     * 扫码
     *
     * @param cls
     * @param title
     */
    private void startScan(Class<?> cls, String title) {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.in, R.anim.out);
        Intent intent = new Intent(this, cls);
        intent.putExtra(KEY_TITLE, title);
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_SCAN, optionsCompat.toBundle());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_CODE_SCAN:
                    //大码
                    String result = data.getStringExtra(Intents.Scan.RESULT);
                    result = result.replace(titleCode, "MIN");
                    code_etv.setText(result);
                    break;
            }
        }
    }
}
