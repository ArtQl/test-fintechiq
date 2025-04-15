package ru.artq.testfintechiq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.artq.testfintechiq.model.AccountInfo;

import java.util.List;

@Repository
public interface AccountInfoRepository extends JpaRepository<AccountInfo, Long> {
    List<AccountInfo> findAllByLoanRequestId(String loanRequestId);
}