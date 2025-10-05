package com.example.facelearn.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

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


    public static Bitmap imageToBitmap(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    public Bitmap convertImageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        // 変換するBitmapオブジェクトを作成
        Bitmap bitmap = Bitmap.createBitmap(
                // image.getWidth() + rowPadding / pixelStride,
                image.getWidth(),
                image.getHeight(),
                Bitmap.Config.RGB_565 // Bitmap.Config.ARGB_8888
        );

        // ByteBufferからBitmapにピクセルデータをコピー
        bitmap.copyPixelsFromBuffer(buffer);

        return bitmap;
    }

    public static void saveJpegFile(Bitmap bitmap, String filename) {
        // 保存するファイルパス
        File file = new File(filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // JPEGフォーマットで保存、品質は80
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
