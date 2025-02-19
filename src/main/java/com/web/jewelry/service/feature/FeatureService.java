package com.web.jewelry.service.feature;

import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Feature;
import com.web.jewelry.repository.FeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FeatureService implements IFeatureService {
    private final FeatureRepository featureRepository;

    @Override
    public List<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    @Override
    public Feature getFeatureById(Long id) {
        return featureRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Feature not found"));
    }

    @Override
    public Optional<Feature> getFeatureByName(String name) {
        return Optional.ofNullable(featureRepository.findByName(name));
    }

    @Override
    public Feature addFeature(String name) {
        return featureRepository.save(Feature.builder().name(name).createdAt(LocalDateTime.now()).build());
    }

    @Override
    public Feature updateFeature(Long id, Feature feature) {
        Feature oldFeature = featureRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Feature not found"));
        oldFeature.setName(feature.getName());
        return featureRepository.save(oldFeature);
    }

    @Override
    public void deleteFeature(Long id) {
        featureRepository.findById(id).ifPresentOrElse(featureRepository::delete,
                () -> {throw new ResourceNotFoundException("Feature not found");
                });
    }
}
