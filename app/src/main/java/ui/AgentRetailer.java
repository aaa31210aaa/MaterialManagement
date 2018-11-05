package ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import adapter.AgentRetailerAdapter;
import bean.AgentRetailerBean;
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

public class AgentRetailer extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.title_name_right)
    ImageView title_name_right;
    private String tag = "";
    @BindView(R.id.search_edittext)
    EditText search_edittext;
    @BindView(R.id.search_clear)
    ImageView search_clear;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    private AgentRetailerAdapter adapter;
    private List<AgentRetailerBean> mDatas;
    private List<AgentRetailerBean> searchDatas;


    private static int ADD_AGENTRETAILER_CODE = 20;
    private static int MODIFY_AGENTRETAILER_CODE = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_retailer);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
//        if (SharedPrefsUtil.getValue(this, "userType", "code", "").equals(AGENT_CODE)) {
//            title_name.setText("我的零售商");
//        } else {
//            title_name.setText("我的代理商");
//        }
        title_name.setText("我的经销商");
        title_name_right.setBackgroundResource(R.drawable.ic_action_add);
        initRv();
        mConnect(search_edittext.getText().toString());
        initRefresh();
        MonitorEditext();
    }


    @OnClick(R.id.back)
    void Back() {
        finish();
    }

    @OnClick(R.id.title_name_right)
    void Add() {
        tag = "add";
        Intent intent = new Intent(this, AgentRetailerDetail.class);
        intent.putExtra("tag", tag);
        startActivityForResult(intent, ADD_AGENTRETAILER_CODE);
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


    private void initRv() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        if (adapter == null) {
            adapter = new AgentRetailerAdapter(R.layout.agent_retailer_item, mDatas);
            adapter.bindToRecyclerView(recyclerView);
//            adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
//            adapter.isFirstOnly(true);
            recyclerView.setAdapter(adapter);
        }
        mDatas = new ArrayList<>();
        dialog = DialogUtil.createLoadingDialog(AgentRetailer.this, R.string.loading);
    }

    private void mConnect(String searchparam) {
        OkGo.<String>get(PortIpAddress.DlsJxsList())
                .tag(TAG)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
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
                                    AgentRetailerBean bean = new AgentRetailerBean();
                                    bean.setId(jsonArray.optJSONObject(i).getString("bean.dealersid"));
                                    bean.setDlsbh(jsonArray.optJSONObject(i).getString("bean.dealersno"));
                                    bean.setDlsmc(jsonArray.optJSONObject(i).getString("bean.dealersname"));
                                    bean.setDlslxdh(jsonArray.optJSONObject(i).getString("bean.phone"));
                                    mDatas.add(bean);
                                }
                                adapter.setNewData(mDatas);

                                //如果无数据设置空布局
                                if (mDatas.size() == 0) {
                                    adapter.setEmptyView(R.layout.nodata_layout, (ViewGroup) recyclerView.getParent());
                                }

                                //列表子项点击监听
                                adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                                        tag = "modify";
                                        AgentRetailerBean bean = (AgentRetailerBean) adapter.getData().get(position);
                                        Intent intent = new Intent(AgentRetailer.this, AgentRetailerDetail.class);
                                        intent.putExtra("bean.dealersid", bean.getId());
                                        intent.putExtra("tag", tag);
                                        startActivityForResult(intent, MODIFY_AGENTRETAILER_CODE);
                                    }
                                });


                                //长按点击删除一条
                                adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
                                    @Override
                                    public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
//                                        ShowToast.showShort(AgentRetailer.this,"长按"+position);
                                        AgentRetailerBean bean = (AgentRetailerBean) adapter.getData().get(position);
                                        Delete(bean.getId());
                                        return false;
                                    }
                                });

                            } else {
                                ShowToast.showShort(AgentRetailer.this, err);
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
                        ShowToast.showShort(AgentRetailer.this, R.string.connect_err);
                    }
                });
    }



    /**
     * 删除一条信息
     */
    private void Delete(final String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AgentRetailer.this);
        builder.setTitle(R.string.Prompt);
        builder.setMessage(R.string.DeleteTemporaryData);
        builder.setPositiveButton(R.string.mine_cancellation_dialog_btn2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OkGo.<String>get(PortIpAddress.DeleteOne())
                        .tag(TAG)
                        .params("loginuserid", SharedPrefsUtil.getValue(AgentRetailer.this, "userInfo", "userid", ""))
                        .params(PortIpAddress.Bean + "dealersid", id)
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().toString());
                                    String err = jsonObject.getString(MESSAGE);
                                    if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                        mConnect(search_edittext.getText().toString());
                                    } else {
                                        ShowToast.showShort(AgentRetailer.this, err);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }

        });
        builder.setNegativeButton(R.string.mine_cancellation_dialog_btn1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            dialog.show();
            mConnect(search_edittext.getText().toString());
        }
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
//                        if (adapter != null) {
//                            adapter.setNewData(mDatas);
//                        }
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


    //搜索框 本地过滤
    private void search(String str) {
        if (mDatas != null) {
            searchDatas = new ArrayList<AgentRetailerBean>();
            for (AgentRetailerBean entity : mDatas) {
                try {
                    if (entity.getDlsbh().contains(str) || entity.getDlsmc().contains(str) || entity.getDlslxdh().contains(str)) {
                        searchDatas.add(entity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adapter.setNewData(searchDatas);
            }
        }
    }
}
