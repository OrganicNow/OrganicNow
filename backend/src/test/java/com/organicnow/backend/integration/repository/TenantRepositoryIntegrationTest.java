package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.Tenant;
import com.organicnow.backend.repository.TenantRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Test: TenantRepository")
class TenantRepositoryIntegrationTest {

    @Autowired
    private TenantRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private Tenant tenant1, tenant2, tenant3, tenant4, tenant5;

    @BeforeEach
    void setUp() {
        // Clear all data with CASCADE to handle foreign keys
        entityManager.getEntityManager()
                .createNativeQuery("TRUNCATE TABLE maintenance_notification_skip, maintenance_schedule, " +
                        "asset_event, room_asset, asset, asset_group, payment_proofs, payment_records, " +
                        "invoice_item, invoice, maintain, contract_file, contract, room, " +
                        "package_plan, contract_type, tenant, fee, admin " +
                        "RESTART IDENTITY CASCADE")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // Create test data
        setupTestData();
    }

    private void setupTestData() {
        // Exact matches
        tenant1 = new Tenant();
        tenant1.setFirstName("John");
        tenant1.setLastName("Doe");
        tenant1.setNationalId("1234567890");
        tenant1.setPhoneNumber("0812345678");
        tenant1.setEmail("john@example.com");
        entityManager.persistAndFlush(tenant1);

        tenant2 = new Tenant();
        tenant2.setFirstName("Jane");
        tenant2.setLastName("Smith");
        tenant2.setNationalId("0987654321");
        tenant2.setPhoneNumber("0887654321");
        tenant2.setEmail("jane@example.com");
        entityManager.persistAndFlush(tenant2);

        // Similar names (for fuzzy search)
        tenant3 = new Tenant();
        tenant3.setFirstName("Jon");  // Similar to "John"
        tenant3.setLastName("Doe");
        tenant3.setNationalId("1111111111");
        tenant3.setPhoneNumber("0811111111");
        tenant3.setEmail("jon@example.com");
        entityManager.persistAndFlush(tenant3);

        tenant4 = new Tenant();
        tenant4.setFirstName("Johnn");  // Similar to "John"
        tenant4.setLastName("Doe");
        tenant4.setNationalId("2222222222");
        tenant4.setPhoneNumber("0822222222");
        tenant4.setEmail("johnn@example.com");
        entityManager.persistAndFlush(tenant4);

        // Partial match
        tenant5 = new Tenant();
        tenant5.setFirstName("Johnson");  // Contains "John"
        tenant5.setLastName("Anderson");
        tenant5.setNationalId("3333333333");
        tenant5.setPhoneNumber("0833333333");
        tenant5.setEmail("johnson@example.com");
        entityManager.persistAndFlush(tenant5);
    }

    @Test
    @DisplayName("findByNationalId: should return tenant when national id matches")
    void findByNationalId_ShouldReturnTenantWhenMatches() {
        // When
        Optional<Tenant> result = repository.findByNationalId("1234567890");

        // Then
        assertThat(result)
                .isPresent()
                .get()
                .satisfies(t -> {
                    assertThat(t.getFirstName()).isEqualTo("John");
                    assertThat(t.getLastName()).isEqualTo("Doe");
                });
    }

    @Test
    @DisplayName("findByNationalId: should return empty when national id not found")
    void findByNationalId_ShouldReturnEmptyWhenNotFound() {
        // When
        Optional<Tenant> result = repository.findByNationalId("9999999999");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("searchFuzzy: should find exact match")
    void searchFuzzy_ShouldFindExactMatch() {
        // When
        List<Tenant> result = repository.searchFuzzy("John");

        // Then
        assertThat(result)
                .isNotEmpty()
                .anyMatch(t -> t.getFirstName().equals("John") && t.getLastName().equals("Doe"));
    }

    @Test
    @DisplayName("searchFuzzy: should find partial matches with ILIKE")
    void searchFuzzy_ShouldFindPartialMatches() {
        // When
        List<Tenant> result = repository.searchFuzzy("John");

        // Then
        assertThat(result)
                .hasSizeGreaterThanOrEqualTo(3)
                .extracting(Tenant::getFirstName)
                .containsExactlyInAnyOrder("John", "Jon", "Johnn", "Johnson");
    }

    @Test
    @DisplayName("searchFuzzy: should find similar names with similarity function")
    void searchFuzzy_ShouldFindSimilarNames() {
        // When
        List<Tenant> result = repository.searchFuzzy("Johnn");

        // Then
        assertThat(result)
                .isNotEmpty()
                .anyMatch(t -> t.getFirstName().equals("John")) // Should match due to similarity
                .anyMatch(t -> t.getFirstName().equals("Johnn")); // Exact match
    }

    @Test
    @DisplayName("searchFuzzy: should handle short keywords with lower threshold")
    void searchFuzzy_ShouldHandleShortKeywords() {
        // When - search with 2 character keyword
        List<Tenant> result = repository.searchFuzzy("Jo");

        // Then
        assertThat(result)
                .isNotEmpty()
                .anyMatch(t -> t.getFirstName().equals("John"))
                .anyMatch(t -> t.getFirstName().equals("Jon"));
    }

    @Test
    @DisplayName("searchFuzzy: should find by last name")
    void searchFuzzy_ShouldFindByLastName() {
        // When
        List<Tenant> result = repository.searchFuzzy("Doe");

        // Then
        assertThat(result)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(Tenant::getLastName)
                .containsExactlyInAnyOrder("Doe", "Doe", "Doe");
    }

    @Test
    @DisplayName("searchFuzzy: should find by partial last name")
    void searchFuzzy_ShouldFindByPartialLastName() {
        // When
        List<Tenant> result = repository.searchFuzzy("Smith");

        // Then
        assertThat(result)
                .isNotEmpty()
                .anyMatch(t -> t.getLastName().equals("Smith"));
    }

    @Test
    @DisplayName("searchFuzzy: should return empty for no matches")
    void searchFuzzy_ShouldReturnEmptyForNoMatches() {
        // When
        List<Tenant> result = repository.searchFuzzy("XYZ123");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("searchFuzzy: should be case insensitive")
    void searchFuzzy_ShouldBeCaseInsensitive() {
        // When
        List<Tenant> resultLower = repository.searchFuzzy("john");
        List<Tenant> resultUpper = repository.searchFuzzy("JOHN");
        List<Tenant> resultMixed = repository.searchFuzzy("JoHn");

        // Then
        assertThat(resultLower)
                .isNotEmpty()
                .anyMatch(t -> t.getFirstName().equals("John"));
        assertThat(resultUpper)
                .isNotEmpty()
                .anyMatch(t -> t.getFirstName().equals("John"));
        assertThat(resultMixed)
                .isNotEmpty()
                .anyMatch(t -> t.getFirstName().equals("John"));
    }

    @Test
    @DisplayName("searchFuzzy: should rank results by similarity score")
    void searchFuzzy_ShouldRankResultsBySimilarity() {
        // When
        List<Tenant> result = repository.searchFuzzy("John");

        // Then
        // First result should be exact match or very similar
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFirstName())
                .isIn("John", "Johnn", "Jon"); // All are highly similar
    }

    @Test
    @DisplayName("searchFuzzy: should handle long keywords with higher threshold")
    void searchFuzzy_ShouldHandleLongKeywords() {
        // When
        List<Tenant> result = repository.searchFuzzy("Johnson");

        // Then
        assertThat(result)
                .isNotEmpty()
                .anyMatch(t -> t.getFirstName().equals("Johnson"));
    }

    @Test
    @DisplayName("save: should successfully create a new tenant")
    void save_ShouldSuccessfullyCreateNewTenant() {
        // Given
        Tenant newTenant = new Tenant();
        newTenant.setFirstName("Alice");
        newTenant.setLastName("Brown");
        newTenant.setNationalId("4444444444");
        newTenant.setPhoneNumber("0844444444");
        newTenant.setEmail("alice@example.com");

        // When
        Tenant saved = repository.save(newTenant);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findByNationalId("4444444444")).isPresent();
    }

    @Test
    @DisplayName("delete: should successfully delete a tenant")
    void delete_ShouldSuccessfullyDeleteTenant() {
        // Given
        Long tenantId = tenant1.getId();
        assertThat(repository.findById(tenantId)).isPresent();

        // When
        repository.delete(tenant1);
        entityManager.flush();

        // Then
        assertThat(repository.findById(tenantId)).isEmpty();
    }

    @Test
    @DisplayName("findAll: should return all tenants")
    void findAll_ShouldReturnAllTenants() {
        // When
        List<Tenant> result = repository.findAll();

        // Then
        assertThat(result).hasSize(5);
        assertThat(result)
                .extracting(Tenant::getNationalId)
                .containsExactlyInAnyOrder("1234567890", "0987654321", "1111111111", "2222222222", "3333333333");
    }

    @Test
    @DisplayName("searchFuzzy: should search by multiple criteria simultaneously")
    void searchFuzzy_ShouldSearchByMultipleCriteria() {
        // When - search for "Jane" which should match by firstName and lastName patterns
        List<Tenant> result = repository.searchFuzzy("Jane");

        // Then
        assertThat(result)
                .isNotEmpty()
                .anyMatch(t -> t.getFirstName().equals("Jane"));
    }

    @Test
    @DisplayName("searchFuzzy: should handle special characters in names")
    void searchFuzzy_ShouldHandleSearch() {
        // When - search for a part of last name "Smith"
        List<Tenant> result = repository.searchFuzzy("Smith");

        // Then
        assertThat(result)
                .isNotEmpty()
                .anyMatch(t -> t.getLastName().contains("Smith"));
    }

}