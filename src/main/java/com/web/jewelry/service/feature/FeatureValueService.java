package com.web.jewelry.service.feature;

import com.web.jewelry.dto.request.FeatureValueRequest;
import com.web.jewelry.dto.response.FeatureValueResponse;
import com.web.jewelry.model.Feature;
import com.web.jewelry.model.FeatureValue;
import com.web.jewelry.model.Product;
import com.web.jewelry.repository.FeatureValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FeatureValueService implements IFeatureValueService {
    private final FeatureValueRepository featureValueRepository;
    private final IFeatureService featureService;

    @Override
    public List<FeatureValue> addProductFeatures(Product product, List<FeatureValueRequest> request) {
        return request.stream().map(featureValueRequest -> {
            Feature feature = featureService.getFeatureByName(featureValueRequest.getName())
                                        .orElseGet(() -> featureService.addFeature(featureValueRequest.getName()));
            return featureValueRepository.save(FeatureValue.builder()
                    .product(product)
                    .feature(feature)
                    .value(featureValueRequest.getValue())
                    .build());
        }).distinct().toList();
    }

    @Override
    public List<FeatureValue> updateProductFeature(Product product, List<FeatureValueRequest> request) {
        product.getFeatures().stream()
                .filter(featureValue -> request.stream()
                        .noneMatch(featureValueRequest -> featureValueRequest.getName().equals(featureValue.getFeature().getName())))
                .forEach(featureValue -> featureValueRepository.deleteByProductIdAndFeatureId(featureValue.getProduct().getId(), featureValue.getFeature().getId()));
        return request.stream()
                .map(featureValueRequest -> {
                    Feature feature = featureService.getFeatureByName(featureValueRequest.getName())
                            .orElseGet(() -> featureService.addFeature(featureValueRequest.getName()));
                    FeatureValue featureValue = featureValueRepository.findByProductIdAndFeatureId(product.getId(), feature.getId())
                            .orElseGet(() -> FeatureValue.builder()
                                    .product(product)
                                    .feature(feature)
                                    .build());
                    featureValue.setValue(featureValueRequest.getValue());
                    return featureValueRepository.save(featureValue);
                }).distinct().toList();
    }

    @Override
    public List<FeatureValueResponse> convertToFeatureValueResponses(List<FeatureValue> features) {
        return features.stream().map(featureValue -> {
            FeatureValueResponse response = new FeatureValueResponse();
            response.setId(featureValue.getId());
            response.setFeatureId(featureValue.getFeature().getId());
            response.setName(featureValue.getFeature().getName());
            response.setValue(featureValue.getValue());
            return response;
        }).toList();
    }
}
