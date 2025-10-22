package com.organicnow.backend.repository;

import com.organicnow.backend.model.TenantDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TenantSearchRepository extends ElasticsearchRepository<TenantDocument, Long> {
    List<TenantDocument> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}
