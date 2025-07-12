package com.hangout.core.post_api.dto.internal;

import java.util.List;

public record AddressFeatures(String type, AddressProperties properties, AddressGeometry geometry, List<Double> bbox) {

}
