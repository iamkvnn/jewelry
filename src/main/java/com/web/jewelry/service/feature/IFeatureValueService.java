package com.web.jewelry.service.feature;

import com.web.jewelry.dto.request.FeatureValueRequest;
import com.web.jewelry.dto.request.ProductRequest;
import com.web.jewelry.dto.response.FeatureValueResponse;
import com.web.jewelry.model.Feature;
import com.web.jewelry.model.FeatureValue;
import com.web.jewelry.model.Product;

import java.util.List;

public interface IFeatureValueService {
    List<FeatureValue> addProductFeatures(Product product, List<FeatureValueRequest> request);
    List<FeatureValue> updateProductFeature(Product product, List<FeatureValueRequest> request);
    List<FeatureValueResponse> convertToFeatureValueResponses(List<FeatureValue> features);
}
