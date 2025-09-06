package com.example.facelearn;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class ImageUtil {
    private byte[] cropImage(Bitmap bitmap , int x, int y, int w, int h) {
        // ref: https://qiita.com/Nao9syu/items/b0cdefec14c3bcc67647
        int quality = 75;

        Bitmap bitmapFinal= Bitmap.createBitmap(
                bitmap,
                x, y, w, h
        );
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmapFinal.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                stream
        );
        return stream.toByteArray();
    }
}
