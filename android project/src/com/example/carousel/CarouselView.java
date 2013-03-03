package com.example.carousel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * Please note: This View controls its own height because the way ViewPager works (http://stackoverflow.com/a/8532550/121654)
 * @author tomrenn
 *
 */
public class CarouselView extends ViewPager implements CarouselListener {
	private CarouselFetch fetcher;
	private CarouselAdapter adapter;
	private int numFeatures;
	private int featuresLoaded;
//	private CacheManager cache;
//	private ImageView view1;
	private static final int CAROUSEL_WIDTH = 528;
	private static final int CAROUSEL_HEIGHT = 220;
	
	public CarouselView(Context context) {
		super(context);
		numFeatures = 0;
		featuresLoaded = 0;
		this.setBackgroundColor(Color.BLACK);
		Resources r = context.getResources();
		int width = r.getDisplayMetrics().widthPixels;
		float ratio = (float) width / CAROUSEL_WIDTH;
		int height = 0;
		height = (int) (ratio * CAROUSEL_HEIGHT + 0.5);
		this.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, height));
		Log.d("Rowan Prototype", "Height of viewpager is " + height);
		System.out.println("Height of ViewPager is " + height);
		
		adapter = new CarouselAdapter();
		this.setAdapter(adapter);
		
		fetcher = new CarouselFetch(this, context);
		fetcher.execute();
	}

	/**
	 * Receive the list of fetched features
	 */
	@Override
	public void receiveFeatures(CarouselFeature[] features) {
		System.out.println("Received all features! -> " + features.length);
		numFeatures = features.length;
		adapter.updateFeatures(features);
		
		for (CarouselFeature feature : features)
			feature.loadImage();
	}

	/**
	 * Feature is fully loaded (Image downloaded or loaded from cache)
	 * Once all features are loaded, CarouselView is updated
	 */
	@Override
	public void doneLoading(final CarouselFeature feature) {
		System.out.println("Feature image loaded");
		featuresLoaded++;
		
		// update features when they have all loaded
		if (featuresLoaded == numFeatures){
			adapter.notifyDataSetChanged();
		}
	}
	
	/***************************************************
	 * CarouselAdapter
	 * Handles the data for the CarouselView Pager
	 */
	private class CarouselAdapter extends PagerAdapter {
		private CarouselView carouselPager;
		private CarouselFeature[] features;
		
		
		public CarouselAdapter() {
			final CarouselFeature defaultFeature = new CarouselFeature(CarouselView.this.getContext());
			carouselPager = CarouselView.this;
			features = new CarouselFeature[] { defaultFeature };
//			features = new CarouselFeature[0];
		}
		
		public void updateFeatures(CarouselFeature[] newFeatures){
			features = newFeatures;
		}
		
		@Override 
		public Object instantiateItem(ViewGroup container, int position) {
			System.out.println("instantiate item position " + position);
			View view = features[position].getView();
			container.addView(view);
			return view;
		}
		
		@Override
		public void destroyItem (ViewGroup container, int position, Object object) {
			System.out.println("destory item position " + position);
			container.removeView((View)object);
		}
		
		@Override
		public int getCount() {
			return features.length;
		}
		
		public int getItemPosition(Object object) {
			Object tag = ((View)object).getTag();
			if (tag != null && tag.equals(CarouselFeature.DEFAULT_FEATURE_TAG)){
				System.out.println("REMOVING DEFAULT VIEW");
				return POSITION_NONE;
			}
			else
				return super.getItemPosition(object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
	}

}
