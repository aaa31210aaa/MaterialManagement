package adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.administrator.materialmanagement.R;

import java.util.List;

import bean.OutLibraryHistoryBean;

public class OutLibraryHistoryDetailCpzlAdapter extends BaseQuickAdapter<OutLibraryHistoryBean, BaseViewHolder> {
    public OutLibraryHistoryDetailCpzlAdapter(int layoutResId, @Nullable List<OutLibraryHistoryBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, OutLibraryHistoryBean item) {
        helper.setText(R.id.cpzl_item1, "产品：" + item.getProductname());
        helper.setText(R.id.cpzl_item2, "件数：" + item.getXs());
    }
}
