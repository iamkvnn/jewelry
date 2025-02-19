package com.web.jewelry.service.feature;

import com.web.jewelry.model.Feature;

import java.util.List;
import java.util.Optional;

public interface IFeatureService {
    List<Feature> getAllFeatures();
    Feature getFeatureById(Long id);
    Optional<Feature> getFeatureByName(String name);
    Feature addFeature(String name);
    Feature updateFeature(Long id, Feature feature);
    void deleteFeature(Long id);
}
