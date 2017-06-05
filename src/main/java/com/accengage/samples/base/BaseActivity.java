package com.accengage.samples.base;

import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.accengage.samples.R;
import com.ad4screen.sdk.Acc;

public class BaseActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        setupToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Acc.get(this).startActivity(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Acc.get(this).setIntent(intent);
    }

    @Override
    protected void onPause() {
        Acc.get(this).stopActivity(this);
        super.onPause();
    }

    protected void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

}
