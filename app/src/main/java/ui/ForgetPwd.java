package ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.EditText;
import android.widget.TextView;

import com.example.administrator.materialmanagement.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utile.BaseActivity;

public class ForgetPwd extends BaseActivity {
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.moblie_num_etv)
    EditText moblie_num_etv;
    @BindView(R.id.send_message)
    TextView send_message;
    @BindView(R.id.verification_code)
    EditText verification_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void initData() {
        title_name.setText("密码设置");
        send_message.setText("获取验证码");
    }

    @OnClick(R.id.back)
    void Back(){
        finish();
    }

    @OnClick(R.id.send_message)
    void SendMessage() {
        CountDown(send_message);
    }

    /**
     * 倒计时30S
     */
    private void CountDown(final TextView textView) {
        //10秒倒计时   一秒一跳
        CountDownTimer timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textView.setEnabled(false);
                textView.setBackgroundResource(R.color.gray_deep);
                textView.setText("已发送(" + millisUntilFinished / 1000 + ")");
            }

            @Override
            public void onFinish() {
                textView.setEnabled(true);
                textView.setBackgroundResource(R.color.colorPrimary);
                textView.setText("重新获取验证码");
            }
        }.start();
    }

    @OnClick(R.id.next_submit)
    void Submit(){
        //判断验证码输入和后台发送的验证码是否一致 进入重新设置密码
        Intent intent = new Intent(this,ModifyPwd.class);
        intent.putExtra("tag","forget");
        startActivity(intent);
    }
}
