package com.themejunky.water.ripper.effects;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import effects.ripper.water.themejunky.com.rippereffects.ManagerWaterEffects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.setId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ManagerWaterEffects().setWaterEffects(MainActivity.this);
            }
        });
    }
}
