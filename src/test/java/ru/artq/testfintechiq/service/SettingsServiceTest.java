package ru.artq.testfintechiq.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.artq.testfintechiq.model.Settings;
import ru.artq.testfintechiq.repository.SettingsRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettingsServiceTest {

    @Mock
    private SettingsRepository settingsRepository;

    @InjectMocks
    private SettingsService settingsService;

    @Test
    void getDistanceRatioThreshold_shouldReturnCorrectValue() {
        Settings settings = new Settings();
        settings.setName("distanceRatioThreshold");
        settings.setValue("0.9");
        when(settingsRepository.findByName("distanceRatioThreshold")).thenReturn(settings);

        double result = settingsService.getDistanceRatioThreshold();

        assertEquals(0.9, result);
    }
} 