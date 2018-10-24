package adapter;

import android.content.Context;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.administrator.materialmanagement.R;

import java.util.List;

import bean.OutLibraryHistoryBean;
import utile.SharedPrefsUtil;

import static com.example.administrator.materialmanagement.Login.ONE_CODE;

public class OutLibraryAdapter extends BaseQuickAdapter<OutLibraryHistoryBean, BaseViewHolder> {
    private Context context;

    public OutLibraryAdapter(int layoutResId, @Nullable List<OutLibraryHistoryBean> data, Context context) {
        super(layoutResId, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, OutLibraryHistoryBean item) {
        if (SharedPrefsUtil.getValue(context, "userInfo", "usertype", "").equals(ONE_CODE)) {
            helper.setText(R.id.item, "代理商名称：" + item.getJxs());
        }else{
            helper.setText(R.id.item, "零售商名称：" + item.getJxs());
        }

    }
}
