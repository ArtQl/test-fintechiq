package ru.artq.testfintechiq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.artq.testfintechiq.dto.StopFactorRequest;
import ru.artq.testfintechiq.dto.StopFactorResponse;
import ru.artq.testfintechiq.service.LevenshteinService;
import ru.artq.testfintechiq.service.RequestProcessingService;


@RestController
@RequestMapping("/api")
public record RequestController(
        RequestProcessingService requestProcessingService,
        LevenshteinService levenshteinService
) {

    @PostMapping("/process")
    public ResponseEntity<String> processRequest(
            @RequestBody String jsonContent
    ) {
        requestProcessingService.processRequest(jsonContent);
        return ResponseEntity.ok("Request processed successfully");
    }

    @PostMapping("/process-all")
    public ResponseEntity<String> processAllRequests() {
        requestProcessingService.processAllRequests();
        return ResponseEntity.ok("All requests processed successfully");
    }


    @PostMapping("/calculate")
    public ResponseEntity<StopFactorResponse> calculateStopFactor(
            @RequestBody StopFactorRequest request
    ) {
        boolean stopFactor = levenshteinService.calculateStopFactor(
                request.getRegPersonString(),
                request.getVerifiedNameString()
        );
        return ResponseEntity.ok(new StopFactorResponse(stopFactor));
    }
}