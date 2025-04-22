package com.web.jewelry.service.privacy;

import com.web.jewelry.dto.request.PrivacyAndTermRequest;
import com.web.jewelry.dto.response.PrivacyAndTermResponse;
import com.web.jewelry.model.PrivacyAndTerm;

public interface IPrivacyAndTermService {
    PrivacyAndTerm get();
    PrivacyAndTerm update(PrivacyAndTermRequest request);
    PrivacyAndTermResponse convertToResponse(PrivacyAndTerm privacyAndTerm);
}
