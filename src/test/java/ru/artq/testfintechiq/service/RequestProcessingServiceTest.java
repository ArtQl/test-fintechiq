package ru.artq.testfintechiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.artq.testfintechiq.exception.BadRequestException;
import ru.artq.testfintechiq.model.RegPerson;
import ru.artq.testfintechiq.model.RequestContent;
import ru.artq.testfintechiq.model.VerifiedName;
import ru.artq.testfintechiq.repository.AccountInfoRepository;
import ru.artq.testfintechiq.repository.RegPersonRepository;
import ru.artq.testfintechiq.repository.RequestContentRepository;
import ru.artq.testfintechiq.repository.VerifiedNameRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestProcessingServiceTest {

    @Mock
    private RequestContentRepository requestContentRepository;

    @Mock
    private RegPersonRepository regPersonRepository;

    @Mock
    private VerifiedNameRepository verifiedNameRepository;

    @Mock
    private AccountInfoRepository accountInfoRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RequestProcessingService requestProcessingService;

    private ObjectMapper realObjectMapper;
    private JsonNode sampleJsonNode;
    private String sampleJsonString;

    @BeforeEach
    void setUp() throws Exception {
        realObjectMapper = new ObjectMapper();

        ObjectNode rootNode = realObjectMapper.createObjectNode();
        rootNode.put("loanRequestID", "12345");

        ObjectNode regPersonNode = realObjectMapper.createObjectNode();
        regPersonNode.put("firstName", "John");
        regPersonNode.put("middleName", "William");
        regPersonNode.put("lastName", "Doe");
        rootNode.set("regPerson", regPersonNode);

        ObjectNode verifiedNameNode = realObjectMapper.createObjectNode();
        verifiedNameNode.put("first_name", "JOHN");
        verifiedNameNode.put("other_name", "WILLIAM");
        verifiedNameNode.put("surname", "DOE");
        rootNode.set("verified_name", verifiedNameNode);

        ObjectNode creditBureau = realObjectMapper.createObjectNode();
        rootNode.set("creditBureau", creditBureau);

        sampleJsonNode = rootNode;
        sampleJsonString = realObjectMapper.writeValueAsString(rootNode);

        lenient().when(objectMapper.readTree(anyString())).thenReturn(sampleJsonNode);
        lenient().when(objectMapper.writeValueAsString(any(JsonNode.class))).thenReturn(sampleJsonString);
    }

    @Test
    void processRequest_validation() throws Exception {
        // Missing loanRequestID
        ObjectNode invalidNode = realObjectMapper.createObjectNode();
        when(objectMapper.readTree(anyString())).thenReturn(invalidNode);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> requestProcessingService.processRequest("{}"));
        assertEquals("Missing required field: loanRequestID", exception.getMessage());
    }

    @Test
    void processRequest_contentHandling() {
        // New content - should save
        when(requestContentRepository.findByLoanRequestId(anyString())).thenReturn(null);
        requestProcessingService.processRequest(sampleJsonString);

        ArgumentCaptor<RequestContent> contentCaptor = ArgumentCaptor.forClass(RequestContent.class);
        verify(requestContentRepository).save(contentCaptor.capture());

        RequestContent savedContent = contentCaptor.getValue();
        assertEquals("12345", savedContent.getLoanRequestId());
        assertEquals(sampleJsonString, savedContent.getContent());

        // Existing content - should not save
        RequestContent existingContent = new RequestContent();
        existingContent.setLoanRequestId("12345");
        when(requestContentRepository.findByLoanRequestId("12345")).thenReturn(existingContent);

        requestProcessingService.processRequest(sampleJsonString);
        verify(requestContentRepository, times(1)).save(any(RequestContent.class));
    }

    @Test
    void processRequest_entitySaving() {
        when(requestContentRepository.findByLoanRequestId(anyString())).thenReturn(null);
        when(regPersonRepository.findByLoanRequestId(anyString())).thenReturn(null);
        when(verifiedNameRepository.findByLoanRequestId(anyString())).thenReturn(null);

        requestProcessingService.processRequest(sampleJsonString);

        // Verify RegPerson is saved
        ArgumentCaptor<RegPerson> regPersonCaptor = ArgumentCaptor.forClass(RegPerson.class);
        verify(regPersonRepository).save(regPersonCaptor.capture());

        RegPerson savedRegPerson = regPersonCaptor.getValue();
        assertEquals("12345", savedRegPerson.getLoanRequestId());
        assertEquals("John", savedRegPerson.getFirstName());
        assertEquals("William", savedRegPerson.getMiddleName());
        assertEquals("Doe", savedRegPerson.getLastName());

        // Verify VerifiedName is saved
        ArgumentCaptor<VerifiedName> verifiedNameCaptor = ArgumentCaptor.forClass(VerifiedName.class);
        verify(verifiedNameRepository).save(verifiedNameCaptor.capture());

        VerifiedName savedVerifiedName = verifiedNameCaptor.getValue();
        assertEquals("12345", savedVerifiedName.getLoanRequestId());
        assertEquals("JOHN", savedVerifiedName.getFirstName());
        assertEquals("WILLIAM", savedVerifiedName.getOtherName());
        assertEquals("DOE", savedVerifiedName.getSurname());
    }

    @Test
    void processAllRequests_handling() throws Exception {
        // Multiple requests
        List<RequestContent> requests = new ArrayList<>();
        RequestContent request1 = new RequestContent();
        request1.setLoanRequestId("12345");
        request1.setContent(sampleJsonString);
        requests.add(request1);

        RequestContent request2 = new RequestContent();
        request2.setLoanRequestId("67890");
        request2.setContent(sampleJsonString);
        requests.add(request2);

        when(requestContentRepository.findAll()).thenReturn(requests);
        when(objectMapper.readTree(anyString())).thenReturn(sampleJsonNode);

        requestProcessingService.processAllRequests();
        verify(objectMapper, times(2)).readTree(anyString());

        // Error handling - one request fails but processing continues
        List<RequestContent> mixedRequests = new ArrayList<>();
        RequestContent badRequest = new RequestContent();
        badRequest.setLoanRequestId("12345");
        badRequest.setContent("invalid json");
        mixedRequests.add(badRequest);

        RequestContent goodRequest = new RequestContent();
        goodRequest.setLoanRequestId("67890");
        goodRequest.setContent(sampleJsonString);
        mixedRequests.add(goodRequest);

        when(requestContentRepository.findAll()).thenReturn(mixedRequests);
        when(objectMapper.readTree("invalid json")).thenThrow(new RuntimeException("Invalid JSON"));
        reset(objectMapper);
        when(objectMapper.readTree("invalid json")).thenThrow(new RuntimeException("Invalid JSON"));
        when(objectMapper.readTree(sampleJsonString)).thenReturn(sampleJsonNode);

        // Should not throw exception
        requestProcessingService.processAllRequests();

        // Should still process the second request - без конкретного количества
        verify(objectMapper).readTree(sampleJsonString);
    }
} 