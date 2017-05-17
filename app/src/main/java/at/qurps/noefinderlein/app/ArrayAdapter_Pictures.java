package at.qurps.noefinderlein.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import com.gjiazhe.scrollparallaximageview.ScrollParallaxImageView;
import com.gjiazhe.scrollparallaximageview.parallaxstyle.HorizontalMovingStyle;

import java.util.List;

/**
 * Created by roman on 17.04.17.
 */

public class ArrayAdapter_Pictures extends RecyclerView.Adapter<ArrayAdapter_Pictures.ViewHolder> {

    public static final String TAG = "ArrayAdapter_Pictures";
    private Context context;
    private List<CloudPicture> uploads;
    private int height;
    private ScrollParallaxImageView.ParallaxStyle parallaxStyle;


    public ArrayAdapter_Pictures(Context context, List<CloudPicture> uploads, int height) {
        this.uploads = uploads;
        this.context = context;
        this.height = height;
        this.parallaxStyle = new HorizontalMovingStyle();
    }
    public ArrayAdapter_Pictures(ScrollParallaxImageView.ParallaxStyle parallaxStyle) {
        this.parallaxStyle = parallaxStyle;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sliding_detail_images, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ViewHolder innerVH = holder;

        holder.imageView.setParallaxStyles(parallaxStyle);
        CloudPicture upload = uploads.get(position);
        final String photoreference = upload.getPhotoreference();
        final String locationName = upload.getLocationname();
        final String locationUrl = upload.getPictureurl();
        String url;
        if(locationUrl == null) {
            url = "https://maps.googleapis.com/maps/api/place/photo?maxheight=" + this.height + "&photoreference=" + photoreference + "&key=" + context.getString(R.string.google_photo_key);
        } else {
            url = locationUrl;
        }

        GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Referer", "https://noecard.reitschmied.at")
                .build());
        Glide.with(context)
            .load(glideUrl)
            .asBitmap()
            .into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                    //Bitmap googleBM = BitmapFactory.decodeResource(context.getResources(), R.mipmap.powered_by_google_on_non_white);
                    int width = (height* resource.getWidth())/resource.getHeight();
                    Bitmap newBit = Bitmap.createScaledBitmap(resource, width, height, false);
                    Drawable shape =  context.getResources().getDrawable(R.drawable.detailmenubackground);
                    Canvas c = new Canvas(newBit);
                    //c.drawBitmap(googleBM, ((newBit.getWidth()/2)-(googleBM.getWidth()/2)), (newBit.getHeight()-googleBM.getHeight()-50), null);
                    shape.setBounds( 0, 0, newBit.getWidth(), getActionBarHeight()*3 );
                    shape.draw(c);
                    innerVH.imageView.setImageBitmap(newBit);
                    innerVH.imageView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            Intent myIntent = new Intent(context, Activity_Picture.class);
                            myIntent.putExtra(Activity_Picture.ARG_LOCATION_NAME, locationName);
                            myIntent.putExtra(Activity_Picture.ARG_PICTURE_REFERENCE, photoreference);
                            if(locationUrl != null) {
                                myIntent.putExtra(Activity_Picture.ARG_LOCATIONURL, locationUrl);
                            }
                            context.startActivity(myIntent);
                        }
                    });

                }
            });//.into(holder.imageView);
    }
    public int getActionBarHeight() {
        final TypedArray ta = context.getTheme().obtainStyledAttributes(
                new int[] {android.R.attr.actionBarSize});
        int actionBarHeight = (int) ta.getDimension(0, 0);
        Log.d(TAG, String.valueOf(actionBarHeight));
        return actionBarHeight;
    }
    @Override
    public int getItemCount() {
        return uploads.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {


        public ScrollParallaxImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);


            imageView = (ScrollParallaxImageView) itemView.findViewById(R.id.img_sliding_single);
            imageView.getLayoutParams().height = height;
            imageView.requestLayout();

        }
    }
}
