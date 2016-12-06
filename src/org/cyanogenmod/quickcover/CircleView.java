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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.RemoteViews;
import android.view.View;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import android.util.Log;

public class CircleView extends View {
    private static final String TAG = "QuickCover";

    private final Context mContext;
    private final Resources res;
    private final IntentFilter mFilter = new IntentFilter();
    private Paint mPaint;
    private int mCenter_x;
    private int mCenter_y;
    private int mRadius;

    Intent batteryStatus;

    private static final float CLOCK_VERTICAL_OFFSET = 0.4f;

    public CircleView(Context context) {
        super(context);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        res = mContext.getResources();
        mCenter_x = res.getInteger(R.integer.x_center);
        mCenter_y = res.getInteger(R.integer.y_center);
        mRadius = res.getInteger(R.integer.radius);
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = mContext.registerReceiver(null, mFilter);

    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        res = mContext.getResources();
        mCenter_x = res.getInteger(R.integer.x_center);
        mCenter_y = res.getInteger(R.integer.y_center);
        mRadius = res.getInteger(R.integer.radius);
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = mContext.registerReceiver(null, mFilter);
    }

    @Override
    public void onDraw(Canvas canvas) {

        mFilter.addAction(QuickCoverConstants.ACTION_REDRAW);
        mContext.getApplicationContext().registerReceiver(receiver, mFilter);

        drawBackground(canvas);
    }

    private void drawBackground(Canvas canvas) {

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        canvas.drawRGB(0, 0, 0);
        mPaint.setStyle(Style.FILL);

        if (isCharging) {
            mPaint.setColor(res.getColor(R.color.charge_bat_bg));
        } else if (level >= 90) {
            mPaint.setColor(res.getColor(R.color.full_bat_bg));
        } else if ((level < 90) && (level >= 30)) {
            mPaint.setColor(res.getColor(R.color.normal_bat_bg));
        } else if ((level < 30) && (level >= 15)) {
            mPaint.setColor(res.getColor(R.color.mid_bat_bg));
        } else if (level < 15) {
            mPaint.setColor(res.getColor(R.color.low_bat_bg));
        }
        Log.e(TAG, "Drawing background" );
        canvas.drawCircle((float) mCenter_x, (float) mCenter_y, (float) mRadius, mPaint);
        Log.e(TAG, "Done drawing background" );
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(QuickCoverConstants.ACTION_REDRAW)) {
                postInvalidate();
            }
        }
    };

}
