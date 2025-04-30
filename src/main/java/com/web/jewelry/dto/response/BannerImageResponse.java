package com.web.jewelry.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BannerImageResponse extends ImageResponse{
    private String position;
}
