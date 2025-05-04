package com.web.jewelry.controller;

import com.web.jewelry.dto.request.CollectionRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.CollectionResponse;
import com.web.jewelry.model.Collection;
import com.web.jewelry.service.collection.ICollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/collections")
public class CollectionController {
    private final ICollectionService collectionService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getCollections() {
        List<CollectionResponse> collections = collectionService.getAllCollections();
        return ResponseEntity.ok(new ApiResponse("200", "Success", collections));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCollection(@RequestBody CollectionRequest request) {
        CollectionResponse collection = collectionService.addCollection(request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", collection));
    }

    @GetMapping("/collection/{id}")
    public ResponseEntity<ApiResponse> getCollectionById(@PathVariable Long id){
        Collection collection = collectionService.getCollectionById(id);
        CollectionResponse response = collectionService.convertToResponse(collection);
        return  ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @GetMapping("/collection")
    public ResponseEntity<ApiResponse> getCollectionByName(@RequestParam String name){
        List<CollectionResponse> collections = collectionService.getCollectionByName(name);
        return  ResponseEntity.ok(new ApiResponse("200", "Success", collections));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/collection/{id}")
    public ResponseEntity<ApiResponse> deleteCollection(@PathVariable Long id){
        collectionService.deleteCollectionById(id);
        return  ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/collection/{id}")
    public ResponseEntity<ApiResponse> updateCollection(@PathVariable Long id, @RequestBody CollectionRequest request) {
        CollectionResponse collection = collectionService.updateCollection(id, request);
        return ResponseEntity.ok(new ApiResponse("200", "Success", collection));
    }
}
