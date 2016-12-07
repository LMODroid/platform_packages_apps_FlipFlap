/*
 * Copyright (C) 2013 The CyanogenMod Project (DvTonder)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cyanogenmod.quickcover;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class IconUtils {
    private static final String TAG = "IconUtils";
    private static boolean D = true;

    public static Bitmap getOverlaidBitmap(Resources res, int resId, int color) {
        return getOverlaidBitmap(res, resId, color, 0);
    }

    public static Bitmap getOverlaidBitmap(Resources res, int resId, int color, int density) {
        Bitmap src = getBitmapFromResource(res, resId, density);
        if (color == 0 || src == null) {
            return src;
        }

        final Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(dest);
        final Paint paint = new Paint();

        // Overlay the selected color and set the imageview
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        c.drawBitmap(src, 0, 0, paint);
        return dest;
    }

    public static Bitmap getBitmapFromResource(Resources res, int resId, int density) {
        if (density == 0) {
            if (D) Log.d(TAG, "Decoding resource id = " + resId + " for default density");
            return BitmapFactory.decodeResource(res, resId);
        }

        if (D) Log.d(TAG, "Decoding resource id = " + resId + " for density = " + density);
        Drawable d = res.getDrawableForDensity(resId, density);
        if (d instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) d;
            return bd.getBitmap();
        }

        Bitmap result = Bitmap.createBitmap(d.getIntrinsicWidth(),
                d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        d.setBounds(0, 0, result.getWidth(), result.getHeight());
        d.draw(canvas);
        canvas.setBitmap(null);

        return result;
    }

}
