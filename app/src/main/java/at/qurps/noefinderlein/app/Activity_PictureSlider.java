package at.qurps.noefinderlein.app;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.BaseSliderView;
import com.glide.slider.library.SliderTypes.TextSliderView;
import com.glide.slider.library.Tricks.ViewPagerEx;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 23.02.18.
 */

public class Activity_PictureSlider extends AppCompatActivity
        implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener {

    private SliderLayout mSlider;
    private Bundle data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = getIntent().getExtras();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_pictureslider);
        mSlider = findViewById(R.id.slider);
        ArrayList<CloudPicture> upList = data.getParcelableArrayList("upList");
        int position = data.getInt("position");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        int uiOptions = this.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            //HyperLog.i(TAG, "Turning immersive mode mode off. ");
        } else {
            //HyperLog.i(TAG, "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        this.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

        RequestOptions requestOptions = new RequestOptions();
        //requestOptions
        //        .centerCrop();
        //.diskCacheStrategy(DiskCacheStrategy.NONE)
        //.placeholder(R.drawable.placeholder)
        //.error(R.drawable.placeholder);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;

        for (int i = 0; i < upList.size(); i++) {
            CloudPicture cp = upList.get(i);
            TextSliderView sliderView = new TextSliderView(this);
            // if you want show image only / without description text use DefaultSliderView instead

            String url;
            String locationurl = cp.getPictureurl();
            String photoreference = cp.getPhotoreference();

            if(locationurl == null) {
                url = "https://maps.googleapis.com/maps/api/place/photo?maxheight=" + String.valueOf(height) + "&photoreference=" + photoreference + "&key=" + getApplicationContext().getString(R.string.google_photo_key);
                sliderView.description("powered by Google");
            } else {
                url = locationurl;

                /*ImageView iv = (ImageView) findViewById(R.id.poweredbygoogle);
                iv.setVisibility(View.GONE);*/
            }

            // initialize SliderLayout
            sliderView.image(url);
            /*if (!cp.getPhotoreference().equals("")) {
                sliderView.description(cp.getPhotoreference());
            }*/
            sliderView.setRequestOption(requestOptions);
            sliderView.setBackgroundColor(R.color.picture_background);
            sliderView.setProgressBarVisible(true);
            sliderView.setOnSliderClickListener(this);

            //add your extra information
            //sliderView.bundle(new Bundle());
            //sliderView.getBundle().putString("extra", listName.get(i));
            mSlider.addSlider(sliderView);
        }
        mSlider.setCurrentPosition(position);
        // set Slider Transition Animation
        mSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        //mSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);

        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        //mSlider.setCustomAnimation(new DescriptionAnimation());
        //mSlider.setDuration(4000);
        mSlider.addOnPageChangeListener(this);
        mSlider.stopAutoCycle();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        mSlider.stopAutoCycle();
        mSlider.removeAllSliders();
    }


    @Override
    public void onSliderClick(BaseSliderView slider) {
        //Toast.makeText(this, slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
