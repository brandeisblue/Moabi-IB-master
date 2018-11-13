package com.ivorybridge.moabi.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MotionSensorServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 26) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                Intent serviceIntent = new Intent(context, MotionSensorService.class);
                context.startForegroundService(serviceIntent);
            }
        } else {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                context.startService(new Intent(context, MotionSensorService.class));
            }
        }
    }
}
