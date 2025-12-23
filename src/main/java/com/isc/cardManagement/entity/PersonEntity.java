package com.isc.cardManagement.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "tbl_person")
@NoArgsConstructor
@Getter
@Setter
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long id;

    @NotBlank(message = "{first.name.blank}")
    @Size(min = 2, max = 50)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "{last.name.blank}")
    @Size(min = 2, max = 50)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "{national.code.can.not.be.blank}")
    @Size(min = 10, max = 10)
    @Pattern(regexp = "\\d+", message = "{national.code.invalid}")
    @Column(name = "national_code", length = 10, unique = true, nullable = false)
    private String nationalCode;

    @NotBlank(message = "{phone.invalid}")
    @Pattern(regexp = "\\d{11}", message = "{phone.invalid}")
    @Column(nullable = false)
    private String phone;

    @NotBlank(message = "{address.blank}")
    @Size(max = 255)
    @Column(nullable = false)
    private String address;


    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<AccountEntity> accounts = new HashSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonEntity that)) return false;
        return nationalCode != null && nationalCode.equals(that.nationalCode);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("PersonEntity[id=%d, nationalCode=%s, name=%s %s]",
                id, nationalCode, firstName, lastName);
    }

}

