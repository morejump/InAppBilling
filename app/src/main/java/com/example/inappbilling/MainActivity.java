package com.example.inappbilling;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    private BillingClient billingClient;
    private List<String> skuList;
    private List<SkuDetails> skuDetailsList;
    private String premium_upgrade_price;
    private String gas_price;

    Button btnPurchaseItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnPurchaseItem = findViewById(R.id.btnPurchaseItem);
        btnPurchaseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                purchaseItem();
            }
        });
        ConsumeParams consumeParams = ConsumeParams.newBuilder().build();
        billingClient = BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build();
        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // successfully connect
                    Log.d(TAG, "onBillingSetupFinished: ");
                    getPrice();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // do something here later
                Log.d(TAG, "onBillingServiceDisconnected: ");
            }
        });

        BillingResult billingResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS);
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
        }
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
        Log.d(TAG, "onPurchasesUpdated: ");
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // grant entitlement to the user
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                        // do something here later after acknowledge purchase
                    }
                });
            }
        }
    }

    public void getPrice() {
        skuList = new ArrayList<>();
        skuList.add("android.test.purchased");
        skuList.add("android.test.purchased");
        skuList.add("android.test.purchased");
        skuList.add("gas");
        SkuDetailsParams.Builder skuDetailsParams = SkuDetailsParams.newBuilder();
        skuDetailsParams.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(skuDetailsParams.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                // process result
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    Log.d(TAG, "onSkuDetailsResponse: OK");
                    updateSkudetailsList(skuDetailsList);
                } else {
                    Log.d(TAG, "onSkuDetailsResponse: Not OK");
                }
            }
        });
    }

    public void updateSkudetailsList(List<SkuDetails> skuDetailsList) {
        this.skuDetailsList = skuDetailsList;
    }

    public boolean isPurchaseItem(String sku) {
        for (String item : skuList) {
            if (sku.equals(item)) {
                return true;
            }
        }
        return false;
    }

    public void purchaseItem() {

        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetailsList.get(0))
                .build();
        BillingResult billingResult = billingClient.launchBillingFlow(this, flowParams);
    }
}
