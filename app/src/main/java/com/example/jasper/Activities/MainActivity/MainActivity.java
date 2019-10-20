package com.example.jasper.Activities.MainActivity;

import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jasper.Activities.Login;
import com.example.jasper.Activities.MainActivity.Fragments.ChatListFragment;
import com.example.jasper.Activities.MainActivity.Fragments.GroupsFragment;
import com.example.jasper.Activities.MainActivity.Fragments.RequestFragment;
import com.example.jasper.Adapters.ViewPagerAdapter;
import com.example.jasper.AppBackend.Xmpp.XmppCore;
import com.example.jasper.R;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements GroupsFragment.OnFragmentInteractionListener, RequestFragment.OnFragmentInteractionListener{

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    private TabLayout.OnTabSelectedListener OnTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int c = tab.getPosition();
            adapter.SetOnSelectView(tabLayout, c, getApplicationContext());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            int c = tab.getPosition();
            adapter.SetUnSelectView(tabLayout, c, getApplicationContext());
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (XmppCore.getInstance().getXmppConnection() == null) {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }
        initComponents();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!XmppCore.getInstance().getXmppConnection().isConnected()){
            XmppCore.getInstance().reLogin();
        }
    }


    private void initComponents(){
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager2);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setUpCustomViewTab();
        tabLayout.addOnTabSelectedListener(OnTabSelectedListener);
        adapter.SetOnSelectView(tabLayout, viewPager.getCurrentItem(), getApplicationContext());

    }

    private void setUpCustomViewTab() {
        try {
            RelativeLayout tab1 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab_title, null);
            TextView title1 = tab1.findViewById(R.id.tab);
            TextView badge1 = tab1.findViewById(R.id.badge);
            badge1.setVisibility(GONE);
            title1.setText("Chats");
            tabLayout.getTabAt(0).setCustomView(tab1);

            RelativeLayout tab2 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab_title, null);
            TextView title2 = tab2.findViewById(R.id.tab);
            TextView badge2 = tab2.findViewById(R.id.badge);
            badge2.setVisibility(GONE);
            title2.setText("Groups");
            tabLayout.getTabAt(1).setCustomView(tab2);

            RelativeLayout tab3 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab_title, null);
            TextView title3 = tab3.findViewById(R.id.tab);
            TextView badge3 = tab3.findViewById(R.id.badge);
            title3.setText("Requests");
            tabLayout.getTabAt(2).setCustomView(tab3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(ChatListFragment.getInstance(), "");
        adapter.addFragment(GroupsFragment.getInstance(), "");
        adapter.addFragment(RequestFragment.getInstance(), "");
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
