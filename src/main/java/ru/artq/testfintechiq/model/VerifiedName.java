package ru.artq.testfintechiq.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "verified_name")
public class VerifiedName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_request_id")
    private String loanRequestId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "other_name")
    private String otherName;

    @Column(name = "surname")
    private String surname;
}