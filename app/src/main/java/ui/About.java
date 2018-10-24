package ui;

import android.os.Bundle;
import android.widget.TextView;

import com.example.administrator.materialmanagement.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utile.BaseActivity;

public class About extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.about_content)
    TextView about_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        initData();
    }

    @OnClick(R.id.back)
    void Back(){
        finish();
    }

    @Override
    protected void initData() {
        title_name.setText("关于");
        about_content.setText("\u3000\u3000" + "这是一款关于物流追踪的app,可以进行一些出库，退库的操作和查询");
    }
}
