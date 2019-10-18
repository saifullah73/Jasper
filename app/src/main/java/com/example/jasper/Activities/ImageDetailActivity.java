package com.example.jasper.Activities;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.jasper.R;

import java.util.Objects;

public class ImageDetailActivity extends AppCompatActivity {

    private boolean mIsFullScreen;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_detail);
        mToolbar = findViewById(R.id.toolbar);
        mIsFullScreen = false;

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setTitleTextColor(Color.WHITE);
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);

        upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        mIsFullScreen = true;


        String path = getIntent().getStringExtra("path");
        Log.d("imagepath", "onClick: Image Path is: " + path);


        AppCompatImageView photoView = findViewById(R.id.photo_view);


        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateView();
            }
        });

        Glide.with(this).load(path).placeholder(R.drawable.ic_photo_size_select_actual_black_24dp).into(photoView);
    }

    public void updateView() {
        Log.d("checkbool", "onClick: bool is: " + mIsFullScreen);
        if (!mIsFullScreen) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    private void hideSystemUI() {
        mIsFullScreen = true;
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showSystemUI() {
        mIsFullScreen = false;
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}