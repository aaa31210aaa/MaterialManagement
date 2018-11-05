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

import com.chad.library.adapter.base.BaseQuickAdapter;
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

import adapter.OutLibraryHistoryAdapter;
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

/**
 * 出、退库历史
 */
public class OutLibraryHistory extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
    private String tag = "";
    private String url = "";
    private String dataKey = "";
    @BindView(R.id.search_edittext)
    EditText search_edittext;
    @BindView(R.id.search_clear)
    ImageView search_clear;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    private OutLibraryHistoryAdapter adapter;
    private List<OutLibraryHistoryBean> mDatas;
    private String dealersid = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_library_history);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        dealersid = intent.getStringExtra("bean.dealersid");
        if (tag.equals("ckls")) {
            title_name.setText(R.string.ckrwlb);
            url = PortIpAddress.CkHistoryRwList();
            dataKey = "bean.outdate";
        } else if (tag.equals("tkls")) {
            title_name.setText(R.string.tkrwlb);
            url = PortIpAddress.TkHistoryRwList();
            dataKey = "bean.refunddate";
        } else {
            title_name.setText(R.string.tiaojirwlb);
            url = PortIpAddress.TiaoJiHistoryRwList();
            dataKey = "bean.taskdate";

        }
        initRv();
        mConnect(search_edittext.getText().toString());
        initRefresh();
        MonitorEditext();
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
            adapter = new OutLibraryHistoryAdapter(this, R.layout.outlibraryhistory_item, mDatas, tag);
            adapter.bindToRecyclerView(recyclerView);
            recyclerView.setAdapter(adapter);
        }
        mDatas = new ArrayList<>();
        dialog = DialogUtil.createLoadingDialog(OutLibraryHistory.this, R.string.loading);
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
                    mConnect(search_edittext.getText().toString());
                    break;
                default:
                    break;
            }
        }
    };


    private void mConnect(String searchparam) {
        OkGo.<String>get(url)
                .tag(TAG)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
                .params("bean.dealersid", dealersid)
                .params("searchparam", searchparam)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                JSONArray jsonArray = jsonObject.getJSONArray("listdata");
                                mDatas.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                                    bean.setId(jsonArray.optJSONObject(i).getString("bean.taskid"));
                                    bean.setCkrq(jsonArray.optJSONObject(i).getString(dataKey));
                                    bean.setRwbh(jsonArray.optJSONObject(i).getString("bean.taskno"));
                                    mDatas.add(bean);
                                }
                                adapter.setNewData(mDatas);

                                //如果无数据设置空布局
                                if (mDatas.size() == 0) {
                                    adapter.setEmptyView(R.layout.nodata_layout, (ViewGroup) recyclerView.getParent());
                                }

                                adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                                        OutLibraryHistoryBean bean = (OutLibraryHistoryBean) adapter.getData().get(position);
                                        Intent intent = new Intent(OutLibraryHistory.this, OutLibraryHistoryDetail.class);
                                        intent.putExtra("taskid", bean.getId());
                                        intent.putExtra("tag", tag);
                                        startActivity(intent);
                                    }
                                });
                            } else {
                                ShowToast.showShort(OutLibraryHistory.this, err);
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
                        dialog.dismiss();
                        refreshLayout.finishRefresh();
                        ShowToast.showShort(OutLibraryHistory.this, R.string.connect_err);
                    }
                });
    }

    /**
     * 监听搜索框
     */
    private void MonitorEditext() {
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
                        search_clear.setVisibility(View.VISIBLE);
//                        search(search_edittext.getText().toString().trim());
                    } else {
                        refreshLayout.setEnableRefresh(true);
                        search_clear.setVisibility(View.GONE);
                    }
                    mConnect(search_edittext.getText().toString());
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


    /**
     * 清除搜索框内容
     */
    @OnClick(R.id.search_clear)
    public void ClearSearch() {
        search_edittext.setText("");
        search_clear.setVisibility(View.GONE);
    }

}
