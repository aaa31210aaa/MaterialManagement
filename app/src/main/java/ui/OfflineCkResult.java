package ui;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.materialmanagement.R;

import java.util.ArrayList;
import java.util.List;

import adapter.OutLibraryResultAdapter;
import bean.OutLibraryHistoryBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;
import utile.BaseActivity;
import utile.DividerItemDecoration;
import utile.PermissionSettingPage;
import utile.PortIpAddress;

import static com.example.administrator.materialmanagement.MyApplication.sqldb;

public class OfflineCkResult extends BaseActivity implements EasyPermissions.PermissionCallbacks {
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
    //    private String taskid = "";
    private String dealersid = "";
    private String outdate = "";
    private String dealersname = "";
    private String errStr = "连接失败，请检查网络";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_ck_result);
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

//        taskid = intent.getStringExtra("taskid");

        title_name.setText("离线出库任务结果");
        url = PortIpAddress.OutLibraryStepTwo();
        postUrl = PortIpAddress.OutLibraryResult();
        keyCode = "outdatabean";
        dataKey = "bean.outdate";

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
        OfflineCkStepTwo.offlineStepTwo.finish();
    }

    //展示已保存的条码
    private void ShowBm() {
//        if (NetUtils.isConnected(this)) {
//            mConnect();
//        } else {

        if (mDatas.size() > 0)
            mDatas.clear();
        Cursor cursor = sqldb.rawQuery("select Barcode from out_storage where UserId = ? and DealersId =? and DealersName =? and Date =?",
                new String[]{PortIpAddress.getUserId(this), dealersid, dealersname, outdate});
        while (cursor.moveToNext()) {
            OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
            String barcode = cursor.getString(cursor.getColumnIndex("Barcode"));
            bean.setBm(barcode);
            mDatas.add(bean);
        }
        if (mDatas.size() > 0) {
            adapter.setNewData(mDatas);
        } else {
            adapter.setEmptyView(R.layout.nodata_layout, (ViewGroup) recyclerView.getParent());
        }

    }

//        }
//    }


    /**
     * 检测拍摄权限
     */
//    @AfterPermissionGranted(RC_CAMERA)
    private void checkCameraPermissions() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            Log.e(TAG, "判断有权限");
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
        intent.putExtra("dealersid", dealersid);
        intent.putExtra("dealersname", dealersname);
        intent.putExtra("outdate", outdate);
        intent.putExtra("postUrl", postUrl);
        intent.putExtra("keyCode", keyCode);
//        intent.putExtra("taskid", taskid);
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_SCAN, optionsCompat.toBundle());
    }

    /**
     * 扫码
     *
     * @param v
     */
    @OnClick(R.id.out_fbt)
    void OutFbt(View v) {
//        this.cls = CaptureActivity.class;
        this.cls = OffLineCkCustomCaptureActivity.class;
        this.title = ((Button) v).getText().toString();
        checkCameraPermissions();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart");
        ShowBm();
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
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.e(TAG, "权限允许后回调");
        startScan(cls, title);
    }

    /**
     * 请求权限失败。
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            PermissionSettingPage.GoToPermissionSetting(this);
        }
    }
}
