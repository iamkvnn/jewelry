package com.web.jewelry.service.collection;

import com.web.jewelry.dto.request.CollectionRequest;
import com.web.jewelry.dto.response.CollectionResponse;
import com.web.jewelry.model.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ICollectionService {
    List<CollectionResponse> getAllCollections();
    Collection getCollectionById(Long id);
    List<CollectionResponse> getCollectionByName(String name);
    CollectionResponse addCollection(CollectionRequest request);
    CollectionResponse updateCollection(Long id, CollectionRequest request);
    void deleteCollectionById(Long id);
    CollectionResponse convertToResponse(Collection collection);
    boolean existsByName(String name);
}
