package com.example.sim_details;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.annotation.NonNull;
import android.Manifest;

import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;


public class MainActivity extends FlutterActivity {

    public static String channel_1 = "Channel";
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(),channel_1).setMethodCallHandler(
                ((call, result) -> {
                    if (call.method.equals("getSimDetails")) {
                        if (checkPermission()) {
                            Map<String, Object> simDetails = getSimDetails();
                            if (simDetails != null) {
                                result.success(simDetails);
                            } else {
                                result.error("UNAVAILABLE", "SIM details not available.", null);
                            }
                        } else {
                            requestPermission();
                            result.error("PERMISSION_DENIED", "Permission not granted.", null);
                        }
                    } else {
                        result.notImplemented();
                    }
                })
        );
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                System.out.println("Permission granted");
            } else {
                // Permission denied
                System.out.println("Permission denied");
            }
        }
    }
    private Map<String, Object> getSimDetails() {
        Map<String, Object> simData = new HashMap<>();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // Request permission if not granted
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
                    return null;
                }

                List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

                if (subscriptionInfoList != null && !subscriptionInfoList.isEmpty()) {
//                    for (SubscriptionInfo info : subscriptionInfoList) {
                    for (int i = 0; i < subscriptionInfoList.size(); i++) {
                        simData.put("simSlot" + i, subscriptionInfoList.get(i).getSimSlotIndex());
                        simData.put("carrierName" + i,  subscriptionInfoList.get(i).getCarrierName());
                        simData.put("displayName" + i, subscriptionInfoList.get(i).getDisplayName());
                        simData.put("country" + i, subscriptionInfoList.get(i).getCountryIso());
                    }
                    return simData;
                } else {
                    System.out.println("No active SIMs found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return simData;
    }
}