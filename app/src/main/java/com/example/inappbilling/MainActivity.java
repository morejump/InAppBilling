package com.example.inappbilling;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private BillingClient billingClient;
    private List<String> skuList;
    private String premium_upgrade_price;
    private String gas_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        billingClient = BillingClient.newBuilder(this).setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // successfully connect
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // do something here later
            }
        });
        skuList = new ArrayList<>();
        skuList.add("premium_upgrade");
        skuList.add("gas");
        SkuDetailsParams.Builder skuDetailsParams = SkuDetailsParams.newBuilder();
        skuDetailsParams.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(skuDetailsParams.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                // process result
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    for (SkuDetails skuDetails : skuDetailsList) {
                        String sku = skuDetails.getSku();
                        String price = skuDetails.getPrice();
                        if ("premium_upgrade".equals(sku)) {
                            premium_upgrade_price = price;
                        }
                    }
                }
            }
        });
        BillingResult billingResult =billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS);
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){}

    }

    /**
     * Implement this method to get notifications for purchases updates. Both purchases initiated by
     * your app and the ones initiated outside of your app will be reported here.
     *
     * <p><b>Warning!</b> All purchases reported here must either be consumed or acknowledged. Failure
     * to either consume (via {@link BillingClient#consumeAsync}) or acknowledge (via {@link
     * BillingClient#acknowledgePurchase}) a purchase will result in that purchase being refunded.
     * Please refer to
     * https://developer.android.com/google/play/billing/billing_library_overview#acknowledge for more
     * details.
     *
     * @param billingResult BillingResult of the update.
     * @param purchases     List of updated purchases if present.
     */
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {

    }
}
