package com.drinker.applock.password.view;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.drinker.alock.lock.manager.LockHelper;
import com.drinker.alock.lock.manager.LockedApp;
import com.drinker.applock.BaseActivity;
import com.drinker.applock.R;
import com.drinker.applock.applist.view.MainActivity;
import com.drinker.applock.password.Contants;
import com.drinker.applock.password.enums.LockMode;
import com.drinker.applock.password.util.PasswordUtil;
import com.drinker.applock.util.LockUtils;
import com.samsung.android.sdk.pass.support.IFingerprintManagerProxy;

import static com.drinker.applock.password.enums.LockMode.CLEAR_PASSWORD;
import static com.drinker.applock.password.enums.LockMode.SETTING_PASSWORD;

public class PasswordActivity extends BaseActivity implements RippleView.OnRippleCompleteListener {

    protected final static String TITLE = "title";

    protected final static String FROM = "from";

    protected final static int FROM_SET = 1;

    protected final static int FROM_MODIFY = FROM_SET + 1;

    protected final static int FROM_VERIFY = FROM_MODIFY + 1;


    protected CustomLockView lvLock;

    protected int from = FROM_SET;

    protected TextView tvHint;
    protected TextView tvText;
    protected RippleView rvBack;
    protected View topBar;
    protected String currentPackageName;

    @Override
    protected boolean isLockAtForeground(Intent intent) {
        from = intent.getIntExtra(FROM, FROM_SET);
        if (from == FROM_MODIFY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 密码相关操作完成回调提示
     */
    private String getPassWordHint() {
        String str = null;
        switch (lvLock.getMode()) {
            case SETTING_PASSWORD:
                str = "密码设置成功";
                break;
            case EDIT_PASSWORD:
                str = "密码修改成功";
                break;
            case VERIFY_PASSWORD:
                str = "密码正确";
                break;
            case CLEAR_PASSWORD:
                str = "密码已经清除";
                break;
        }
        return str;
    }

    private void gotoMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    protected CustomLockView.OnCompleteListener getOnCompleteListener() {
        return null;
    }


    /**
     * 密码输入监听
     */
    CustomLockView.OnCompleteListener onCompleteListener = new CustomLockView.OnCompleteListener() {
        @Override
        public void onComplete(String password, int[] indexs) {
            tvHint.setText(getPassWordHint());
            LockUtils.setLock();
            if (from == FROM_SET) {
                gotoMain();
            } else if (from == FROM_VERIFY) {
                LockHelper.get().setLockedAppStatus(currentPackageName, LockedApp.UNLOCK);
            }
            finish();
        }

        @Override
        public void onError(String errorTimes) {
            tvHint.setText("密码错误，还可以输入" + errorTimes + "次");
        }

        @Override
        public void onPasswordIsShort(int passwordMinLength) {
            tvHint.setText("密码不能少于" + passwordMinLength + "个点");
        }

        @Override
        public void onAginInputPassword(LockMode mode, String password, int[] indexs) {
            tvHint.setText("请再次输入密码");
        }


        @Override
        public void onInputNewPassword() {
            tvHint.setText("请输入新密码");
        }

        @Override
        public void onEnteredPasswordsDiffer() {
            tvHint.setText("两次输入的密码不一致");
        }

        @Override
        public void onErrorNumberMany() {
            tvHint.setText("密码错误次数超过限制，不能再输入");
        }

    };

    /**
     * 设置解锁模式
     */
    private void setLockMode(LockMode mode) {
        String str = "";
        switch (mode) {
            case CLEAR_PASSWORD:
                str = "清除密码";
                setLockMode(CLEAR_PASSWORD, PasswordUtil.getPin(this), str);
                break;
            case EDIT_PASSWORD:
                str = "修改密码";
                setLockMode(LockMode.EDIT_PASSWORD, PasswordUtil.getPin(this), str);
                break;
            case SETTING_PASSWORD:
                str = "设置密码";
                setLockMode(SETTING_PASSWORD, null, str);
                break;
            case VERIFY_PASSWORD:
                str = "验证密码";
                setLockMode(LockMode.VERIFY_PASSWORD, PasswordUtil.getPin(this), str);
                break;
        }
        if (tvText != null) {
            tvText.setText(str);
        }
    }

    /**
     * 密码输入模式
     */
    private void setLockMode(LockMode mode, String password, String msg) {
        lvLock.setMode(mode);
        lvLock.setErrorNumber(3);
        lvLock.setClearPasssword(false);
        if (mode != SETTING_PASSWORD) {
            tvHint.setText("请输入已经设置过的密码");
            lvLock.setOldPassword(password);
        } else {
            tvHint.setText("请输入要设置的密码");
        }
        if (tvText != null) {
            tvText.setText(msg);
        }
    }
    @Override
    public void beforeInitView() {
        String title = getIntent().getStringExtra(TITLE);
        setTitle(title);
        setContentView(R.layout.activity_password);
    }

    @Override
    protected void initView() {
        rvBack = (RippleView) findViewById(R.id.rv_back);
        tvText = (TextView) findViewById(R.id.tv_text);
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
    }

    @Override
    protected void initListener() {
        rvBack.setOnRippleCompleteListener(this);
        lvLock.setOnCompleteListener(onCompleteListener);
    }

    @Override
    protected void initData() {
        //设置模式
        LockMode lockMode = (LockMode) getIntent().getSerializableExtra(Contants.INTENT_SECONDACTIVITY_KEY);
        if (lockMode != null) {
            setLockMode(lockMode);
        }
    }

    @Override
    public void onSlideStateChanged(int state) {

    }

    @Override
    public void onSlideChange(float percent) {

    }

    @Override
    public void onSlideOpened() {

    }

    @Override
    public void onSlideClosed() {

    }

    public static void startForSet(Activity activity) {
        Intent intent = new Intent(activity, PasswordActivity.class);
        intent.putExtra(FROM, FROM_SET);
        intent.putExtra(TITLE, R.string.set_title);
        intent.putExtra(Contants.INTENT_SECONDACTIVITY_KEY, LockMode.SETTING_PASSWORD);
        activity.startActivity(intent);
    }

    public static void startForModify(Activity activity) {
        Intent intent = new Intent(activity, PasswordActivity.class);
        intent.putExtra(FROM, FROM_MODIFY);
        intent.putExtra(TITLE, R.string.modify_title);
        intent.putExtra(Contants.INTENT_SECONDACTIVITY_KEY, LockMode.EDIT_PASSWORD);
        activity.startActivity(intent);
    }

    @Override
    public void onComplete(RippleView rippleView) {
        onBackPressed();
    }
}
