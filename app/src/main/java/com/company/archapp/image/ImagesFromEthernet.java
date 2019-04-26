package com.company.archapp.image;

import android.content.Context;
import android.util.Log;
import com.chahinem.pageindicator.PageIndicator;
import com.company.archapp.image.img.ImageDownloader;
import com.google.android.gms.maps.model.LatLng;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;


/**
 * Download images from ethernet and paste them into discrete scroll view and insert as first element google map
 */
public class ImagesFromEthernet {

    private final static String TAG = "MyActivity";
    private ImageDownloader myImg;
    private String[] urls = new String[4];

    public String[] putNameOfLandmarkToImage(String name, final DiscreteScrollView discreteScrollView, final PageIndicator pageIndicator, final Context context, final LatLng latLng) {

        NetworkService.getInstance()
                .getImgApi()
                .getAPIImg(name.toLowerCase(), "de464d64876cca5b3adc9f9f2473d5e3082c3b2d7d356d27bdb6eb4007827252")
                .enqueue(new Callback<ImageDownloader>() {
                    @Override
                    public void onResponse(Call<ImageDownloader> call, Response<ImageDownloader> response) {
                        if (response.isSuccessful()) {
                            myImg = response.body();

                            for (int i = 0; i < urls.length; i++)
                                urls[i] = myImg.getResults()[i].getUrls().getRegular();

                            generateDataForDSV(urls, discreteScrollView, pageIndicator, context, latLng);
                        } else {
                            Log.d(TAG, "Query error");
                        }
                    }

                    @Override
                    public void onFailure(Call<ImageDownloader> call, Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }
                });
        return urls;
    }

    private void generateDataForDSV(String[] urls, DiscreteScrollView discreteScrollView, PageIndicator pageIndicator, Context context, LatLng latLng) {

        List<LandmarkContentItem> landmarkContentItems = new ArrayList<>();

        landmarkContentItems.add(new LandmarkContentItem(latLng));
        for (String url : urls)
            landmarkContentItems.add(new LandmarkContentItem(url));

        LandmarkContentAdapter adapter = new LandmarkContentAdapter(landmarkContentItems, context);
        discreteScrollView.setAdapter(adapter);

        pageIndicator.attachTo(discreteScrollView);
    }
}
