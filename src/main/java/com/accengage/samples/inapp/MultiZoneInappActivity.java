package com.accengage.samples.inapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.a4s.sdk.plugins.annotations.UseA4S;
import com.accengage.samples.R;

@UseA4S
public class MultiZoneInappActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_zone_inapp);
    }
}
