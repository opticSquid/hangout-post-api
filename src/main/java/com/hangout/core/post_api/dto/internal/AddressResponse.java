package com.hangout.core.post_api.dto.internal;

import java.util.List;

public record AddressResponse(String type, List<AddressFeatures> features, AddressQuery query) {
}

record AddressQuery(Double lat, Double lon, String plusCode) {

}

record AddressGeometry(String type, List<Double> coordinates) {

}