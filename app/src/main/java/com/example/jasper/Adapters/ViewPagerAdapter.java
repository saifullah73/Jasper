package com.example.jasper.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.TextView;

import com.example.jasper.R;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentListTitle = new ArrayList<>();

    public void SetOnSelectView(@NonNull TabLayout tabLayout, int position, @NonNull Context context) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        View selected = null;
        if (tab != null) {
            selected = tab.getCustomView();
        }
        TextView iv_text = null;
        TextView iv_text_2 = null;
        if (selected != null) {
            iv_text = selected.findViewById(R.id.tab);
            iv_text_2 = selected.findViewById(R.id.badge);
        }
        if (iv_text != null) {
            iv_text.setTextColor(ContextCompat.getColor(context, R.color.fullwhite));
        }
        if (iv_text_2 != null) {
            DrawableCompat.setTint(
                    DrawableCompat.wrap(iv_text_2.getBackground()),
                    ContextCompat.getColor(context, R.color.fullwhite)
            );
        }

    }

    public void SetUnSelectView(@NonNull TabLayout tabLayout, int position, @NonNull Context context) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        View selected = null;
        if (tab != null) {
            selected = tab.getCustomView();
        }
        TextView iv_text = null;
        TextView iv_text_2 = null;
        if (selected != null) {
            iv_text = selected.findViewById(R.id.tab);
            iv_text_2 = selected.findViewById(R.id.badge);
        }
        if (iv_text != null) {
            iv_text.setTextColor(ContextCompat.getColor(context, R.color.chatActivitysendIconButtonColorNotActive));
        }
        if (iv_text_2 != null) {
            DrawableCompat.setTint(
                    DrawableCompat.wrap(iv_text_2.getBackground()),
                    ContextCompat.getColor(context, R.color.chatActivitysendIconButtonColorNotActive)
            );
        }
    }


    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentListTitle.size();
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentListTitle.get(position);
    }

    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        fragmentListTitle.add(title);
    }
}

