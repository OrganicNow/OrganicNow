package com.organicnow.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "tenant_index")  // ✅ ชื่อ index ใน Elasticsearch
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDocument {

    @Id
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String nationalId;
}
