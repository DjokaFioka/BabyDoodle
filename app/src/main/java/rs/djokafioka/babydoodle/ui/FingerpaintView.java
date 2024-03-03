package rs.djokafioka.babydoodle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import rs.djokafioka.babydoodle.utils.BitmapUtility;

/**
 * Created by Djole on 04.01.2024..
 */
public class FingerpaintView extends View
{
    private static final float TOUCH_TOLERANCE = 4;

    private float mX, mY;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    private Path mPath;
    private Paint mBitmapPaint;

    private boolean mIsEnabled;

    private File mSaveInstanceTmpBitmapFile;
    private boolean mIsChanged;

    public FingerpaintView(Context context) {
        this(context,null,0);
    }

    public FingerpaintView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public FingerpaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mSaveInstanceTmpBitmapFile = new File(context.getFilesDir().getAbsolutePath() + File.separator + "tmpBitmap.png");
    }

    public String getBase64EncodedBitmap() {
        if (isEmpty()) {
            return null;
        }
        return BitmapUtility.encodeToBase64(mBitmap);
    }

    public void setBase64EncodedBitmap(String base64EncodedBitmap) {
        mBitmap = BitmapUtility.decodeFromBase64(base64EncodedBitmap);
    }

    public void runEraseMode() {
        mIsEnabled = true;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mPaint.setStrokeWidth(36);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void runDrawMode() {
        mIsEnabled = true;
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mPaint.setXfermode(null);
        mPaint.setStrokeWidth(12);
        mPaint.setColor(Color.BLACK);
    }

    public void clearAll() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
        mIsChanged = true;
        mIsEnabled = false;
        runDrawMode();
    }

    public boolean isEmpty() {
        Bitmap emptyBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap.getConfig());
        return mBitmap.sameAs(emptyBitmap);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBitmap != null) {
            mBitmap = Bitmap.createScaledBitmap(mBitmap, w, h, true);
        } else {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        mCanvas = new Canvas(mBitmap);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    private void touch_start(float x, float y) {
        getParent().requestDisallowInterceptTouchEvent(true);
        mIsChanged = true;
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }
    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        mPath.reset();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsEnabled) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public boolean isChanged() {
        return mIsChanged;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mSaveInstanceTmpBitmapFile);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new SavedState(super.onSaveInstanceState(), mIsChanged, mIsEnabled);

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(((SavedState)state).getSuperState());
        if (mSaveInstanceTmpBitmapFile.exists()) {
            mBitmap = BitmapFactory.decodeFile(mSaveInstanceTmpBitmapFile.getAbsolutePath());
            invalidate();
            mSaveInstanceTmpBitmapFile.delete();
        }
        SavedState savedState = (SavedState) state;
        mIsChanged = savedState.isChanged();
        mIsEnabled = savedState.isEnabled();
    }

    protected static class SavedState extends BaseSavedState {

        private final boolean mIsChanged;
        private final boolean mIsEnabled;

        SavedState(Parcelable superState, boolean isChanged, boolean isEnabled) {
            super(superState);
            mIsChanged = isChanged;
            mIsEnabled = isEnabled;
        }

        SavedState(Parcel source) {
            super(source);
            mIsChanged = source.readByte() != 0;
            mIsEnabled = source.readByte() != 0;
        }

        boolean isChanged() {return mIsChanged;}

        boolean isEnabled() {
            return mIsEnabled;
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {

            super.writeToParcel(destination, flags);
            destination.writeByte(mIsChanged ? (byte)1 : 0);
            destination.writeByte(mIsEnabled? (byte)1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        };
    }
}
