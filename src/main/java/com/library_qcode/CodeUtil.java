package com.library_qcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * Created by Administrator on 2015/9/30.
 */
public class CodeUtil {
    public static int LOGO_HALFWIDTH = 18;

    private CodeUtil() {
        throw new UnsupportedOperationException("cannot instanced");
    }

    /**
     * 将指定的内容生成成二维码
     *
     * @param content 将要生成二维码的内容
     * @return 返回生成好的二维码事件
     * @throws WriterException WriterException异常
     */
    private static Bitmap createNoLogoTwoDCode(Context context, String content, BarcodeFormat format) throws WriterException {
        // 用于设置QR二维码参数
        Hashtable<EncodeHintType, Object> qrParam = new Hashtable<EncodeHintType, Object>();
        // 设置QR二维码的纠错级别——这里选择最高H级别
        qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 设置编码方式
        qrParam.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        int size = context.getResources().getDisplayMetrics().widthPixels / 2;
        if (size < 400)
            size = 400;
        BitMatrix matrix = new MultiFormatWriter().encode(content,
                format, size, size, qrParam);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩阵转为一维像素数组,也就是一直横着排了
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 用于将给定的内容生成成一维码 注：目前生成内容为中文的话将直接报错，要修改底层jar包的内容
     *
     * @param content 将要生成一维码的内容
     * @return 返回生成好的一维码bitmap
     * @throws WriterException WriterException异常
     */
    public static Bitmap createOneDCode(Context context, String content, BarcodeFormat format) throws WriterException {
        // 用于设置QR二维码参数
        Hashtable<EncodeHintType, Object> qrParam = new Hashtable<EncodeHintType, Object>();
        // 设置QR二维码的纠错级别——这里选择最高H级别
        qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 设置编码方式
        qrParam.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 生成一维条码,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        int size = context.getResources().getDisplayMetrics().widthPixels / 3 * 2;
        if (size < 500) {
            size = 500;
        }
        if (format == null)
            format = BarcodeFormat.CODE_128;

        BitMatrix matrix = new MultiFormatWriter().encode(content,
                format, size, size / 2, qrParam);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    /**
     * 生成二维码
     *
     * @param content 二维码中包含的文本信息
     * @param logo    logo图片
     * @param format  编码格式
     * @return Bitmap 位图
     * @throws WriterException
     */
    private static Bitmap createLogoCode(Context context, String content, Bitmap logo, BarcodeFormat format)
            throws WriterException {
        DisplayMetrics out = context.getResources().getDisplayMetrics();
        int IMAGE_HALFWIDTH = (int) out.density * LOGO_HALFWIDTH;
        //二维码的大小
        int size = out.widthPixels / 2;
        Matrix m = new Matrix();
        float sx = (float) 2 * IMAGE_HALFWIDTH / logo.getWidth();
        float sy = (float) 2 * IMAGE_HALFWIDTH / logo.getHeight();
        m.setScale(sx, sy);//设置缩放信息
        //将logo图片按martix设置的信息缩放
        logo = Bitmap.createBitmap(logo, 0, 0,
                logo.getWidth(), logo.getHeight(), m, false);
        // 用于设置QR二维码参数
        Hashtable<EncodeHintType, Object> hst = new Hashtable<EncodeHintType, Object>();
        hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");//设置字符编码
        // 设置QR二维码的纠错级别——这里选择最高H级别
        hst.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        BitMatrix matrix = new MultiFormatWriter().encode(content, format, size, size, hst);//生成二维码矩阵信息
        int width = matrix.getWidth();//矩阵高度
        int height = matrix.getHeight();//矩阵宽度
        int halfW = width / 2;
        int halfH = height / 2;
        int[] pixels = new int[width * height];//定义数组长度为矩阵高度*矩阵宽度，用于记录矩阵中像素信息
        for (int y = 0; y < height; y++) {//从行开始迭代矩阵
            for (int x = 0; x < width; x++) {//迭代列
                if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
                        && y > halfH - IMAGE_HALFWIDTH
                        && y < halfH + IMAGE_HALFWIDTH) {//该位置用于存放图片信息
//记录图片每个像素信息
                    pixels[y * width + x] = logo.getPixel(x - halfW
                            + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                } else {
                    if (matrix.get(x, y)) {//如果有黑块点，记录信息
                        pixels[y * width + x] = 0xff000000;//记录黑块信息
                    }
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 生成二维码
     *
     * @param context 上下文环境
     * @param content 二维码内容
     * @param logo    二维码logo（可以为空)
     * @param format  二维码格式（可以为空，默认为BarcodeFormat.QR_CODE)
     * @return
     * @throws WriterException
     */
    public static Bitmap createTwoCode(Context context, String content, Bitmap logo, BarcodeFormat format) throws WriterException {
        if (format == null)
            format = BarcodeFormat.QR_CODE;
        if (logo == null) {
            return createNoLogoTwoDCode(context, content, format);
        } else {
            return createLogoCode(context, content, logo, format);
        }
    }

}
