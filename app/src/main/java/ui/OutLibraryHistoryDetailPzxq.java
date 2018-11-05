package ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.materialmanagement.R;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapter.BmListAdapter;
import bean.OutLibraryHistoryBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utile.BaseActivity;
import utile.DialogUtil;
import utile.DividerItemDecoration;
import utile.PortIpAddress;
import utile.SharedPrefsUtil;
import utile.ShowToast;

import static utile.PortIpAddress.CODE;
import static utile.PortIpAddress.MESSAGE;
import static utile.PortIpAddress.SUCCESS_CODE;

public class OutLibraryHistoryDetailPzxq extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.search_edittext)
    EditText search_edittext;
    @BindView(R.id.search_clear)
    ImageView search_clear;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    private BmListAdapter adapter;
    private List<OutLibraryHistoryBean> mDatas;
    private List<OutLibraryHistoryBean> searchDatas;
    private String taskid = "";
    private String productid = "";
    private String tag = "";
    private String url = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_library_history_detail_pzxq);
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
        taskid = intent.getStringExtra("taskid");
        productid = intent.getStringExtra("productid");
        title_name.setText("大码清单");
        tag = intent.getStringExtra("tag");
        if (tag.equals("ckls")) {
            url = PortIpAddress.OutLibraryDetailBmList();
        } else if (tag.equals("tkls")) {
            url = PortIpAddress.TkLibraryDetailBmList();
        } else {
            url = PortIpAddress.TiaoJiLibraryDetailBmList();
        }

        initRv();
        mConnect();
        initRefresh();
        MonitorEditext();
    }

    private void initRv() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        if (adapter == null) {
            adapter = new BmListAdapter(R.layout.jxs_item, mDatas);
            adapter.bindToRecyclerView(recyclerView);
            recyclerView.setAdapter(adapter);
        }
        mDatas = new ArrayList<>();
        dialog = DialogUtil.createLoadingDialog(OutLibraryHistoryDetailPzxq.this, R.string.loading);
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
                    break;
                default:
                    break;
            }
        }
    };


    private void mConnect() {
        OkGo.<String>get(url)
                .tag(TAG)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
                .params(PortIpAddress.Bean + "taskid", taskid)
                .params(PortIpAddress.Bean + "productid", productid)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                mDatas.clear();
                                JSONArray jsonArray = jsonObject.getJSONArray("listdata");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                                    bean.setBm(jsonArray.optJSONObject(i).getString("maxcode"));
                                    mDatas.add(bean);
                                }

                                adapter.setNewData(mDatas);
                                //如果无数据设置空布局
                                if (mDatas.size() == 0) {
                                    adapter.setEmptyView(R.layout.nodata_layout, (ViewGroup) recyclerView.getParent());
                                }

                            } else {
                                ShowToast.showShort(OutLibraryHistoryDetailPzxq.this, err);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                        refreshLayout.finishRefresh();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);

                    }
                });
//        for (int i = 0; i < 10; i++) {
//            OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
//            bean.setJxs("20180901" + i);
//            mDatas.add(bean);
//        }
//        adapter.setNewData(mDatas);
    }


    /**
     * 监听搜索框
     */
    private void MonitorEditext() {
        searchDatas = new ArrayList<>();
        search_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e(TAG, count + "----");
                if (mDatas != null) {
                    if (search_edittext.length() > 0) {
                        refreshLayout.setEnableRefresh(false);
                        refreshLayout.setEnableLoadmore(false);
                        search_clear.setVisibility(View.VISIBLE);
                        search(search_edittext.getText().toString().trim());
                    } else {
                        refreshLayout.setEnableRefresh(true);
                        refreshLayout.setEnableLoadmore(true);
                        search_clear.setVisibility(View.GONE);
                        if (adapter != null) {
                            adapter.setNewData(mDatas);
                        }

                    }
                } else {
                    if (search_edittext.length() > 0) {
                        search_clear.setVisibility(View.VISIBLE);
                    } else {
                        search_clear.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //搜索框
    private void search(String str) {
        if (mDatas != null) {
            searchDatas.clear();
            for (OutLibraryHistoryBean entity : mDatas) {
                try {
                    if (entity.getBm().contains(str)) {
                        searchDatas.add(entity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adapter.setNewData(searchDatas);
            }
        }
    }


    /**
     * 清除搜索框内容
     */
    @OnClick(R.id.search_clear)
    public void ClearSearch() {
        search_edittext.setText("");
        search_clear.setVisibility(View.GONE);
    }
}
