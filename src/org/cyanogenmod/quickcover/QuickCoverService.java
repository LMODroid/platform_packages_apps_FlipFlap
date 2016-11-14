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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.util.Log;


public class QuickCoverService extends Service {

    private static final String TAG = "QuickCover";

    private Context mContext;

    private static final int COVER_STATE_CHANGED = 0;

    private static final int COVER_OPEN = 0;
    private static final int COVER_CLOSED = 1;

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private final Object mLock = new Object();
    private final UEventObserver mQuickCoverObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEventObserver.UEvent event) {
            onCoverEvent(Integer.parseInt(event.get("SWITCH_STATE")));
        }
    };


    @Override
    public void onCreate() {

        super.onCreate();
        Log.d(TAG, "Creating service");
        mContext = this;

        Log.d(TAG, "CoverObserver contructor");
        mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        mQuickCoverObserver.startObserving(QuickCoverConstants.COVER_UEVENT_MATCH);


    }

    private final Handler mHandler = new Handler(true /*async*/) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COVER_STATE_CHANGED:
                    handleCoverChange(msg.arg1);
                    mWakeLock.release();
                    break;
            }
        }
    };

    private void handleCoverChange(int state) {
        synchronized (mLock) {

            if(state == 1) {
                Log.e(TAG, "Cover Close, Creating QuickCover Activity");
                Intent intent = new Intent(this, QuickCover.class);
                intent.setAction(QuickCoverConstants.ACTION_COVER_CLOSED);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                Log.e(TAG, "Cover Opened, Killing QuickCover Activity");
                Intent intent = new Intent(QuickCoverConstants.ACTION_KILL_ACTIVITY);
                mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
            }
        }
    }

    private void onCoverEvent(int state) {

        Message message = new Message();
        message.what = COVER_STATE_CHANGED;
        message.arg1 = state;

        mWakeLock.acquire();
        mHandler.sendMessage(message);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
