package com.web.jewelry.service.privacy;

import com.web.jewelry.dto.request.NotificationRequest;
import com.web.jewelry.dto.request.PrivacyAndTermRequest;
import com.web.jewelry.dto.response.PrivacyAndTermResponse;
import com.web.jewelry.model.PrivacyAndTerm;
import com.web.jewelry.repository.PrivacyAndTermRepository;
import com.web.jewelry.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class PrivacyAndTermService implements IPrivacyAndTermService {
    private final PrivacyAndTermRepository privacyAndTermRepository;
    private final INotificationService notificationService;
    private final ModelMapper modelMapper;

    @Override
    public PrivacyAndTerm get() {
        return privacyAndTermRepository.findById(1L).orElse(null);
    }

    @Override
    public PrivacyAndTerm update(PrivacyAndTermRequest request) {
        PrivacyAndTerm privacyAndTerm = get();
        privacyAndTerm.setContent(request.getContent());
        privacyAndTerm.setUpdatedAt(LocalDateTime.now());
        notificationService.sendNotificationToAllCustomer(
                NotificationRequest.builder()
                        .title("Cừa hàng vừa thay đổi một số điều khoản và chính sách")
                        .content("Cừa hàng vừa thay đổi một số điều khoản và chính sách, vui lòng chú ý kiểm tra")
                        .build()
        );
        return privacyAndTermRepository.save(privacyAndTerm);
    }

    @Override
    public PrivacyAndTermResponse convertToResponse(PrivacyAndTerm privacyAndTerm) {
        return modelMapper.map(privacyAndTerm, PrivacyAndTermResponse.class);
    }
}
