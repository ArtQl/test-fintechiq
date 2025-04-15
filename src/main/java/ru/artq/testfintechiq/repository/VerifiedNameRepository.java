package ru.artq.testfintechiq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.artq.testfintechiq.model.VerifiedName;

@Repository
public interface VerifiedNameRepository extends JpaRepository<VerifiedName, Long> {
    VerifiedName findByLoanRequestId(String loanRequestId);
}