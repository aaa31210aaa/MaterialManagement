package com.example.administrator.materialmanagement;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import bean.OutLibraryHistoryBean;

public class PreventFleeAdapter extends BaseQuickAdapter<OutLibraryHistoryBean, BaseViewHolder> {
    public PreventFleeAdapter(int layoutResId, @Nullable List<OutLibraryHistoryBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, OutLibraryHistoryBean item) {
        helper.setText(R.id.item, item.getBm());
    }
}
