package com.library_qcode;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2015/9/30.
 */
public interface OnScanCodeResultListener {
    void handleDecode(String resultStr,Bitmap barcode);
}
