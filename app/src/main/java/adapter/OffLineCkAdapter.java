package adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.administrator.materialmanagement.R;

import java.util.List;

import bean.OutLibraryHistoryBean;

public class OffLineCkAdapter extends BaseQuickAdapter<OutLibraryHistoryBean, BaseViewHolder> {
    public OffLineCkAdapter(int layoutResId, @Nullable List<OutLibraryHistoryBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, OutLibraryHistoryBean item) {
        helper.setText(R.id.offline_item1, "经销商名称：" + item.getDealersName());
        helper.setText(R.id.offline_item2, "创建时间：" + item.getCkrq());
    }
}
