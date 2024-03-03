package rs.djokafioka.babydoodle.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Djole on 04.01.2024..
 */
public class BitmapUtility
{
    private static final int TARGET_PHOTO_WIDTH = 640;
    private static final int TARGET_PHOTO_HEIGHT = 320;

    public static Bitmap decodeFromBase64(String base64Bitmap) {
        byte[] bitmapBytes = Base64.decode(base64Bitmap, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);
    }

    public static String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean decodeFromBase64ToFile(@NonNull String base64Bitmap, File destinationFile) {
        Bitmap bitmap = BitmapUtility.decodeFromBase64(base64Bitmap);
        if (bitmap != null) {
            if (destinationFile.exists()) {
                destinationFile.delete();
            }
            FileOutputStream slikaFileOutpusStream = null;
            try {
                slikaFileOutpusStream = new FileOutputStream(destinationFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, slikaFileOutpusStream);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (slikaFileOutpusStream != null) {
                    try {
                        slikaFileOutpusStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            return false;
        }
    }

    public static Bitmap getBitmapFromFile(String fileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(fileName, options);
    }

    public static Bitmap getScaledBitmapFromFile(String fileName) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, bmOptions);
        int photoWidth = bmOptions.outWidth;
        int photoHeight = bmOptions.outHeight;

        int scaleFactor = Math.min(photoWidth/TARGET_PHOTO_WIDTH, photoHeight/TARGET_PHOTO_HEIGHT);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(fileName, bmOptions);
    }

    public static String getScaledBitmapBase64(String fileName) {
        return encodeToBase64(getScaledBitmapFromFile(fileName));
    }

    @NonNull
    public static Drawable tint(@NonNull Drawable original, @ColorInt int tintColor) {
        Drawable wrapDrawable = DrawableCompat.wrap(original.mutate());
        DrawableCompat.setTint(wrapDrawable, tintColor);
        return wrapDrawable;
    }
}
