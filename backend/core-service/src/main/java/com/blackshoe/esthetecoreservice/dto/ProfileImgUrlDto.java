package com.blackshoe.esthetecoreservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileImgUrlDto {
    private String s3Url;
    private String cloudfrontUrl;
}
