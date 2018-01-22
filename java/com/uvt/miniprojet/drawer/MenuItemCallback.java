package com.uvt.miniprojet.drawer;

import android.view.MenuItem;

import java.util.List;

/**
 * @author Mohamed Kdidi
 * Copyright 2017
 */
public interface MenuItemCallback {

    void menuItemClicked(List<NavItem> action, MenuItem item, boolean requiresPurchase);
}
