package com.accengage.samples.backstack;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.accengage.samples.R;

public class BackstackActivityWithParent extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backstack);

        TextView label = (TextView) findViewById(R.id.tv_label);
        label.setText(BackstackActivityWithParent.class.getSimpleName());
    }
}
