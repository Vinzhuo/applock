package com.drinker.applock.password.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.drinker.applock.Constants;
import com.drinker.applock.R;
import com.drinker.applock.password.Contants;
import com.drinker.applock.password.enums.LockMode;
import com.drinker.core.util.IntentUtils;

public class CompareActivity extends PasswordActivity {

    public static void start(Context activity, String packageName) {
        Intent intent = new Intent(activity, CompareActivity.class);
        intent.putExtra(FROM, FROM_VERIFY);
        intent.putExtra(TITLE, R.string.verify_title);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.INTENT_EXTRA_KEY_LOCK_PACKAGENAME, packageName);
        intent.putExtra(Contants.INTENT_SECONDACTIVITY_KEY, LockMode.VERIFY_PASSWORD);
        activity.startActivity(intent);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);

    }
    @Override
    protected void onResume() {
        super.onResume();
        currentPackageName = getIntent().getStringExtra(Constants.INTENT_EXTRA_KEY_LOCK_PACKAGENAME);
    }

    @Override
    public void onBackPressed() {
        IntentUtils.goLauncher(this);
        finish();
    }

    @Override
    public void beforeInitView() {
        String title = getIntent().getStringExtra(TITLE);
        setTitle(title);
        setContentView(R.layout.activity_compare);
    }

    @Override
    protected void initView() {
        tvHint = (TextView) findViewById(R.id.tv_hint);
        lvLock = (CustomLockView) findViewById(R.id.lv_lock);
        //显示绘制方向
        lvLock.setShow(true);
        //允许最大输入次数
        lvLock.setErrorNumber(3);
        //密码最少位数
        lvLock.setPasswordMinLength(4);
        //编辑密码或设置密码时，是否将密码保存到本地，配合setSaveLockKey使用
        lvLock.setSavePin(true);
        //保存密码Key
        lvLock.setSaveLockKey(Contants.PASS_KEY);
//        if (from == FROM_VERIFY) {
//            findViewById(R.id.top_bar).setVisibility(View.GONE);
//        }
    }
    @Override
    protected void initListener() {
//        rvBack.setOnRippleCompleteListener(this);
        lvLock.setOnCompleteListener(onCompleteListener);
    }
}
