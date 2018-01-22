package com.uvt.miniprojet.drawer;

import android.view.MenuItem;
import android.view.SubMenu;

import java.util.List;

/**
 * @author Mohamed Kdidi
 * Copyright 2017
 */
public class SimpleSubMenu {
    //Sub menu
    private SubMenu subMenu;
    private String subMenuTitle;

    //Parent menu
    private SimpleMenu parent;

    //Create a new submenu
    public SimpleSubMenu(SimpleMenu menu, String subMenu){
        super();
        this.parent = menu;
        this.subMenuTitle = subMenu;
        this.subMenu = menu.getMenu().addSubMenu(subMenu);

    }

    public MenuItem add(String title, int drawable, List<NavItem> action) {
        return parent.add(subMenu, title, drawable, action);
    }

    public MenuItem add(String title, int drawable, List<NavItem> action, boolean requiresPurchase) {
        return parent.add(subMenu, title, drawable, action, requiresPurchase);
    }

    public String getSubMenuTitle(){
        return subMenuTitle;
    }

}
