package ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.materialmanagement.R;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapter.OffLineBarcodeAdapter;
import bean.OutLibraryHistoryBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utile.BaseActivity;
import utile.DialogUtil;
import utile.DividerItemDecoration;
import utile.NetUtils;
import utile.PortIpAddress;
import utile.SharedPrefsUtil;
import utile.ShowToast;

import static com.example.administrator.materialmanagement.MyApplication.sqldb;
import static utile.PortIpAddress.CODE;
import static utile.PortIpAddress.MESSAGE;
import static utile.PortIpAddress.SUCCESS_CODE;

public class OffLineBarcode extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.title_name_right)
    ImageView title_name_right;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    private OffLineBarcodeAdapter adapter;
    private List<OutLibraryHistoryBean> mDatas;

    private String userid = "";
    private String usertype = "";
    private String dealersid = "";
    private String dealersName = "";
    private String date = "";
    private String taskid = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_line_barcode);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        userid = intent.getStringExtra("userid");
        usertype = intent.getStringExtra("usertype");
        dealersid = intent.getStringExtra("dealersid");
        dealersName = intent.getStringExtra("dealersName");
        date = intent.getStringExtra("date");
        title_name.setText("离线出库大码");
        title_name_right.setImageResource(R.drawable.upload);
        initRv();
        mConnect();
        initRefresh();
    }

    @OnClick(R.id.back)
    void Back() {
        finish();
    }

    private void initRv() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        if (adapter == null) {
            adapter = new OffLineBarcodeAdapter(R.layout.offlinebarcode_item, mDatas);
            adapter.bindToRecyclerView(recyclerView);
            recyclerView.setAdapter(adapter);
        }
        mDatas = new ArrayList<>();
        dialog = DialogUtil.createLoadingDialog(OffLineBarcode.this, R.string.loading);
    }

    private void mConnect() {
        mDatas.clear();
        Cursor cursor = sqldb.rawQuery("select Barcode from out_storage where UserId=? and UserType =? and DealersName =? and Date=?",
                new String[]{userid, usertype, dealersName, date});
        while (cursor.moveToNext()) {
            OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
            String barcode = cursor.getString(cursor.getColumnIndex("Barcode"));
            bean.setBm(barcode);
            mDatas.add(bean);
        }

        adapter.setNewData(mDatas);

        //如果无数据设置空布局
        if (mDatas.size() == 0) {
            adapter.setEmptyView(R.layout.nodata_layout, (ViewGroup) recyclerView.getParent());
        }

        dialog.dismiss();
    }


    private void initRefresh() {
        //刷新
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                handler.sendEmptyMessageDelayed(1, ShowToast.refreshTime);
            }
        });

        //加载
        refreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                handler.sendEmptyMessageDelayed(0, ShowToast.refreshTime);
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0:
                    refreshLayout.finishLoadmore();
                    break;
                case 1:
                    mConnect();
                    refreshLayout.finishRefresh();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 提交本地存放的大码
     */
    @OnClick(R.id.title_name_right)
    void UploadBarcode() {
        if (NetUtils.isConnected(this)) {
            NewTask();
        } else {
            ShowToast.showShort(this, R.string.connect_err);
        }
    }


    /**
     * 通过当前任务的经销商id 和时间 先创建当前的进度任务
     */
    private void NewTask() {
        dialog = DialogUtil.createLoadingDialog(OffLineBarcode.this, R.string.newTask);
        OkGo.<String>get(PortIpAddress.OutLibraryStepTwo())
                .tag(TAG)
                .params("loginuserid", userid)
                .params("loginusertype", usertype)
                .params("bean.dealersid", dealersid)
                .params("bean.outdate", date)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String jsonStr = response.body().toString();
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                taskid = jsonObject.getString("taskid");
//                                ShowToast.showShort(OffLineBarcode.this, "创建任务成功");
                                mHandler.sendEmptyMessage(0);
                            } else {
                                ShowToast.showLong(OffLineBarcode.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ShowToast.showShort(OffLineBarcode.this, R.string.connect_err);
                        dialog.dismiss();
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                    }
                });
    }

    Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0:
                    UpLoadBarcode();
                    break;
            }
        }
    };


    /**
     * 提交本地的码值
     */
    private void UpLoadBarcode() {
        Gson gson = new Gson();
        String barcodeJson = gson.toJson(mDatas);
        OkGo.<String>get(PortIpAddress.OffLineCk())
                .tag(TAG)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
                .params("outdatabean.taskid", taskid)
                .params("outdatabean.maxcode", barcodeJson)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String jsonStr = response.body().toString();
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            String err = jsonObject.getString(MESSAGE);

                            //提交完本地数据成功之后  将当前任务数据清空
                            sqldb.execSQL("delete from out_storage where UserId = ? and DealersId =? and DealersName =? and Date =?", new String[]{
                                    PortIpAddress.getUserId(OffLineBarcode.this), dealersid, dealersName, date
                            });
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                ShowToast.showShort(OffLineBarcode.this, err);
                            } else {
                                ShowToast.showShort(OffLineBarcode.this, err);
                            }

                            setResult(RESULT_OK);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ShowToast.showShort(OffLineBarcode.this, R.string.connect_err);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dialog.dismiss();
                    }
                });
    }

}
