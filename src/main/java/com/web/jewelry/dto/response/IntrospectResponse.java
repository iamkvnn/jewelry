package com.web.jewelry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class IntrospectResponse {
    private boolean valid;
}
