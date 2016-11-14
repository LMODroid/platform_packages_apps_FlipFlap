/*
 * Copyright (c) 2014 The CyanogenMod Project
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * Also add information on how to contact you by electronic and paper mail.
 *
 */

package org.cyanogenmod.quickcover;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class QuickCover extends Activity {

    private static final String TAG = "QuickCover";

    private final IntentFilter mFilter = new IntentFilter();
    private GestureDetector mDetector;
    private PowerManager mPowerManager;
    private static Context mContext;

    static QuickCoverStatus sStatus = new QuickCoverStatus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate: QuickCover.class");

        mContext = this;

        mFilter.addAction(QuickCoverConstants.ACTION_COVER_CLOSED);
        mFilter.addAction(QuickCoverConstants.ACTION_KILL_ACTIVITY);
        mContext.getApplicationContext().registerReceiver(receiver, mFilter);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 20;
        getWindow().setAttributes(lp);

        final DrawView drawView = new DrawView(mContext);
        setContentView(drawView);

        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mDetector = new GestureDetector(mContext, new QuickCoverGestureListener());
        sStatus.stopRunning();
    }

    @Override
    public void onStart() {
        super.onStart();

        new Thread(new Service()).start();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Starting up or comign back from screen off
        // Ensure device is awake and redraw

        mPowerManager.wakeUp(SystemClock.uptimeMillis(), "Cover Closed");
        Log.d(TAG, "Cover closed, Time to do work");
        Intent newIntent = new Intent();
        newIntent.setAction(QuickCoverConstants.ACTION_REDRAW);
        mContext.sendBroadcast(newIntent);

    }

    @Override
    public void onPause() {
        super.onPause();

        // Pausing activity and turning screen off
        sStatus.stopRunning();
        mPowerManager.goToSleep(SystemClock.uptimeMillis());

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "QuickCover, onDestroy");
        sStatus.stopRunning();

        // Closing up activity, lets wake up device.
        mPowerManager.wakeUp(SystemClock.uptimeMillis(), "Cover Opened");
        super.onDestroy();

    }

    class Service implements Runnable {
        @Override
        public void run() {
            if (!sStatus.isRunning()) {
                sStatus.startRunning();
                while (sStatus.isRunning()) {
                    Intent batteryIntent = mContext.getApplicationContext().registerReceiver(null,
                            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    int timeout;

                    if(batteryIntent.getIntExtra("plugged", -1) > 0) {
                        timeout = 40;
                    } else {
                        timeout = 20;
                    }

                    for (int i = 0; i <= timeout; i++) {
                        if (!sStatus.isRunning()) {
                            return;
                        }

                        try {
                            BufferedReader br = new BufferedReader(new FileReader(QuickCoverConstants.COVER_NODE));
                            String value = br.readLine();
                            br.close();

                            if (value.equals("0")) {
                                sStatus.stopRunning();
                                finish();
                                overridePendingTransition(0, 0);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading cover device", e);
                        }

                        try {
                            Thread.sleep(500);
                        } catch (IllegalArgumentException e) {
                            // This isn't going to happen
                        } catch (InterruptedException e) {
                            Log.i(TAG, "Sleep interrupted", e);
                        }

                        Intent intent = new Intent();
                        intent.setAction(QuickCoverConstants.ACTION_REDRAW);
                        mContext.sendBroadcast(intent);
                    }
                    mPowerManager.goToSleep(SystemClock.uptimeMillis());
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (!sStatus.isPocketed()) {
            this.mDetector.onTouchEvent(event);
            return super.onTouchEvent(event);
        } else {
            // Say that we handled this event so nobody else does
            return true;
        }
    }

    class QuickCoverGestureListener extends GestureDetector.SimpleOnGestureListener
    {

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            //if screen on, screen off & "pause"
            boolean screenOn = mPowerManager.isInteractive();
            Log.d(TAG, "DT detected, screenon:" + screenOn );
            if(screenOn) {
                // Screen is on, trun it off and go to Sleep now
                onPause();
            }
            else {
                // screen was off, Resume
                onResume();
            }

            return true;
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(QuickCoverConstants.ACTION_KILL_ACTIVITY))  {
                try {
                    context.getApplicationContext().unregisterReceiver(receiver);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Failed to unregister receiver", e);
                }
                sStatus.stopRunning();
                finish();
                overridePendingTransition(0, 0);
                onDestroy();
            } else if (intent.getAction().equals(QuickCoverConstants.ACTION_COVER_CLOSED)) {
                onResume();
            }
        }
    };

}
