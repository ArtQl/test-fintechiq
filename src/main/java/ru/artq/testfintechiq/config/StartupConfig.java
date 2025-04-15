package ru.artq.testfintechiq.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.artq.testfintechiq.model.RegPerson;
import ru.artq.testfintechiq.model.RequestContent;
import ru.artq.testfintechiq.model.VerifiedName;
import ru.artq.testfintechiq.repository.RegPersonRepository;
import ru.artq.testfintechiq.repository.RequestContentRepository;
import ru.artq.testfintechiq.repository.VerifiedNameRepository;
import ru.artq.testfintechiq.service.LevenshteinService;
import ru.artq.testfintechiq.service.RequestProcessingService;
import ru.artq.testfintechiq.service.StringConcatenationService;

@Configuration
public class StartupConfig {

    @Bean
    public CommandLineRunner processDataOnStartup(
            RequestProcessingService requestProcessingService,
            RequestContentRepository requestContentRepository,
            RegPersonRepository regPersonRepository,
            VerifiedNameRepository verifiedNameRepository,
            LevenshteinService levenshteinService,
            StringConcatenationService stringConcatenationService) {
        return args -> {
            requestProcessingService.processAllRequests();

            // Расчет Стоп-Фактора для всех обработанных запросов
            for (RequestContent request : requestContentRepository.findAll()) {
                String loanRequestId = request.getLoanRequestId();
                RegPerson regPerson = regPersonRepository.findByLoanRequestId(loanRequestId);
                VerifiedName verifiedName = verifiedNameRepository.findByLoanRequestId(loanRequestId);

                if (regPerson != null && verifiedName != null) {
                    String regPersonString = stringConcatenationService
                            .concatenateRegPersonFields(regPerson);
                    String verifiedNameString = stringConcatenationService
                            .concatenateVerifiedNameFields(verifiedName);

                    boolean stopFactor = levenshteinService
                            .calculateStopFactor(regPersonString, verifiedNameString);

                    // Log the result
                    System.out.println("Loan Request ID: " + loanRequestId);
                    System.out.println("RegPerson: " + regPersonString);
                    System.out.println("VerifiedName: " + verifiedNameString);
                    System.out.println("Stop Factor: " + stopFactor);
                    System.out.println();
                }
            }
        };
    }
} 