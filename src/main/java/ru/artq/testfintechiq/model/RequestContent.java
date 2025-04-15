package ru.artq.testfintechiq.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "request_content")
public class RequestContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_request_id", unique = true)
    private String loanRequestId;

    @Column(name = "content", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String content;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}