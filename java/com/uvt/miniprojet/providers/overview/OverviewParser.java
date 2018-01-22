package com.uvt.miniprojet.providers.overview;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.uvt.miniprojet.ConfigParser;
import com.uvt.miniprojet.drawer.NavItem;
import com.uvt.miniprojet.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author Mohamed Kdidi
 * Copyright 2017
 */

/**
 * Async task class to get json by making HTTP call
 */
public class OverviewParser extends AsyncTask<Void, Void, Void> {

    //Instance variables
    private String sourceLocation;
    private Activity context;
    private CallBack callback;

    private ArrayList<NavItem> result;

    private boolean facedException;
    
    public OverviewParser(String sourceLocation, Activity context, CallBack callback){
        this.sourceLocation = sourceLocation;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... args) {

        JSONArray jsonMenu = null;

        try {
            //Get the JSON
            if (sourceLocation.contains("http")) {
                String jsonStr = Helper.getDataFromUrl(sourceLocation);
                jsonMenu = new JSONArray(jsonStr);
            } else {
                String jsonStr = Helper.loadJSONFromAsset(context, sourceLocation);
                if (jsonStr != null)
                    jsonMenu = new JSONArray(jsonStr);
            }

        } catch (JSONException e) {
            Log.e("INFO", "JSON was invalid");
            facedException = true;
            e.printStackTrace();
        }


        if (jsonMenu  != null) {

            final JSONArray jsonActions = jsonMenu;
            result = new ArrayList<NavItem>();

            try {
                // looping through all menu items
                for (int i = 0; i < jsonActions.length(); i++) {
                    JSONObject jsonAction = jsonActions.getJSONObject(i);

                    result.add(ConfigParser.navItemFromJSON(jsonAction));
                }
            } catch (final JSONException e) {
                e.printStackTrace();
                Log.e("INFO", "JSON was invalid");
                facedException = true;
            }

        } else {
            Log.e("INFO", "JSON Could not be retrieved");
            facedException = true;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void args) {
        if (callback != null)
            callback.categoriesLoaded(result, facedException);
    }

    public interface CallBack {
        void categoriesLoaded(ArrayList<NavItem> result, boolean failed);
    }


}
