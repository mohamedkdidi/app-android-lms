/**
 * Created by Mohamed Kdidi
 * on 29/11/2017.
 * Mini projet dev mobile
 **/

package com.uvt.miniprojet.billing;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import com.uvt.miniprojet.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BillingProcessor extends BillingBase {

	/**
	 * Callback methods where billing events are reported.
	 * Apps must implement one of these to construct a BillingProcessor.
	 */
	public interface IBillingHandler {

		void onBillingError(int errorCode, Throwable error);

		void onBillingInitialized();
	}

	private IInAppBillingService billingService;
	private String contextPackageName;
	private String signatureBase64;
	private BillingCache cachedProducts;
	private BillingCache cachedSubscriptions;
	private IBillingHandler eventHandler;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			billingService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			billingService = IInAppBillingService.Stub.asInterface(service);
			if (eventHandler != null)
				eventHandler.onBillingInitialized();
		}
	};

	public BillingProcessor(Context context, String licenseKey, IBillingHandler handler) {
		super(context);
		signatureBase64 = licenseKey;
		eventHandler = handler;
		contextPackageName = context.getApplicationContext().getPackageName();
		bindPlayServices();
	}

	private void bindPlayServices() {
		try {
			Intent iapIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
			iapIntent.setPackage("com.android.vending");
			getContext().bindService(iapIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		} catch (Exception e) {

		}
	}

	@Override
	public void release() {
		if (serviceConnection != null && getContext() != null) {
			try {
				getContext().unbindService(serviceConnection);
			} catch (Exception e) {
			}
			billingService = null;
		}
		cachedProducts.release();
		super.release();
	}

	public boolean isInitialized() {
		return billingService != null;
	}






	private SkuDetails getSkuDetails(String productId, String purchaseType) {
		ArrayList<String> productIdList = new ArrayList<String>();
		productIdList.add(productId);
		List<SkuDetails> skuDetailsList = getSkuDetails(productIdList, purchaseType);
		if (skuDetailsList != null && skuDetailsList.size() > 0)
			return skuDetailsList.get(0);
		return null;
	}

	private List<SkuDetails> getSkuDetails(ArrayList<String> productIdList, String purchaseType) {
		if (billingService != null && productIdList != null && productIdList.size() > 0) {
			try {
				Bundle products = new Bundle();
				products.putStringArrayList(Constants.PRODUCTS_LIST, productIdList);
				Bundle skuDetails = billingService.getSkuDetails(Constants.GOOGLE_API_VERSION, contextPackageName, purchaseType, products);
				int response = skuDetails.getInt(Constants.RESPONSE_CODE);

				if (response == Constants.BILLING_RESPONSE_RESULT_OK) {
					ArrayList<SkuDetails> productDetails = new ArrayList<SkuDetails>();

					for (String responseLine : skuDetails.getStringArrayList(Constants.DETAILS_LIST)) {
						JSONObject object = new JSONObject(responseLine);
						SkuDetails product = new SkuDetails(object);
						productDetails.add(product);
					}
					return productDetails;

				} else {
					if (eventHandler != null)
						eventHandler.onBillingError(response, null);
				}
			} catch (Exception e) {

			}
		}
		return null;
	}

	public SkuDetails getPurchaseListingDetails(String productId) {
		return getSkuDetails(productId, Constants.PRODUCT_TYPE_MANAGED);
	}

	public SkuDetails getSubscriptionListingDetails(String productId) {
		return getSkuDetails(productId, Constants.PRODUCT_TYPE_SUBSCRIPTION);
	}

	public List<SkuDetails> getPurchaseListingDetails(ArrayList<String> productIdList) {
		return getSkuDetails(productIdList, Constants.PRODUCT_TYPE_MANAGED);
	}

	public List<SkuDetails> getSubscriptionListingDetails(ArrayList<String> productIdList) {
		return getSkuDetails(productIdList, Constants.PRODUCT_TYPE_SUBSCRIPTION);
	}

}
