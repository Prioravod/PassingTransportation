package ru.petrovpavel.passingtransportation.geocode;

import java.util.List;

public interface IPlaceListener {

    void onPlaceLocated(IMapPlace place);

    void onPlacesLocated(List<IMapPlace> place);

    void onPlaceLocationFailed(Exception reason);
}
