package ru.artq.testfintechiq.service;

import org.springframework.stereotype.Service;
import ru.artq.testfintechiq.repository.SettingsRepository;

@Service
public record SettingsService(SettingsRepository settingsRepository) {
    public double getDistanceRatioThreshold() {
        return Double.parseDouble(settingsRepository.findByName("distanceRatioThreshold").getValue());
    }
}