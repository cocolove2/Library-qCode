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

package com.mining.app.zxing.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.library_qcode.Contants;
import com.mining.app.zxing.camera.CameraManager;

import java.util.Collection;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 */

public final class ViewfinderView extends View {
    private static final String TAG = "log";
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
    private static final int CORNER_WIDTH = 5;
    /**
     * 扫描框中的中间线的宽度
     */
    private static final int MIDDLE_LINE_WIDTH = 3;

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
    private static final int TEXT_SIZE = 14;
    /**
     * 字体距离扫描框下面的距离
     */
    private static final int TEXT_PADDING_TOP = 25;

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
    //获取屏幕的宽和高
    final int width;
    final int height;

    /**
     * 将扫描的二维码拍下来，这里没有这个功能，暂时不考虑
     */
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;

    private final int resultPointColor;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;

    boolean isFirst;
    Rect textBound;
    final int START_SCAN_Y;//起始扫描的Y坐标
    Rect frame;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        maskColor = Color.parseColor(Contants.COLOR_VIEWFINDER_MASK);
        resultColor = Color.parseColor(Contants.COLOR_RESULT_VIEW);
        resultPointColor = Color.parseColor(Contants.COLOR_POSSIBLE_RESULT_POINTS);
        DisplayMetrics out = context.getResources().getDisplayMetrics();
        density = out.density;
        width = out.widthPixels;
        height = out.heightPixels;
        START_SCAN_Y = height / 5;
        initViewFinder(context);
    }


    void initViewFinder(Context context) {
        //将像素转换成dp
        ScreenRate = (int) (20 * density);
        possibleResultPoints = new HashSet<ResultPoint>(5);
        textBound = new Rect();
        paint = new Paint();
        slideTop = START_SCAN_Y;

    }

    @Override
    public void onDraw(Canvas canvas) {
        //中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        if (!isFirst) {
            isFirst=true;
            slideBottom = START_SCAN_Y + frame.width();
        }
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        //绘制扫描区域外的阴影
        drawScanArea(canvas);
        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            //绘制扫描区域的四角
            drawArc(canvas);
            //绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
            slideTop += SPEEN_DISTANCE;
            if (slideTop >= slideBottom) {
                slideTop = START_SCAN_Y;
            }
            canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH / 2, frame.right - MIDDLE_LINE_PADDING, slideTop + MIDDLE_LINE_WIDTH / 2, paint);
            //画扫描框下面的字
            drawText(canvas);

            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<ResultPoint>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frame.left + point.getX(), START_SCAN_Y
                            + point.getY(), 6.0f, paint);
                }
            }
            if (currentLast != null) {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frame.left + point.getX(), START_SCAN_Y
                            + point.getY(), 3.0f, paint);
                }
            }
            //只刷新扫描框的内容，其他地方不刷新
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, START_SCAN_Y,
                    frame.right, slideBottom);
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

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

    //画出扫描区域
    private void drawScanArea(Canvas canvas) {
        canvas.drawRect(0, 0, width, START_SCAN_Y, paint);//上边阴影
        canvas.drawRect(0, START_SCAN_Y, frame.left, slideBottom+1, paint);//左边
        canvas.drawRect(frame.right, START_SCAN_Y, width, slideBottom+1, paint);//右边
        canvas.drawRect(0, slideBottom+1, width, height, paint);//下边
    }

    //画扫描框下面的字
    private void drawText(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setTextSize(TEXT_SIZE * density);
        paint.setAlpha(0xee);
        paint.setTypeface(Typeface.create("System", Typeface.BOLD));
        paint.getTextBounds(Contants.TEXT_SCAN_TIP, 0, Contants.TEXT_SCAN_TIP.length(), textBound);
        canvas.drawText(Contants.TEXT_SCAN_TIP, (width - textBound.width()) / 2, (float) (slideBottom + (float) TEXT_PADDING_TOP * density), paint);
    }

    private void drawArc(Canvas canvas) {
        //画扫描框边上的角，总共8个部分
        paint.setColor(Color.GREEN);
        canvas.drawRect(frame.left, START_SCAN_Y, frame.left + ScreenRate,
                START_SCAN_Y + CORNER_WIDTH, paint);
        canvas.drawRect(frame.left, START_SCAN_Y, frame.left + CORNER_WIDTH, START_SCAN_Y
                + ScreenRate, paint);
        canvas.drawRect(frame.right - ScreenRate, START_SCAN_Y, frame.right,
                START_SCAN_Y+ CORNER_WIDTH, paint);
        canvas.drawRect(frame.right - CORNER_WIDTH, START_SCAN_Y, frame.right, START_SCAN_Y
                + ScreenRate, paint);
        canvas.drawRect(frame.left, slideBottom - CORNER_WIDTH, frame.left
                + ScreenRate, slideBottom, paint);
        canvas.drawRect(frame.left, slideBottom - ScreenRate,
                frame.left + CORNER_WIDTH, slideBottom, paint);
        canvas.drawRect(frame.right - ScreenRate, slideBottom - CORNER_WIDTH,
                frame.right, slideBottom, paint);
        canvas.drawRect(frame.right - CORNER_WIDTH,slideBottom - ScreenRate,
                frame.right, slideBottom, paint);

    }
}
