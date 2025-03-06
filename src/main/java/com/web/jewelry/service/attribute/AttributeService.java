package com.web.jewelry.service.attribute;

import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Attribute;
import com.web.jewelry.repository.AttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AttributeService implements IAttributeService {
    private final AttributeRepository attributeRepository;

    @Override
    public List<Attribute> getAllAttributes() {
        return attributeRepository.findAll();
    }

    @Override
    public Attribute getAttributeById(Long id) {
        return attributeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Attribute not found"));
    }

    @Override
    public Optional<Attribute> getAttributeByName(String name) {
        return Optional.ofNullable(attributeRepository.findByName(name));
    }

    @Override
    public Attribute addAttribute(String name) {
        return attributeRepository.save(Attribute.builder()
                .name(name)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    public Attribute updateAttribute(Long id, Attribute attribute) {
        return attributeRepository.findById(id).map(attribute1 -> {
            attribute1.setName(attribute.getName());
            attribute1.setUpdatedAt(LocalDateTime.now());
            return attributeRepository.save(attribute1);
        }).orElseThrow(() -> new ResourceNotFoundException("Attribute not found"));
    }

    @Override
    public void deleteAttribute(Long id) {
        attributeRepository.findById(id).ifPresentOrElse(attributeRepository::delete, () -> {
            throw new ResourceNotFoundException("Attribute not found");
        });
    }
}
