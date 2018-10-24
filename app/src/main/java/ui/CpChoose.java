package ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.administrator.materialmanagement.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import adapter.OutLibraryAdapter;
import bean.OutLibraryHistoryBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import utile.BaseActivity;
import utile.DividerItemDecoration;

public class CpChoose extends BaseActivity {
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
    private OutLibraryAdapter adapter;
    private List<OutLibraryHistoryBean> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp_choose);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
        title_name.setText(R.string.cplb);
        initRv();
        mConnect();
    }

    private void initRv() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        if (adapter == null) {
            adapter = new OutLibraryAdapter(R.layout.jxs_item, mDatas,this);
            adapter.bindToRecyclerView(recyclerView);
//            adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
//            adapter.isFirstOnly(true);
            recyclerView.setAdapter(adapter);
        }
        mDatas = new ArrayList<>();
    }



    private void mConnect() {
        for (int i = 0; i < 10; i++) {
            OutLibraryHistoryBean bean = new OutLibraryHistoryBean();
            bean.setJxs("XXX产品"+i);
            mDatas.add(bean);
        }
        adapter.setNewData(mDatas);


        //列表子项点击监听
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                OutLibraryHistoryBean bean = (OutLibraryHistoryBean) adapter.getData().get(position);
                Intent intent = new Intent();
                intent.putExtra("cp_name", bean.getJxs());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
