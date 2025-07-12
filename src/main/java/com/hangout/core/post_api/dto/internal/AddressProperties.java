package com.hangout.core.post_api.dto.internal;

public record AddressProperties(
        AddressDataSource dataSource, String name, String country, String country_code, String state,
        String city, String postcode, String district, String suburb, String street, String housenumber,
        String iso3166_2, Double lon, Double lat, String stateCode, Integer distance, String resultType,
        String formatted, String addressLine1, String addressLine2, String category, AddressTimeZone timeZone,
        String plusCode, String plusCodeShort, AddressRank rank, String placeId) {

}

record AddressDataSource(String sourcename, String attribution, String license, String url) {
};

record AddressTimeZone(
        String name, String offsetSTD, Integer offsetSTDSeconds, String offsetDST, Integer offsetDSTSeconds,
        String abbreviationSTD, String abbreviationDST) {
}

record AddressRank(Double importance, Double polularity) {

}