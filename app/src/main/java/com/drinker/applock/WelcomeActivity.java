package com.drinker.applock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.drinker.applock.applist.view.MainActivity;
import com.drinker.applock.password.util.LockUtil;
import com.drinker.applock.password.view.PasswordActivity;
import com.drinker.applock.util.LockUtils;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LockUtils.isLockSet()) {
            goMain();
        } else {
            goSetPassword();
        }
        finish();
//        setContentView(R.layout.activity_welcome);
    }

    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }

    private void goSetPassword() {
        PasswordActivity.startForSet(this);
    }
}
