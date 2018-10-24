package adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.administrator.materialmanagement.R;

import java.util.List;

import bean.AgentRetailerBean;

public class AgentRetailerAdapter extends BaseQuickAdapter<AgentRetailerBean, BaseViewHolder> {
    public AgentRetailerAdapter(int layoutResId, @Nullable List<AgentRetailerBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, AgentRetailerBean item) {
        helper.setText(R.id.item1, "编号：" + item.getDlsbh());
        helper.setText(R.id.item2, "名称：" + item.getDlsmc());
        helper.setText(R.id.item3, "联系电话：" + item.getDlslxdh());
    }
}
