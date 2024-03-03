package rs.djokafioka.babydoodle.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created by Djole on 04.01.2024..
 */
public class DrawViewModel extends ViewModel {
    private MutableLiveData<CharSequence> mBase64Img = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsEraseModeOn = new MutableLiveData<>();

    public void setBase64Img(CharSequence base64Img)
    {
        mBase64Img.setValue(base64Img);
    }

    public LiveData<CharSequence> getBase64Img()
    {
        return mBase64Img;
    }

    public LiveData<Boolean> getIsEraseModeOn() {
        return mIsEraseModeOn;
    }

    public void setIsEraseModeOn(boolean isEraseModeOn) {
        mIsEraseModeOn.setValue(isEraseModeOn);
    }
}
