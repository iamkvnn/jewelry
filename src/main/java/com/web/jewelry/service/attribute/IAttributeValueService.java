package com.web.jewelry.service.attribute;

import com.web.jewelry.dto.request.AttributeValueRequest;
import com.web.jewelry.dto.response.AttributeValueResponse;
import com.web.jewelry.model.AttributeValue;
import com.web.jewelry.model.Product;

import java.util.List;

public interface IAttributeValueService {
    List<AttributeValue> addProductAttributes(Product product, List<AttributeValueRequest> request);
    List<AttributeValue> updateProductAttributes(Product product, List<AttributeValueRequest> request);
    List<AttributeValueResponse> convertToAttributeValueResponses(List<AttributeValue> attributeValues);
}
