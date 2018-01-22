package com.uvt.miniprojet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.uvt.miniprojet.drawer.MenuItemCallback;
import com.uvt.miniprojet.drawer.NavItem;
import com.uvt.miniprojet.drawer.SimpleMenu;
import com.uvt.miniprojet.drawer.TabAdapter;
import com.uvt.miniprojet.inherit.BackPressFragment;
import com.uvt.miniprojet.inherit.CollapseControllingFragment;
import com.uvt.miniprojet.inherit.PermissionsFragment;
import com.uvt.miniprojet.providers.CustomIntent;
import com.uvt.miniprojet.providers.fav.ui.FavFragment;
import com.uvt.miniprojet.util.Log;
import com.uvt.miniprojet.util.Helper;
import com.uvt.miniprojet.util.layout.DisableableViewPager;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Mohamed Kdidi
 * Copyright 2017
 */

public class MainActivity extends AppCompatActivity implements MenuItemCallback, ConfigParser.CallBack {

    private Toolbar mToolbar;
    private TabLayout tabLayout;
    private DisableableViewPager viewPager;
    private NavigationView navigationView;
    private TabAdapter adapter;
    private static SimpleMenu menu;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    //Keep track of the interstitials we show
    private int interstitialCount = -1;

    //Data to pass to a fragment
    public static String FRAGMENT_DATA = "transaction_data";
    public static String FRAGMENT_CLASS = "transation_target";

    public static boolean TABLET_LAYOUT = true;

    //Permissions Queu
    List<NavItem> queueItem;
    MenuItem queueMenuItem;

    @Override
    public boolean startNextMatchingActivity(@RequiresPermission @NonNull Intent intent) {
        return super.startNextMatchingActivity(intent);
    }

    @Override
    public void configLoaded(boolean facedException) {
        if (facedException || menu.getFirstMenuItem() == null){
            if (Helper.isOnlineShowDialog(MainActivity.this))
                Toast.makeText(this, R.string.invalid_configuration, Toast.LENGTH_LONG).show();
        } else {
            //Load the first item (we assume the first item doesn't require purchase)
            menuItemClicked(menu.getFirstMenuItem().getValue(), menu.getFirstMenuItem().getKey(), false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load the appropriate layout
        if (useTabletMenu()){
            setContentView(R.layout.activity_main_tablet);
            Helper.setStatusBarColor(MainActivity.this,
                    ContextCompat.getColor(this, R.color.myPrimaryDarkColor));
        } else {
            setContentView(R.layout.activity_main);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (!useTabletMenu())
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        else {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        //Drawer
        if (!useTabletMenu()) {
            drawer = (DrawerLayout) findViewById(R.id.drawer);
            toggle = new ActionBarDrawerToggle(
                    this, drawer, mToolbar, R.string.drawer_open, R.string.drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }

        //Layouts
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (DisableableViewPager) findViewById(R.id.viewpager);

        //Check if we should open a fragment based on the arguments we have
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(FRAGMENT_CLASS)) {
            try {
                Class<? extends Fragment> fragmentClass = (Class<? extends Fragment>) getIntent().getExtras().getSerializable(FRAGMENT_CLASS);
                if (fragmentClass != null) {
                    String[] extra = getIntent().getExtras().getStringArray(FRAGMENT_DATA);

                    HolderActivity.startActivity(this, fragmentClass, extra);
                    finish();
                    //Optionally, we can also point intents to holderactivity directly instead of MainAc.
                }
            } catch (Exception e) {
                //If we come across any errors, just continue and open the default fragment
                Log.printStackTrace(e);
            }
        }

        //Menu items
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        menu = new SimpleMenu(navigationView.getMenu(), this);
        if (Config.USE_HARDCODED_CONFIG) {
            Config.configureMenu(menu, this);
        } else if (!Config.CONFIG_URL.isEmpty() && Config.CONFIG_URL.contains("http"))
            new ConfigParser(Config.CONFIG_URL, menu, this, this).execute();
        else
            new ConfigParser("config.json", menu, this, this).execute();
        tabLayout.setupWithViewPager(viewPager);

        if (!useTabletMenu()) {
            drawer.setStatusBarBackgroundColor(
                    ContextCompat.getColor(this, R.color.myPrimaryDarkColor));
        }

        applyDrawerLocks();


        Helper.updateAndroidSecurityProvider(this);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                onTabBecomesActive(position);
            }
        });
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                boolean foundfalse = false;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        foundfalse = true;
                    }
                }
                if (!foundfalse){
                    //Retry to open the menu item
                    //(we can assume the item is 'purchased' otherwise a permission check would have not occured)
                    menuItemClicked(queueItem, queueMenuItem, false);
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.permissions_required), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void menuItemClicked(List<NavItem> actions, MenuItem item, boolean requiresPurchase) {
        // Checking if the user would prefer to show the menu on start
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean openOnStart = prefs.getBoolean("menuOpenOnStart", false);
        if (drawer != null)
            if (openOnStart && !useTabletMenu() && adapter == null) {
                drawer.openDrawer(GravityCompat.START);
            } else {
                //Close the drawer
                drawer.closeDrawer(GravityCompat.START);
            }


        //Check if the user is allowed to open item
        if (!checkPermissionsHandleIfNeeded(actions, item)) return; //checkPermissions will handle.

        if (isCustomIntent(actions)) return;

        //Uncheck all other items, check the current item
        if (item != null) {
            for (MenuItem menuItem : menu.getMenuItems())
                menuItem.setChecked(false);
            item.setChecked(true);
        }

        //Load the new tab
        boolean isRtl = ViewCompat.getLayoutDirection(tabLayout) == ViewCompat.LAYOUT_DIRECTION_RTL;
        adapter = new TabAdapter(getSupportFragmentManager(), actions, this, isRtl);
        viewPager.setAdapter(adapter);

        //Show or hide the tab bar depending on if we need it
        if (actions.size() == 1) {
            tabLayout.setVisibility(View.GONE);
            viewPager.setPagingEnabled(false);
        } else {
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setPagingEnabled(true);
        }

        //Show in interstitial
        showInterstitial(false);

        onTabBecomesActive(0);
    }

    private void onTabBecomesActive(int position){
        Fragment fragment = adapter.getItem(position);
        //If fragment does not support collapse, or if OS does not support collapse, disable collapsing toolbar
        if ((fragment instanceof CollapseControllingFragment
                && !((CollapseControllingFragment) fragment).supportsCollapse())
                ||
                (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT))
            lockAppBar();
        else
            unlockAppBar();

        if (position != 0)
            showInterstitial(true);
    }

    /**
     * Show an interstitial ad
     * @param fromPager if the showing is triggered from a swipe in the viewpager
     */
    private void showInterstitial(boolean fromPager){
        //if (fromPager) return;


     interstitialCount++;

    }

    /**
     * Checks if the item is/contains a custom intent, and if that the case it will handle it.
     * @param items List of NavigationItems
     * @return True if the item is a custom intent, in that case
     */
    private boolean isCustomIntent(List<NavItem> items){
        NavItem customIntentItem = null;
        for (NavItem item : items){
            if (CustomIntent.class.isAssignableFrom(item.getFragment())){
                customIntentItem = item;
            }
        }

        if (customIntentItem == null) return false;
        if (items.size() > 1) Log.e("INFO", "Custom Intent Item must be only child of menu item! Ignorning all other tabs");

        CustomIntent.performIntent(MainActivity.this, customIntentItem.getData());
        return true;
    }



    /**
     * Checks if the item can be opened because it has sufficient permissions.
     * @param tabs The tabs to check
     * @return true if the item is safe to open
     */
    private boolean checkPermissionsHandleIfNeeded(List<NavItem> tabs, MenuItem item){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return true;

        List<String> allPermissions = new ArrayList<>();
        for (NavItem tab : tabs){
            if (PermissionsFragment.class.isAssignableFrom(tab.getFragment())) {
                try {
                    allPermissions.addAll(Arrays.asList(((PermissionsFragment) tab.getFragment().newInstance()).requiredPermissions()));
                } catch (Exception e) {
                    //Don't really care
                }
            }
        }

        if (allPermissions.size() > 1) {
            boolean allGranted = true;
            for (String permission : allPermissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    allGranted = false;
            }

            if (!allGranted) {
                //TODO An explaination before asking
                requestPermissions(allPermissions.toArray(new String[0]), 1);
                queueItem = tabs;
                queueMenuItem = item;
                return false;
            }

            return true;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                HolderActivity.startActivity(this, SettingsFragment.class, null);
                return true;
            case R.id.favorites:
                HolderActivity.startActivity(this, FavFragment.class, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment activeFragment = null;
        if (adapter != null)
            activeFragment = adapter.getCurrentFragment();

        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (activeFragment instanceof BackPressFragment){
            boolean handled = ((BackPressFragment) activeFragment).handleBackPress();
            if (!handled){
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null)
            for (Fragment frag : fragments)
                if (frag != null)
                    frag.onActivityResult(requestCode, resultCode, data);
*/    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
    }

    //Check if we should adjust our layouts for tablets
    public boolean useTabletMenu(){
        return (getResources().getBoolean(R.bool.isWideTablet) && TABLET_LAYOUT);
    }

    //Apply the appropiate locks to the drawer
    public void applyDrawerLocks(){
        if (drawer == null) {
            if (Config.HIDE_DRAWER)
                navigationView.setVisibility(View.GONE);
            return;
        }

        if (Config.HIDE_DRAWER){
            toggle.setDrawerIndicatorEnabled(false);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    public void lockAppBar() {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(0);
    }

    public void unlockAppBar() {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
    }

}