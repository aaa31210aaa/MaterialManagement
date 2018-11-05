package ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.administrator.materialmanagement.R;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapter.OutLibraryHistoryDetailCpzlAdapter;
import bean.OutLibraryHistoryBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utile.BaseActivity;
import utile.DateUtils;
import utile.DialogUtil;
import utile.DividerItemDecoration;
import utile.PortIpAddress;
import utile.SharedPrefsUtil;
import utile.ShowToast;

import static com.example.administrator.materialmanagement.Login.ONE_CODE;
import static utile.PortIpAddress.CODE;
import static utile.PortIpAddress.MESSAGE;
import static utile.PortIpAddress.SUCCESS_CODE;

public class OutLibraryHistoryDetail extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private String tag = "";
    private String url = "";
    private String dataKey = "";


    private OutLibraryHistoryDetailCpzlAdapter adapter;
    private List<OutLibraryHistoryBean> mDatas;

    private View headView;
    private TextView rwbh_tv;
    private TextView dls_jxs_title;
    private TextView dls_jxs_tv;
    private TextView sjxs_tv;
    private TextView rq_title;
    private TextView rq_tv;
    //    private TextView jbr_tv;
    private TextView bz_tv;
    private TextView cktkxq_title;
    private String taskid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_library_history_detail);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
        headView = View.inflate(this, R.layout.history_detail_head, null);
        rwbh_tv = headView.findViewById(R.id.rwbh_tv);
        dls_jxs_title = headView.findViewById(R.id.dls_jxs_title);
        dls_jxs_tv = headView.findViewById(R.id.dls_jxs_tv);
        sjxs_tv = headView.findViewById(R.id.sjxs_tv);
        rq_title = headView.findViewById(R.id.rq_title);
        rq_tv = headView.findViewById(R.id.rq_tv);
//        jbr_tv = headView.findViewById(R.id.jbr_tv);
        bz_tv = headView.findViewById(R.id.bz_tv);
        cktkxq_title = headView.findViewById(R.id.cktkxq_title);

        rq_tv.setText(DateUtils.dateToStrHour());

        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        taskid = intent.getStringExtra("taskid");
        cktkxq_title.setText("产品种类");
        if (tag.equals("ckls")) {
            title_name.setText("出库结果");
            rq_title.setText("出库日期");
            url = PortIpAddress.OutLibraryDetail();
            dataKey = "bean.outdate";
        } else if (tag.equals("tkls")) {
            title_name.setText("退库结果");
            rq_title.setText("退库日期");
            url = PortIpAddress.TkLibraryDetail();
            dataKey = "bean.refunddate";
        } else {
            title_name.setText("调剂结果");
            rq_title.setText("调剂日期");
            url = PortIpAddress.TiaoJiLibraryDetail();
            dataKey = "bean.taskdate";
        }

        if (SharedPrefsUtil.getValue(this, "userInfo", "usertype", "").equals(ONE_CODE)) {
            dls_jxs_title.setText("代理商");
        } else {
            dls_jxs_title.setText("零售商");
        }

        initRv();
        mConnect();
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
            adapter = new OutLibraryHistoryDetailCpzlAdapter(R.layout.cpzl_item, mDatas);
            adapter.bindToRecyclerView(recyclerView);
            adapter.addHeaderView(headView);
            recyclerView.setAdapter(adapter);
        }
        mDatas = new ArrayList<>();
        dialog = DialogUtil.createLoadingDialog(OutLibraryHistoryDetail.this, R.string.loading);
    }


    private void mConnect() {
        OkGo.<String>get(url)
                .tag(TAG)
                .params("loginuserid", SharedPrefsUtil.getValue(this, "userInfo", "userid", ""))
                .params("loginusertype", SharedPrefsUtil.getValue(this, "userInfo", "usertype", ""))
                .params(PortIpAddress.Bean + "taskid", taskid)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            String err = jsonObject.getString(MESSAGE);
                            if (jsonObject.getString(CODE).equals(SUCCESS_CODE)) {
                                mDatas.clear();
                                JSONArray jsonArray = jsonObject.getJSONArray("listdata");
                                rwbh_tv.setText(jsonObject.getString("bean.taskno"));
                                dls_jxs_tv.setText(jsonObject.getString("bean.dealersname"));
                                sjxs_tv.setText(jsonObject.getString("bean.realbox"));
                                rq_tv.setText(jsonObject.getString(dataKey));
                                bz_tv.setText(jsonObject.getString("bean.memo"));

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
                                    bean.setProductid(jsonArray.optJSONObject(i).getString("bean.productid"));
                                    bean.setProductname(jsonArray.optJSONObject(i).getString("bean.productname"));
                                    bean.setXs(jsonArray.optJSONObject(i).getString("bean.pnum"));
                                    mDatas.add(bean);
                                }
                                adapter.setNewData(mDatas);


                                adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                                        OutLibraryHistoryBean bean = (OutLibraryHistoryBean) adapter.getData().get(position);
                                        Intent intent = new Intent(OutLibraryHistoryDetail.this, OutLibraryHistoryDetailPzxq.class);
                                        intent.putExtra("taskid", taskid);
                                        intent.putExtra("productid", bean.getProductid());
                                        intent.putExtra("tag", tag);
                                        startActivity(intent);
                                    }
                                });
                            } else {
                                ShowToast.showShort(OutLibraryHistoryDetail.this, err);
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
                        ShowToast.showShort(OutLibraryHistoryDetail.this, R.string.connect_err);
                    }
                });


//        for (int i = 0; i < 10; i++) {
//            OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
//            bean.setId(i + "");
//            bean.setCpzl("XXX产品");
//            bean.setXs("XXX箱");
//            mDatas.add(bean);
//        }
//        adapter.setNewData(mDatas);
//
//
//        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
//                OutLibraryHistoryBean bean = (OutLibraryHistoryBean) adapter.getData().get(position);
//                Intent intent = new Intent(OutLibraryHistoryDetail.this, OutLibraryHistoryDetailPzxq.class);
//                startActivity(intent);
//            }
//        });
    }
}
