package com.hangout.core.post_api.config;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

import org.geolatte.geom.G2D;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.GeolatteGeomModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class ObjectMapperConfig {
    @Bean
    Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        CoordinateReferenceSystem<G2D> crs = WGS84;
        return builder -> builder
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .modules(new GeolatteGeomModule(crs), new JavaTimeModule());
    }
}
