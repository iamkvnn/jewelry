package com.web.jewelry.service.attribute;

import com.web.jewelry.model.Attribute;

import java.util.List;
import java.util.Optional;

public interface IAttributeService {
    List<Attribute> getAllAttributes();
    Attribute getAttributeById(Long id);
    Optional<Attribute> getAttributeByName(String name);
    Attribute addAttribute(String name);
    Attribute updateAttribute(Long id, Attribute attribute);
    void deleteAttribute(Long id);
}
