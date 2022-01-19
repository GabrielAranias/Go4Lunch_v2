package com.gabriel.aranias.go4lunch_v2.utils;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.model.Place;

import java.util.ArrayList;
import java.util.Arrays;

public interface PlaceUtils {

    ArrayList<Place> placeTypes = new ArrayList<>(
            Arrays.asList(
                    new Place(1, R.drawable.ic_baseline_restaurant_24, "Restaurant", "restaurant"),
                    new Place(2, R.drawable.ic_baseline_shopping_cart_24, "Groceries", "supermarket"),
                    new Place(3, R.drawable.ic_baseline_bakery_dining_24, "Bakery", "bakery"),
                    new Place(4, R.drawable.ic_baseline_local_cafe_24, "Cafe", "cafe")
            )
    );

}
