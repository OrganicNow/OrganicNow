package com.organicnow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackagePlanRequestDto {

    @NotNull
    @JsonProperty("contractTypeId")
    @JsonAlias({"contract_type_id"})
    private Long contractTypeId;

    @NotNull
    @Min(0)
    @JsonProperty("price")
    private BigDecimal price;

    @NotNull
    @JsonProperty("isActive")
    @JsonAlias({"is_active"})
    private Integer isActive;

    @NotNull
    @JsonProperty("roomSize")
    @JsonAlias({"room_size"})
    private Integer roomSize;
}
