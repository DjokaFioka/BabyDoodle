package rs.djokafioka.babydoodle.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import rs.djokafioka.babydoodle.R;

/**
 * Created by Djole on 04.01.2024..
 */
public class DrawFragment extends Fragment
{
    private DrawViewModel mViewModel;
    private String mBase64Img;
    private boolean mIsEraseModeOn;
    private ImageButton mImgDraw;
    private ImageButton mImgErase;
    private ImageButton mImgClearAll;
    private ImageButton mImgShare;

    public static DrawFragment newInstance() {
        return new DrawFragment();
    }

    private FingerpaintView mFingerpaintView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_draw, container, false);

        mFingerpaintView = (FingerpaintView) v.findViewById(R.id.draw_view);

        mImgDraw = v.findViewById(R.id.img_draw);
        mImgErase = v.findViewById(R.id.img_erase);
        mImgClearAll = (ImageButton) v.findViewById(R.id.img_clear_all);
        mImgShare = v.findViewById(R.id.img_share);

        mImgDraw.setOnClickListener(onClick -> {
            mFingerpaintView.runDrawMode();
            mIsEraseModeOn = false;
            mImgErase.setBackground(AppCompatResources.getDrawable(mImgErase.getContext(), R.drawable.ic_erase));
            mImgDraw.setBackground(AppCompatResources.getDrawable(mImgDraw.getContext(), R.drawable.ic_edit_checked));
        });

        mImgErase.setOnClickListener(onClick -> {
            mFingerpaintView.runEraseMode();
            mIsEraseModeOn = true;
            mImgErase.setBackground(AppCompatResources.getDrawable(mImgErase.getContext(), R.drawable.ic_erase_checked));
            mImgDraw.setBackground(AppCompatResources.getDrawable(mImgDraw.getContext(), R.drawable.ic_edit));
        });

        mImgClearAll.setOnClickListener(onClick -> {
            mFingerpaintView.clearAll();
            mImgDraw.callOnClick();
        });

        mImgShare.setOnClickListener(onClick -> {
            takeScreenshot();
        });

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DrawViewModel.class);
        mViewModel.getBase64Img().observe(getViewLifecycleOwner(), charSequence -> {
            mBase64Img = (charSequence == null ? "" : charSequence.toString());
        });
        if (!TextUtils.isEmpty(mBase64Img)) {
            mFingerpaintView.setBase64EncodedBitmap(mBase64Img);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mImgDraw.callOnClick();
        mViewModel.getIsEraseModeOn().observe(getViewLifecycleOwner(), isOn -> {
            if (isOn)
                mImgErase.callOnClick();
            else
                mImgDraw.callOnClick();
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mViewModel.setBase64Img(mFingerpaintView.getBase64EncodedBitmap());
        mViewModel.setIsEraseModeOn(mIsEraseModeOn);
    }

    private void takeScreenshot()
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            try
            {
                // create bitmap screen capture
                View v1 = getActivity().getWindow().getDecorView().getRootView();
                v1.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                v1.setDrawingCacheEnabled(false);

                // Create an image file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "Doodle_" + timeStamp;
                File storageDir = new File(getContext().getFilesDir(), "Pictures");
                storageDir.mkdirs();
                File imageFile = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );

                FileOutputStream outputStream = new FileOutputStream(imageFile);
                int quality = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.flush();
                outputStream.close();

                shareScreenshot(imageFile);


            }
            catch (Throwable e)
            {
                // Several error may come out with file handling or DOM
                e.printStackTrace();
            }
        }, 200);
    }

    private void shareScreenshot(File imageFile)
    {
        Uri photoURI = FileProvider.getUriForFile(getContext(),
                getString(R.string.authority_photo_fileprovider),
                imageFile);

        Intent slanjeIntent = new Intent(Intent.ACTION_SEND);
        slanjeIntent.putExtra(Intent.EXTRA_STREAM, photoURI);

        slanjeIntent.setType("image/*");
        startActivity(Intent.createChooser(slanjeIntent, getString(R.string.select_app_to_share)));
    }
}
