package rs.djokafioka.babydoodle.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

}
