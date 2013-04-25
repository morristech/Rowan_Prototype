package edu.rowan.app.carousel;


public interface CarouselListener {

	abstract public void receiveFeatures(CarouselFeature[] features);
	abstract public void doneLoading(CarouselFeature feature);
}
