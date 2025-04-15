package ru.artq.testfintechiq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.artq.testfintechiq.model.RequestContent;

@Repository
public interface RequestContentRepository extends JpaRepository<RequestContent, Long> {
    RequestContent findByLoanRequestId(String loanRequestId);
}