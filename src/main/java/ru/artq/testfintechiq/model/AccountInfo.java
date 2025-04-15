package ru.artq.testfintechiq.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "account_info")
public class AccountInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_request_id")
    private String loanRequestId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "current_balance")
    private BigDecimal currentBalance;

    @Column(name = "date_opened")
    private LocalDate dateOpened;

    @Column(name = "days_in_arrears")
    private Integer daysInArrears;

    @Column(name = "delinquency_code")
    private String delinquencyCode;

    @Column(name = "highest_days_in_arrears")
    private Integer highestDaysInArrears;

    @Column(name = "is_your_account")
    private Boolean isYourAccount;

    @Column(name = "last_payment_amount")
    private BigDecimal lastPaymentAmount;

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @Column(name = "loaded_at")
    private LocalDate loadedAt;

    @Column(name = "original_amount")
    private BigDecimal originalAmount;

    @Column(name = "overdue_balance")
    private BigDecimal overdueBalance;

    @Column(name = "overdue_date")
    private LocalDate overdueDate;

    @Column(name = "product_type_id")
    private Integer productTypeId;
}