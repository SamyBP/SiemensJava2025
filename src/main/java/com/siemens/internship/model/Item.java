package com.siemens.internship.model;

import jakarta.persistence.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "name must not be empty")
    private String name;

    @NotEmpty(message = "description must not be empty")
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @Email(regexp = "^(.+)@(.+)$", message = "invalid email format")
    private String email;

    public Item(String name, String description, String email) {
        this.name = name;
        this.description = description;
        this.status = ItemStatus.UNPROCESSED;
        this.email = email;
    }
}