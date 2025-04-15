package ru.artq.testfintechiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artq.testfintechiq.exception.BadRequestException;
import ru.artq.testfintechiq.model.AccountInfo;
import ru.artq.testfintechiq.model.RegPerson;
import ru.artq.testfintechiq.model.RequestContent;
import ru.artq.testfintechiq.model.VerifiedName;
import ru.artq.testfintechiq.repository.AccountInfoRepository;
import ru.artq.testfintechiq.repository.RegPersonRepository;
import ru.artq.testfintechiq.repository.RequestContentRepository;
import ru.artq.testfintechiq.repository.VerifiedNameRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestProcessingService {
    private final RequestContentRepository requestContentRepository;
    private final RegPersonRepository regPersonRepository;
    private final VerifiedNameRepository verifiedNameRepository;
    private final AccountInfoRepository accountInfoRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processAllRequests() {
        try {
            List<RequestContent> requests = requestContentRepository.findAll();
            for (RequestContent request : requests) {
                try {
                    processRequestContent(request.getContent());
                } catch (Exception e) {
                    log.error("Error processing request: {} - {}", request.getLoanRequestId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            throw new BadRequestException("Error processing all requests: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void processRequest(String jsonContent) {
        try {
            processRequestContent(jsonContent);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Error processing request: %s"
                    .formatted(e.getMessage()), e);
        }
    }

    private void processRequestContent(String jsonContent) throws Exception {
        JsonNode rootNode = objectMapper.readTree(jsonContent);
        if (!rootNode.has("loanRequestID")) {
            throw new BadRequestException("Missing required field: loanRequestID");
        }

        String loanRequestId = rootNode.get("loanRequestID").asText();
        saveRequestContent(loanRequestId, objectMapper.writeValueAsString(rootNode));

        processRegPerson(loanRequestId, rootNode.path("regPerson"));
        processVerifiedName(loanRequestId, getVerifiedNameNode(rootNode));
        processAccountInfo(loanRequestId, rootNode.path("creditBureau").path("account_info"));
    }

    private JsonNode getVerifiedNameNode(JsonNode rootNode) {
        JsonNode verifiedNameNode = rootNode.path("verified_name");
        if (verifiedNameNode.isMissingNode()) {
            verifiedNameNode = rootNode.path("creditBureau").path("verified_name");
        }
        return verifiedNameNode;
    }

    private void saveRequestContent(String loanRequestId, String content) {
        RequestContent existingContent = requestContentRepository
                .findByLoanRequestId(loanRequestId);
        if (existingContent == null) {
            RequestContent requestContent = new RequestContent();
            requestContent.setLoanRequestId(loanRequestId);
            requestContent.setContent(content);
            requestContent.setCreatedAt(OffsetDateTime.now());
            requestContentRepository.save(requestContent);
        }
    }

    private void processRegPerson(String loanRequestId, JsonNode regPersonNode) {
        if (!regPersonNode.isMissingNode() &&
                regPersonRepository.findByLoanRequestId(loanRequestId) == null) {
            RegPerson regPerson = new RegPerson();
            regPerson.setLoanRequestId(loanRequestId);
            regPerson.setFirstName(getTextValue(regPersonNode, "firstName"));
            regPerson.setMiddleName(getTextValue(regPersonNode, "middleName"));
            regPerson.setLastName(getTextValue(regPersonNode, "lastName"));
            regPersonRepository.save(regPerson);
        }
    }

    private void processVerifiedName(String loanRequestId, JsonNode verifiedNameNode) {
        if (!verifiedNameNode.isMissingNode() &&
                verifiedNameRepository.findByLoanRequestId(loanRequestId) == null) {
            VerifiedName verifiedName = new VerifiedName();
            verifiedName.setLoanRequestId(loanRequestId);
            verifiedName.setFirstName(getTextValue(verifiedNameNode, "first_name"));
            verifiedName.setOtherName(getTextValue(verifiedNameNode, "other_name"));
            verifiedName.setSurname(getTextValue(verifiedNameNode, "surname"));
            verifiedNameRepository.save(verifiedName);
        }
    }

    private void processAccountInfo(String loanRequestId, JsonNode accountInfoArray) {
        if (!accountInfoArray.isArray()) return;

        List<AccountInfo> existingAccounts = accountInfoRepository.findAllByLoanRequestId(loanRequestId);
        if (!existingAccounts.isEmpty()) return;

        try {
            for (JsonNode accountNode : accountInfoArray) {
                AccountInfo accountInfo = createAccountInfo(loanRequestId, accountNode);
                accountInfoRepository.save(accountInfo);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing account info: " + e.getMessage(), e);
        }
    }

    private AccountInfo createAccountInfo(String loanRequestId, JsonNode accountNode) {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setLoanRequestId(loanRequestId);
        accountInfo.setAccountNumber(getTextValue(accountNode, "account_number"));
        accountInfo.setAccountStatus(getTextValue(accountNode, "account_status"));
        accountInfo.setCurrentBalance(parseBigDecimal(accountNode, "current_balance"));
        accountInfo.setDateOpened(parseLocalDate(accountNode, "date_opened"));
        accountInfo.setDaysInArrears(parseInteger(accountNode, "days_in_arrears"));
        accountInfo.setDelinquencyCode(getTextValue(accountNode, "delinquency_code"));
        accountInfo.setHighestDaysInArrears(parseInteger(accountNode, "highest_days_in_arrears"));
        accountInfo.setIsYourAccount(parseBoolean(accountNode, "is_your_account"));
        accountInfo.setLastPaymentAmount(parseBigDecimal(accountNode, "last_payment_amount"));
        accountInfo.setLastPaymentDate(parseLocalDate(accountNode, "last_payment_date"));
        accountInfo.setLoadedAt(parseLocalDate(accountNode, "loaded_at"));
        accountInfo.setOriginalAmount(parseBigDecimal(accountNode, "original_amount"));
        accountInfo.setOverdueBalance(parseBigDecimal(accountNode, "overdue_balance"));
        accountInfo.setOverdueDate(parseLocalDate(accountNode, "overdue_date"));
        accountInfo.setProductTypeId(parseInteger(accountNode, "product_type_id"));
        return accountInfo;
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asText() : null;
    }

    private BigDecimal parseBigDecimal(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull()) {
            try {
                return new BigDecimal(field.asText().replace("\"", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer parseInteger(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull()) {
            try {
                return field.asInt();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private Boolean parseBoolean(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull()) {
            return field.asBoolean();
        }
        return null;
    }

    private LocalDate parseLocalDate(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull() && !field.asText().equals("null")) {
            try {
                return LocalDate.parse(field.asText());
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }
}