package com.accengage.samples.backstack;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.accengage.samples.R;

public class BackstackActivity extends Activity {

    public static final String EXTRA_PARAM_KEY = "BACKSTACK_ACTIVITY_EXTRA_PARAM_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backstack);

        String extraParam = getIntent().getStringExtra(EXTRA_PARAM_KEY);
        if (extraParam == null) extraParam = "null";

        TextView label = (TextView) findViewById(R.id.tv_label);
        label.setText(BackstackActivity.class.getSimpleName() + " " + extraParam);
    }
}
