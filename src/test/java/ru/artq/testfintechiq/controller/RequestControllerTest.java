package ru.artq.testfintechiq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.artq.testfintechiq.dto.StopFactorRequest;
import ru.artq.testfintechiq.service.LevenshteinService;
import ru.artq.testfintechiq.service.RequestProcessingService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
public class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RequestProcessingService requestProcessingService;

    @MockitoBean
    private LevenshteinService levenshteinService;

    @Test
    public void processEndpoints_shouldReturnOk() throws Exception {
        // Test /api/process endpoint
        String jsonContent = "{\"test\": \"value\"}";
        doNothing().when(requestProcessingService).processRequest(anyString());

        MvcResult result = mockMvc.perform(post("/api/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("Request processed successfully", result.getResponse().getContentAsString());
        verify(requestProcessingService, times(1)).processRequest(jsonContent);

        // Test /api/process-all endpoint
        doNothing().when(requestProcessingService).processAllRequests();

        result = mockMvc.perform(post("/api/process-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("All requests processed successfully", result.getResponse().getContentAsString());
        verify(requestProcessingService, times(1)).processAllRequests();
    }

    @Test
    public void calculateStopFactor_shouldReturnCorrectResults() throws Exception {
        // Test when factor is true
        var trueRequest = new StopFactorRequest("person string", "verified string");
        when(levenshteinService.calculateStopFactor(eq("person string"), eq("verified string"))).thenReturn(true);

        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trueRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stopFactor").value(true));

        // Test when factor is false
        var falseRequest = new StopFactorRequest("person string", "different string");
        when(levenshteinService.calculateStopFactor(eq("person string"), eq("different string"))).thenReturn(false);

        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(falseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stopFactor").value(false));
    }
} 