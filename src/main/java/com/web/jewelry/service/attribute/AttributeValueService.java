package com.web.jewelry.service.attribute;

import com.web.jewelry.dto.request.AttributeValueRequest;
import com.web.jewelry.dto.response.AttributeValueResponse;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.model.Attribute;
import com.web.jewelry.model.AttributeValue;
import com.web.jewelry.model.Product;
import com.web.jewelry.repository.AttributeValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AttributeValueService implements IAttributeValueService {
    private final AttributeValueRepository attributeValueRepository;
    private final IAttributeService attributeService;

    @Override
    public List<AttributeValue> addProductAttributes(Product product, List<AttributeValueRequest> request) {
        return request.stream().map(attributeValueRequest -> {
            Attribute attribute = attributeService.getAttributeByName(attributeValueRequest.getName())
                                        .orElseGet(() -> attributeService.addAttribute(attributeValueRequest.getName()));
            if (attributeValueRepository.existsByProductIdAndAttributeId(product.getId(), attribute.getId())) {
                throw new AlreadyExistException("Attribute " + attributeValueRequest.getName() + " already exists for this product");
            }
            return attributeValueRepository.save(AttributeValue.builder()
                    .product(product)
                    .attribute(attribute)
                    .value(attributeValueRequest.getValue())
                    .build());
        }).distinct().toList();
    }

    @Override
    public List<AttributeValue> updateProductAttributes(Product product, List<AttributeValueRequest> request) {
        product.getAttributes().stream()
                .filter(attributeValue -> request.stream()
                        .noneMatch(attributeValueRequest -> attributeValueRequest.getName().equals(attributeValue.getAttribute().getName())))
                .forEach(attributeValue -> attributeValueRepository.deleteByProductIdAndAttributeId(attributeValue.getProduct().getId(), attributeValue.getAttribute().getId()));
        return request.stream()
                .map(attributeValueRequest -> {
                    Attribute attribute = attributeService.getAttributeByName(attributeValueRequest.getName())
                            .orElseGet(() -> attributeService.addAttribute(attributeValueRequest.getName()));
                    AttributeValue attributeValue = attributeValueRepository.findByProductIdAndAttributeId(product.getId(), attribute.getId())
                            .orElseGet(() -> AttributeValue.builder()
                                    .product(product)
                                    .attribute(attribute)
                                    .build());
                    attributeValue.setValue(attributeValueRequest.getValue());
                    return attributeValueRepository.save(attributeValue);
                }).distinct().toList();
    }

    @Override
    public List<AttributeValueResponse> convertToAttributeValueResponses(List<AttributeValue> attributeValues) {
        return attributeValues.stream().map(attributeValue -> {
            AttributeValueResponse response = new AttributeValueResponse();
            response.setAttributeId(attributeValue.getAttribute().getId());
            response.setName(attributeValue.getAttribute().getName());
            response.setValue(attributeValue.getValue());
            return response;
        }).toList();
    }
}
