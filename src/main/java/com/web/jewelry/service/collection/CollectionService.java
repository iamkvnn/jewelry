package com.web.jewelry.service.collection;

import com.web.jewelry.dto.request.CollectionRequest;
import com.web.jewelry.dto.response.CollectionResponse;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Collection;
import com.web.jewelry.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CollectionService implements ICollectionService{
    private final CollectionRepository collectionRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<CollectionResponse> getAllCollections() {
        return collectionRepository.findAll().stream().map(this::convertToResponse).toList();
    }

    @Override
    public Collection getCollectionById(Long id) {
        return collectionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
    }

    @Override
    public List<CollectionResponse> getCollectionByName(String name) {
        return collectionRepository.findByNameContaining(name).stream().map(this::convertToResponse).toList();
    }

    @Override
    public CollectionResponse addCollection(CollectionRequest request) {
        if (existsByName(request.getName())) {
            throw new ResourceNotFoundException("Collection already exists please check again!");
        }
        return convertToResponse(collectionRepository.save(Collection.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .build()));
    }

    @Override
    public CollectionResponse updateCollection(Long id, CollectionRequest request) {
        Collection oldCollection = collectionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
        if (existsByName(request.getName()) && !oldCollection.getName().equals(request.getName())) {
            throw new ResourceNotFoundException("Collection already exists please check again!");
        }
        oldCollection.setName(request.getName());
        oldCollection.setDescription(request.getDescription());
        oldCollection.setUpdatedAt(LocalDateTime.now());
        return convertToResponse(collectionRepository.save(oldCollection));
    }

    @Override
    public void deleteCollectionById(Long id) {
        collectionRepository.findById(id).ifPresentOrElse(collectionRepository::delete,
                () -> {throw new ResourceNotFoundException("Collection not found");
                });
    }

    @Override
    public CollectionResponse convertToResponse(Collection collection) {
        return modelMapper.map(collection, CollectionResponse.class);
    }

    @Override
    public boolean existsByName(String name) {
        return collectionRepository.existsByName(name);
    }
}
