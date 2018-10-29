package ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.materialmanagement.R;
import com.king.zxing.Intents;
import com.king.zxing.util.CodeUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapter.OutLibraryResultAdapter;
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
import utile.UriUtils;

import static utile.PortIpAddress.CODE;
import static utile.PortIpAddress.MESSAGE;
import static utile.PortIpAddress.SUCCESS_CODE;

public class OutLibraryResult extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_IS_QR_CODE = "key_code";

    public static final int REQUEST_CODE_SCAN = 0X01;
    public static final int REQUEST_CODE_PHOTO = 0X02;

    public static final int RC_CAMERA = 0X01;
    public static final int RC_READ_PHOTO = 0X02;

    private Class<?> cls;
    private String title;
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.title_name_right)
    TextView title_name_right;
    private String tag = "";
    //展示条码的url
    private String url = "";
    //提交条码的url
    private String postUrl = "";
    private String keyCode = "";
    private String dataKey = "";

    //    @BindView(R.id.result_ll)
//    LinearLayout result_ll;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private OutLibraryResultAdapter adapter;
    private List<OutLibraryHistoryBean> mDatas;
    public static final String titleCode = "http://www.lcseed.com.cn/fangwei/ifindscode.action?scode=";
    private String taskid = "";
    private String dealersid = "";
    private String outdate = "";
    private String dealersname = "";
    private String errStr = "连接失败，请检查网络";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_library_result);
        ButterKnife.bind(this);
        initData();
    }

    @OnClick(R.id.back)
    void Back() {
        finish();
    }


    @Override
    protected void initData() {
        Intent intent = getIntent();
        dealersid = intent.getStringExtra("bean.dealersid");
        dealersname = intent.getStringExtra("dealersname");
        outdate = intent.getStringExtra("outdate");
        tag = intent.getStringExtra("tag");
        taskid = intent.getStringExtra("taskid");

        if (tag.equals("ck")) {
            title_name.setText("出库任务结果");
            url = PortIpAddress.OutLibraryStepTwo();
            postUrl = PortIpAddress.OutLibraryResult();
            keyCode = "outdatabean";
            dataKey = "bean.outdate";
        } else if (tag.equals("tk")) {
            title_name.setText("退库任务结果");
            url = PortIpAddress.TkStepTwo();
            postUrl = PortIpAddress.TkLibraryResult();
            keyCode = "taskdatabean";
            dataKey = "bean.refunddate";
        } else {
            title_name.setText("调剂任务结果");
            url = PortIpAddress.TiaoJiLibraryStepTwo();
            postUrl = PortIpAddress.TiaoJiLibraryResult();
            keyCode = "taskdatabean";
            dataKey = "bean.taskdate";
        }


        title_name_right.setText(R.string.submit);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        if (adapter == null) {
            adapter = new OutLibraryResultAdapter(R.layout.jxs_item, mDatas);
            adapter.bindToRecyclerView(recyclerView);
            recyclerView.setAdapter(adapter);
        }
        mDatas = new ArrayList<>();
        ShowBm();
    }


    @OnClick(R.id.title_name_right)
    void Submit() {
        finish();
        OutLibraryStepTwo.instanceStepTwo.finish();
    }


    //展示已保存的条码
    private void ShowBm() {
        dialog = DialogUtil.createLoadingDialog(OutLibraryResult.this, R.string.loading_write);
        OkGo.<String>get(url)
                .tag(this)
                .cacheKey("outlibrary_result")
                .cacheMode(CacheMode.FIRST_CACHE_THEN_REQUEST)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
                .params(PortIpAddress.Bean + "dealersid", dealersid)
                .params(dataKey, outdate)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            String err = jsonObject.getString(MESSAGE);

                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                mDatas.clear();
                                JSONArray jsonArray = jsonObject.getJSONArray("maxcodelist");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                                    bean.setBm(jsonArray.optJSONObject(i).getString(keyCode + ".maxcode"));
                                    mDatas.add(bean);
                                }
                                adapter.setNewData(mDatas);
                            } else {
                                ShowToast.showShort(OutLibraryResult.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        errStr = "网络连接失败，请重试";
                    }

                    @Override
                    public void onCacheSuccess(Response<String> response) {
                        super.onCacheSuccess(response);
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            String err = jsonObject.getString(MESSAGE);

                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                mDatas.clear();
                                JSONArray jsonArray = jsonObject.getJSONArray("maxcodelist");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                                    bean.setBm(jsonArray.optJSONObject(i).getString(keyCode + ".maxcode"));
                                    mDatas.add(bean);
                                }
                                adapter.setNewData(mDatas);
                            } else {
                                ShowToast.showShort(OutLibraryResult.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        errStr = "网络连接失败，加载缓存数据";
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ShowToast.showShort(OutLibraryResult.this, errStr);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dialog.dismiss();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_CODE_SCAN:
                    String result = data.getStringExtra(Intents.Scan.RESULT);
//                    ArrayList<String> mDatas = new ArrayList<>();
//                    mDatas = data.getStringArrayListExtra(Intents.Scan.RESULT);

                    result = result.replace(titleCode, "MIN");
//                    mConnect(result);
                    break;
                case REQUEST_CODE_PHOTO:
                    parsePhoto(data);
                    break;
            }
        }
    }

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
                                OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                                bean.setBm(jsonObject.getString(keyCode + ".maxcode"));
                                mDatas.add(bean);
                                adapter.setNewData(mDatas);
                            } else {
                                ShowToast.showShort(OutLibraryResult.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ShowToast.showShort(OutLibraryResult.this, R.string.connect_err);
                    }
                });
    }


    private void parsePhoto(Intent data) {
        final String path = UriUtils.INSTANCE.getImagePath(this, data);
        Log.d("Jenly", "path:" + path);
        if (TextUtils.isEmpty(path)) {
            return;
        }

        //异步解析
        asyncThread(new Runnable() {
            @Override
            public void run() {
                final String result = CodeUtils.parseCode(path);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Jenly", "result:" + result);
                        Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private Context getContext() {
        return this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 请求权限成功。
     *
     * @param requestCode
     * @param list
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted

    }

    /**
     * 请求权限失败。
     *
     * @param requestCode
     * @param list
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
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

    private void asyncThread(Runnable runnable) {
        new Thread(runnable).start();
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
        intent.putExtra("tag", tag);
        intent.putExtra("dealersid", dealersid);
        intent.putExtra("dealersname", dealersname);
        intent.putExtra("postUrl", postUrl);
        intent.putExtra("keyCode", keyCode);
        intent.putExtra("taskid", taskid);
        intent.putExtra("outdate", outdate);
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_SCAN, optionsCompat.toBundle());
    }


    private void startPhotoCode() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(pickIntent, REQUEST_CODE_PHOTO);
    }


    @AfterPermissionGranted(RC_READ_PHOTO)
    private void checkExternalStoragePermissions() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            startPhotoCode();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_external_storage),
                    RC_READ_PHOTO, perms);
        }
    }

    /**
     * 扫码
     *
     * @param v
     */
    @OnClick(R.id.out_fbt)
    void OutFbt(View v) {
//        this.cls = CaptureActivity.class;
        this.cls = CustomCaptureActivity.class;
        this.title = ((Button) v).getText().toString();
        checkCameraPermissions();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart");
        ShowBm();
    }
}
