package com.adspeek.authservice.dto;

import com.adspeek.authservice.entity.Lead;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicLeadRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String companyName;
    private Long serviceItemId;
    private String serviceCategoryName;
    private String serviceSubcategoryName;
    private String serviceItemName;
    private String serviceDescription;
    private Lead.Source source;
    private String ipAddress;
    private String userAgent;
}
