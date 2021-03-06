/*
 * Copyright (C) 2008 ZXing authors
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

package com.yihao.test.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.yihao.test.R;
import com.yihao.test.camera.CameraManager;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 */
public final class ViewfinderView extends View {
    /**
     * 刷新界面的时间
     */
    private static final long ANIMATION_DELAY = 10L;
    private static final int OPAQUE = 0xFF;

    /**
     * 四个绿色边角对应的长度
     */
    private int ScreenRate;

    /**
     * 四个绿色边角对应的宽度
     */
    private static final int CORNER_WIDTH = 8;
    /**
     * 扫描框中的中间线的宽度
     */
    private static final int MIDDLE_LINE_WIDTH = 6;

    /**
     * 扫描框中的中间线的与扫描框左右的间隙
     */
    private static final int MIDDLE_LINE_PADDING = 5;

    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 5;

    /**
     * 手机的屏幕密度
     */
    private static float density;
    /**
     * 字体大小
     */
    private static final int TEXT_SIZE = 16;

    /**
     * 字体大小
     */
    private static final int TEXT_LARGE_SIZE = 20;

    /**
     * 字体距离扫描框下面的距离
     */
    private static final int TEXT_PADDING_TOP = 30;

    /**
     * 画笔对象的引用
     */
    private Paint paint;

    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;

    /**
     * 中间滑动线的最底端位置
     */
    private int slideBottom;

    // 顶部的字体内容
    private String mTopText;

    /**
     * 将扫描的二维码拍下来，这里没有这个功能，暂时不考虑
     */
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;


    boolean isFirst;

    public ViewfinderView(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        // 将像素转换成dp
        ScreenRate = (int) (15 * density);
        paint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
    }


    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        // 将像素转换成dp
        ScreenRate = (int) (15 * density);
        paint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
    }

    public void setTopText(String topText) {
        if (topText != null && topText.trim().length() > 0) {
            this.mTopText = "请扫描" + topText + "二维码";
        }
//        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // 中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        // 初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top;
            slideBottom = frame.bottom;
        }
        // 获取屏幕的宽和高
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        // 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
        // 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            // 绘制扫描框边上的角，总共8个部分
            paint.setColor(Color.GREEN);
            canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
                    frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top
                    + ScreenRate, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
                    frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top
                    + ScreenRate, paint);
            canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
                    + ScreenRate, frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - ScreenRate,
                    frame.left + CORNER_WIDTH, frame.bottom, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH,
                    frame.right, frame.bottom, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate,
                    frame.right, frame.bottom, paint);
            // 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
            slideTop += SPEEN_DISTANCE;
            if (slideTop >= frame.bottom) {
                slideTop = frame.top;
            }

            // 绘制扫描框边线
            paint.setColor(Color.WHITE);
            canvas.drawLine(frame.left + ScreenRate, frame.top + CORNER_WIDTH / 2, frame.right - ScreenRate, frame.top + CORNER_WIDTH / 2, paint);
            canvas.drawLine(frame.right - CORNER_WIDTH / 2, frame.top + ScreenRate, frame.right - CORNER_WIDTH / 2, frame.bottom - ScreenRate, paint);
            canvas.drawLine(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH / 2, frame.left + ScreenRate, frame.bottom - CORNER_WIDTH / 2, paint);
            canvas.drawLine(frame.left + CORNER_WIDTH / 2, frame.bottom - ScreenRate, frame.left + CORNER_WIDTH / 2, frame.top + ScreenRate, paint);


            Rect lineRect = new Rect();
            int xpadding = 10;
            lineRect.left = frame.left + xpadding;
            lineRect.right = frame.right - xpadding;
            lineRect.top = slideTop;
            lineRect.bottom = slideTop + 5;
            //canvas.drawBitmap(((BitmapDrawable) (getResources().getDrawable(R.drawable.scan_line))).getBitmap(), null, lineRect, paint);
            paint.setColor(Color.GREEN);
            canvas.drawRect(lineRect, paint);

            //画出扫描框上面的字体
            if (!TextUtils.isEmpty(mTopText)) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(TEXT_SIZE * density);
                float textWidthTop = paint.measureText(mTopText);
                canvas.drawText(mTopText, (width - textWidthTop) / 2, frame.top - (float) TEXT_PADDING_TOP * density, paint);
            }

            //画扫描框下面的字
            paint.setColor(Color.WHITE);
            paint.setTextSize(TEXT_SIZE * density);
//            paint.setAlpha(0xFF);
//            paint.setTypeface(Typeface.create("System", Typeface.BOLD));
            String text = getResources().getString(R.string.scan_text1);
            float textWidth = paint.measureText(text);
            canvas.drawText(text, (width - textWidth) / 2, frame.bottom + (float) TEXT_PADDING_TOP * density, paint);
            text = getResources().getString(R.string.scan_text2);
            textWidth = paint.measureText(text);
            paint.setColor(Color.GREEN);
            paint.setTextSize(TEXT_LARGE_SIZE * density);
            canvas.drawText(text, (width - textWidth) / 2, frame.bottom + (float) TEXT_PADDING_TOP * density + 2 * TEXT_SIZE * density, paint);
            //只刷新扫描框的内容，其他地方不刷新
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                    frame.right, frame.bottom);
        }
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }


}
