package com.web.jewelry.service.privacy;

import com.web.jewelry.dto.request.PrivacyAndTermRequest;
import com.web.jewelry.dto.response.PrivacyAndTermResponse;
import com.web.jewelry.model.PrivacyAndTerm;
import com.web.jewelry.repository.PrivacyAndTermRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class PrivacyAndTermService implements IPrivacyAndTermService {
    private final PrivacyAndTermRepository privacyAndTermRepository;
    private final ModelMapper modelMapper;

    @Override
    public PrivacyAndTerm get() {
        return privacyAndTermRepository.findById(1L).orElse(null);
    }

    @Override
    public PrivacyAndTerm update(PrivacyAndTermRequest request) {
        PrivacyAndTerm privacyAndTerm = get();
        if (privacyAndTerm != null) {
            privacyAndTerm.setContent(request.getContent());
            privacyAndTerm.setUpdatedAt(LocalDateTime.now());
            return privacyAndTermRepository.save(privacyAndTerm);
        }
        return null;
    }

    @Override
    public PrivacyAndTermResponse convertToResponse(PrivacyAndTerm privacyAndTerm) {
        return modelMapper.map(privacyAndTerm, PrivacyAndTermResponse.class);
    }
}
