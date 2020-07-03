package com.hapi.memotrancer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.hapi.baseprofile.ActivityCollection;
import com.hapi.baseprofile.NotificationHelper;
import com.taobao.android.dexposed.XC_MethodHook;

public class ImageHook extends XC_MethodHook {

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        // 1
        ImageView imageView = (ImageView) param.thisObject;
        checkBitmap(imageView,((ImageView) param.thisObject).getDrawable());
    }


    private static void checkBitmap(Object thiz, Drawable drawable) {
        if (drawable instanceof BitmapDrawable && thiz instanceof View) {
            final Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                final View view = (View) thiz;
                int width = view.getWidth();
                int height = view.getHeight();
                if (width > 0 && height > 0) {
                    // 2、图标宽高都大于view的2倍以上，则警告
                    if (bitmap.getWidth() >= (width << 1)
                            &&  bitmap.getHeight() >= (height << 1)) {
                        warn(bitmap.getWidth(), bitmap.getHeight(), width, height, new RuntimeException("Bitmap size too large"));
                    }
                } else {
                    // 3、当宽高度等于0时，说明ImageView还没有进行绘制，使用ViewTreeObserver进行大图检测的处理。
                    final Throwable stackTrace = new RuntimeException();
                    view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            int w = view.getWidth();
                            int h = view.getHeight();
                            if (w > 0 && h > 0) {
                                if (bitmap.getWidth() >= (w << 1)
                                        && bitmap.getHeight() >= (h << 1)) {
                                    warn(bitmap.getWidth(), bitmap.getHeight(), w, h, stackTrace);
                                }
                                view.getViewTreeObserver().removeOnPreDrawListener(this);
                            }
                            return true;
                        }
                    });
                }
            }
        }
    }

    private static void warn(int bitmapWidth, int bitmapHeight, int viewWidth, int viewHeight, Throwable t) {
        String warnInfo = "Bitmap size too large: " +
                "\n real size: (" + bitmapWidth + ',' + bitmapHeight + ')' +
                "\n desired size: (" + viewWidth + ',' + viewHeight + ')' +
                "\n call stack trace: \n" + Log.getStackTraceString(t) + '\n';

//        LogHelper.i(warnInfo);
        ImgIssure issure= new ImgIssure();
        issure.setBitmapWidth(bitmapWidth);
        issure.setBitmapHeight(bitmapHeight);
        issure.setStack(Log.getStackTraceString(t) );
        NotificationHelper.INSTANCE.send(issure);
    }
}


