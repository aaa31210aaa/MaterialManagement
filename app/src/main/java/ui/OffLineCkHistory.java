package ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import adapter.OffLineCkAdapter;
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

import static com.example.administrator.materialmanagement.MyApplication.sqldb;

public class OffLineCkHistory extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
    private String tag = "";
    @BindView(R.id.search_edittext)
    EditText search_edittext;
    @BindView(R.id.search_clear)
    ImageView search_clear;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    private OffLineCkAdapter adapter;
    private List<OutLibraryHistoryBean> mDatas;
    private List<OutLibraryHistoryBean> searchDatas;
    private static final int CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_line_ck_history);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
        title_name.setText("离线出库任务");
        initRv();
        mConnect();
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
            adapter = new OffLineCkAdapter(R.layout.offline_item, mDatas);
            adapter.bindToRecyclerView(recyclerView);
//            adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
//            adapter.isFirstOnly(true);
            recyclerView.setAdapter(adapter);
        }
        mDatas = new ArrayList<>();
        dialog = DialogUtil.createLoadingDialog(OffLineCkHistory.this, R.string.loading);
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


    private void mConnect() {
        mDatas.clear();
        try {
            Cursor cursor = sqldb.rawQuery("select distinct UserId,UserType,DealersId,DealersName,Date from out_storage where UserId =?",
                    new String[]{SharedPrefsUtil.getValue(this, "userInfo", "userid", "")});

            while (cursor.moveToNext()) {
                OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                String userid = cursor.getString(cursor.getColumnIndex("UserId"));
                String usertype = cursor.getString(cursor.getColumnIndex("UserType"));
                String dealersid = cursor.getString(cursor.getColumnIndex("DealersId"));
                String dealersName = cursor.getString(cursor.getColumnIndex("DealersName"));
                String date = cursor.getString(cursor.getColumnIndex("Date"));
                bean.setUserid(userid);
                bean.setUsertype(usertype);
                bean.setDealersid(dealersid);
                bean.setDealersName(dealersName);
                bean.setCkrq(date);
                mDatas.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter.setNewData(mDatas);

        //如果无数据设置空布局
        if (mDatas.size() == 0) {
            adapter.setEmptyView(R.layout.nodata_layout, (ViewGroup) recyclerView.getParent());
        }

        dialog.dismiss();

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                OutLibraryHistoryBean bean = (OutLibraryHistoryBean) adapter.getData().get(position);
                Intent intent = new Intent(OffLineCkHistory.this, OffLineBarcode.class);
                intent.putExtra("userid", bean.getUserid());
                intent.putExtra("usertype",bean.getUsertype());
                intent.putExtra("dealersid", bean.getDealersid());
                intent.putExtra("dealersName", bean.getDealersName());
                intent.putExtra("date", bean.getCkrq());
                startActivityForResult(intent, CODE);
            }
        });

        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final BaseQuickAdapter adapter, View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OffLineCkHistory.this);
                builder.setTitle(R.string.Prompt);
                builder.setMessage(R.string.DeleteTemporaryData);
                builder.setPositiveButton(R.string.mine_cancellation_dialog_btn2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OutLibraryHistoryBean bean = (OutLibraryHistoryBean) adapter.getData().get(position);
                        //删除一条信息
                        sqldb.execSQL("delete from out_storage where UserId = ? and DealersId =? and DealersName =? and Date =?", new String[]{
                                PortIpAddress.getUserId(OffLineCkHistory.this), bean.getDealersid(), bean.getDealersName(), bean.getCkrq()
                        });
                        ShowToast.showShort(OffLineCkHistory.this, "删除成功");
                        search_edittext.setText("");
                        mConnect();
                    }
                });
                builder.setNegativeButton(R.string.mine_cancellation_dialog_btn1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                return false;
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
                        search(search_edittext.getText().toString().trim());
                    } else {
                        refreshLayout.setEnableRefresh(true);
                        search_clear.setVisibility(View.GONE);
//                        if (adapter != null) {
//                            adapter.setNewData(mDatas);
//                        }
//                        mConnect();
                        adapter.setNewData(mDatas);
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


    /**
     * 清除搜索框内容
     */
    @OnClick(R.id.search_clear)
    public void ClearSearch() {
        search_edittext.setText("");
        search_clear.setVisibility(View.GONE);
    }

    //搜索框
    private void search(String str) {
        if (mDatas != null) {
            searchDatas = new ArrayList<OutLibraryHistoryBean>();
            for (OutLibraryHistoryBean entity : mDatas) {
                try {
                    if (entity.getDealersName().contains(str) || entity.getCkrq().contains(str)) {
                        searchDatas.add(entity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adapter.setNewData(searchDatas);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mConnect();
        }
    }
}
