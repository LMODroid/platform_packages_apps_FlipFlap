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

package org.cyanogenmod.quickcircle;

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
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import android.util.Log;

public class DrawView extends View {
    private static final String TAG = "QuickCircle";

    private final Context mContext;
    private final Resources res;
    private final IntentFilter mFilter = new IntentFilter();
    private Paint mPaint;
    private int mCenter_x;
    private int mCenter_y;
    private int mRadius;

    private static final float CLOCK_VERTICAL_OFFSET = 0.4f;

    public DrawView(Context context) {
        super(context);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        res = mContext.getResources();
        mCenter_x = res.getInteger(R.integer.x_center);
        mCenter_y = res.getInteger(R.integer.y_center);
        mRadius = res.getInteger(R.integer.radius);
    }

    @Override
    public void onDraw(Canvas canvas) {

        drawBackground(canvas);
        drawTime(canvas);

        mFilter.addAction(QuickCircleConstants.ACTION_REDRAW);
        mContext.getApplicationContext().registerReceiver(receiver, mFilter);
    }

    private timeObject getTimeObject() {
        timeObject timeObj = new timeObject();
        timeObj.hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        timeObj.min = Calendar.getInstance().get(Calendar.MINUTE);

        if (DateFormat.is24HourFormat(mContext)) {
            timeObj.is24Hour = true;
        } else {
            timeObj.is24Hour = false;
            if (timeObj.hour > 11) {
                if (timeObj.hour > 12) {
                    timeObj.hour = timeObj.hour - 12;
                }
                timeObj.am = false;
            } else {
                if (timeObj.hour == 0) {
                    timeObj.hour = 12;
                }
                timeObj.am = true;
            }
        }

        timeObj.timeString = (timeObj.hour < 10
                ? " " + Integer.toString(timeObj.hour)
                : Integer.toString(timeObj.hour))
                + ":" +(timeObj.min < 10
                ? "0"  + Integer.toString(timeObj.min)
                : Integer.toString(timeObj.min));
        return timeObj;
    }

    private void drawBackground(Canvas canvas) {

        mPaint.setStyle(Style.FILL);
        mPaint.setColor(res.getColor(R.color.circle_background));
        Log.e(TAG, "Drawing background" );
        canvas.drawCircle((float) mCenter_x, (float) mCenter_y, (float) mRadius, mPaint);
        Log.e(TAG, "Done drawing background" );
    }

    private void drawTime(Canvas canvas) {
        timeObject time = getTimeObject();
        int mClockTextColor = res.getColor(R.color.clock_text_color);
        float mClockFontSize = res.getDimension(R.dimen.clock_font_size);
        float mTextHeight;
        String allDigits = String.format("%010d", 123456789);

        Typeface androidClockMonoLight = Typeface.
                createFromAsset(mContext.getAssets(), "fonts/AndroidClockMono-Light.ttf");
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(androidClockMonoLight);

        mTextHeight = mClockFontSize;
        mPaint.setTextSize(mClockFontSize);
        mPaint.setColor(mClockTextColor);

        if (TextUtils.isEmpty(allDigits)) {
            Log.e(TAG, "Locale digits missing - using English");
            allDigits = "0123456789";
        }

        float widths[] = new float[allDigits.length()];
        int ll = mPaint.getTextWidths(allDigits, widths);
        int largest = 0;
        for (int ii = 1; ii < ll; ii++) {
            if (widths[ii] > widths[largest]) {
                largest = ii;
            }
        }

        String mWidest = allDigits.substring(largest, largest + 1);
        float mTotalTextWidth = time.timeString.length() * mPaint.measureText(mWidest);

        float xTextStart = mCenter_x - mTotalTextWidth / 2;
        float yTextStart = mCenter_y + mTextHeight/2 - (mTextHeight * CLOCK_VERTICAL_OFFSET);

        float textEm  = mPaint.measureText(mWidest) / 2f;
        int ii=0;
        float x=xTextStart;

        while (ii < time.timeString.length()) {
            x += textEm;
            canvas.drawText(time.timeString.substring(ii, ii + 1), x, yTextStart, mPaint);
            x += textEm;
            ii++;
        }

    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(QuickCircleConstants.ACTION_REDRAW)) {
                postInvalidate();
            }
        }
    };

    private class timeObject {
        String timeString;
        int hour;
        int min;
        boolean is24Hour;
        boolean am;
    }

}
