package com.organicnow.backend.unit.exception;

import com.organicnow.backend.exception.RestExceptionHandler;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.validation.BeanPropertyBindingResult;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
    }

    // ✅ Test 1: Validation Exception → 400
    @Test
    void testHandleValidation() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "tenant");
        bindingResult.addError(new FieldError("tenant", "firstName", "must not be blank"));
        bindingResult.addError(new FieldError("tenant", "email", "must be valid"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<?> response = handler.handleValidation(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(400, body.get("status"));
        assertEquals("validation_error", body.get("message"));
        assertTrue(((Map<?, ?>) body.get("errors")).containsKey("firstName"));
        assertTrue(((Map<?, ?>) body.get("errors")).containsKey("email"));
    }

    // ✅ Test 2: Hibernate ConstraintViolation → 409 (duplicate_national_id)
    @Test
    void testHandleHibernateConstraintDuplicateNationalId() {
        SQLException sqlEx = new SQLException("violates constraint uk_tenant_national_id");
        ConstraintViolationException ex = new ConstraintViolationException("constraint", sqlEx, "tenant");
        ResponseEntity<?> response = handler.handleHibernateConstraint(ex);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(409, body.get("status"));
        assertEquals("duplicate_national_id", body.get("message"));
    }

    // ✅ Test 3: Hibernate ConstraintViolation → 409 (duplicate_group_name)
    @Test
    void testHandleHibernateConstraintDuplicateGroupName() {
        SQLException sqlEx = new SQLException("violates constraint uk_asset_group_name");
        ConstraintViolationException ex = new ConstraintViolationException("constraint", sqlEx, "asset_group");
        ResponseEntity<?> response = handler.handleHibernateConstraint(ex);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(409, body.get("status"));
        assertEquals("duplicate_group_name", body.get("message"));
    }

    // ✅ Test 4: DataIntegrityViolation → 409
    @Test
    void testHandleDataIntegrity() {
        Throwable cause = new Throwable("violates constraint uk_tenant_national_id");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("data integrity error", cause);
        ResponseEntity<?> response = handler.handleDataIntegrity(ex);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(409, body.get("status"));
        assertEquals("duplicate_national_id", body.get("message"));
    }

    // ✅ Test 5: Custom business exception (duplicate_group_name)
    @Test
    void testHandleBusinessDuplicateGroupName() {
        RuntimeException ex = new RuntimeException("duplicate_group_name");
        ResponseEntity<?> response = handler.handleBusiness(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(409, body.get("status"));
        assertEquals("duplicate_group_name", body.get("message"));
    }

    // ✅ Test 6: Custom business exception (tenant_already_has_active_contract)
    @Test
    void testHandleBusinessTenantAlreadyHasActiveContract() {
        RuntimeException ex = new RuntimeException("tenant_already_has_active_contract");
        ResponseEntity<?> response = handler.handleBusiness(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(409, body.get("status"));
        assertEquals("duplicate_national_id", body.get("message"));
    }

    // ✅ Test 7: Unknown RuntimeException → 500
    @Test
    void testHandleUnknownRuntimeException() {
        System.setErr(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
        RuntimeException ex = new RuntimeException("something_went_wrong");

        ResponseEntity<?> response = handler.handleBusiness(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(500, body.get("status"));
        assertEquals("server_error", body.get("message"));
        assertTrue(body.containsKey("detail"));
    }


    // ✅ Test 8: resolveDuplicateMessage() fallback case
    @Test
    void testResolveDuplicateMessageFallback() {
        // ใช้ reflection เพื่อเรียก private method
        String result1 = invokeResolveDuplicateMessage("other constraint");
        String result2 = invokeResolveDuplicateMessage("uk_asset_group_name");
        String result3 = invokeResolveDuplicateMessage(null);

        assertEquals("duplicate", result1);
        assertEquals("duplicate_group_name", result2);
        assertEquals("duplicate", result3);
    }

    // helper method for private call
    private String invokeResolveDuplicateMessage(String cause) {
        try {
            var method = RestExceptionHandler.class.getDeclaredMethod("resolveDuplicateMessage", String.class);
            method.setAccessible(true);
            return (String) method.invoke(handler, cause);
        } catch (Exception e) {
            fail("Failed to invoke resolveDuplicateMessage: " + e.getMessage());
            return null;
        }
    }
}
