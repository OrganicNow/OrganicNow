package com.organicnow.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(
        name = "room",
        uniqueConstraints = @UniqueConstraint(name = "uk_room_room_number", columnNames = "room_number")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;   // Room_id

    @NotBlank
    @Column(name = "room_number", nullable = false, length = 30)
    private String roomNumber;   // Room_Number

    @Min(0)
    @Column(name = "room_floor", nullable = false)
    private Integer roomFloor;   // Room_Floor
}
