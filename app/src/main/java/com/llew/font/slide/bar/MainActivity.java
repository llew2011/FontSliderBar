package com.llew.font.slide.bar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FontSliderBar sliderBar = findViewById(R.id.sliderbar);
        sliderBar.setTickCount(6).setTickHeight(30).setBarColor(Color.MAGENTA)
                .setTextColor(Color.CYAN).setTextPadding(20).setTextSize(20)
                .setThumbRadius(30).setThumbColorNormal(Color.CYAN).setThumbColorPressed(Color.GREEN)
                .withAnimation(false).setThumbIndex(4).applay();

    }
}
