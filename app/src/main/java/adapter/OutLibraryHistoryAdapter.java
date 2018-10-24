package adapter;

import android.content.Context;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.administrator.materialmanagement.R;

import java.util.List;

import bean.OutLibraryHistoryBean;

public class OutLibraryHistoryAdapter extends BaseQuickAdapter<OutLibraryHistoryBean, BaseViewHolder> {
    private Context context;
    private String tag;

    public OutLibraryHistoryAdapter(Context context, int layoutResId, @Nullable List<OutLibraryHistoryBean> data, String tag) {
        super(layoutResId, data);
        this.context = context;
        this.tag = tag;
    }

    @Override
    protected void convert(BaseViewHolder helper, OutLibraryHistoryBean item) {
        if (tag.equals("ckls")) {
            helper.setText(R.id.item2, "出库日期：" + item.getCkrq());
        }else{
            helper.setText(R.id.item2, "退库日期：" + item.getCkrq());
        }
        helper.setText(R.id.item3, "任务编号：" + item.getRwbh());
    }
}
