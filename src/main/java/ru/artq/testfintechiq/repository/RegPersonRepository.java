package ru.artq.testfintechiq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.artq.testfintechiq.model.RegPerson;

@Repository
public interface RegPersonRepository extends JpaRepository<RegPerson, Long> {
    RegPerson findByLoanRequestId(String loanRequestId);
}