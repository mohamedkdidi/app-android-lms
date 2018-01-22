package com.uvt.miniprojet;

import com.uvt.miniprojet.billing.BillingProcessor;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;
import android.text.Html;
import com.uvt.miniprojet.util.Log;
import android.widget.Toast;

/**
 * This fragmnt is used to show a settings page to the user
 */

public class SettingsFragment extends PreferenceFragment implements
		BillingProcessor.IBillingHandler {
	
	//You can change this setting if you would like to disable rate-my-app
	boolean HIDE_RATE_MY_APP = false;

	String start;
	String menu;
	BillingProcessor bp;
	Preference preferencepurchase;
	
	AlertDialog dialog;
	
    private static String PRODUCT_ID_BOUGHT = "item_1_bought";
	public static String SHOW_DIALOG = "show_dialog";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_settings);

		// open play store page
		Preference preferencerate = findPreference("rate");
		preferencerate
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Uri uri = Uri.parse("market://details?id="
								+ getActivity().getPackageName());
						Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
						try {
							startActivity(goToMarket);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(getActivity(),
									"Could not open Play Store",
									Toast.LENGTH_SHORT).show();
							return true;
						}
						return true;
					}
				});

		// open about dialog
		Preference preferenceabout = findPreference("about");
		preferenceabout
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						AlertDialog.Builder ab = null;
						ab = new AlertDialog.Builder(getActivity());
						ab.setMessage(Html.fromHtml(getResources().getString(
								R.string.about_text)));
						ab.setPositiveButton(
								getResources().getString(R.string.ok), null);
						ab.setTitle(getResources().getString(
								R.string.about_header));
						ab.show();
						return true;
					}
				});
		

		String[] extra = getArguments().getStringArray(MainActivity.FRAGMENT_DATA);
		if (null != extra && extra.length != 0 && extra[0].equals(SHOW_DIALOG)){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Add the buttons
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User cancelled the dialog
			           }
			       });


			// Create the AlertDialog
			dialog = builder.create();
			dialog.show();
		}
		
		if (HIDE_RATE_MY_APP){
			PreferenceCategory other = (PreferenceCategory) findPreference("other");
			Preference preference = findPreference("rate");
			other.removePreference(preference);
		}

	}

	@Override
	public void onBillingInitialized() {
		/*
		 * Called when BillingProcessor was initialized and it's ready to
		 * purchase
		 */
	}



	@Override
	public void onBillingError(int errorCode, Throwable error) {
		Log.v("INFO", "Error");
	}







	
	@Override
	public void onDestroy() {
	   if (bp != null) 
	        bp.release();

	    super.onDestroy();
	}
}
