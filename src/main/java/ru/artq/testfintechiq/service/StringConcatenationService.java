package ru.artq.testfintechiq.service;

import org.springframework.stereotype.Service;
import ru.artq.testfintechiq.model.RegPerson;
import ru.artq.testfintechiq.model.VerifiedName;

@Service
public class StringConcatenationService {

    public String concatenateRegPersonFields(RegPerson regPerson) {
        if (regPerson == null) return "";

        StringBuilder sb = new StringBuilder();
        if (regPerson.getFirstName() != null) {
            sb.append(regPerson.getFirstName()).append(" ");
        }
        if (regPerson.getMiddleName() != null) {
            sb.append(regPerson.getMiddleName()).append(" ");
        }
        if (regPerson.getLastName() != null) {
            sb.append(regPerson.getLastName());
        }
        return sb.toString().trim();
    }

    public String concatenateVerifiedNameFields(VerifiedName verifiedName) {
        if (verifiedName == null) return "";

        StringBuilder sb = new StringBuilder();
        if (verifiedName.getFirstName() != null) {
            sb.append(verifiedName.getFirstName()).append(" ");
        }
        if (verifiedName.getOtherName() != null) {
            sb.append(verifiedName.getOtherName()).append(" ");
        }
        if (verifiedName.getSurname() != null) {
            sb.append(verifiedName.getSurname());
        }
        return sb.toString().trim();
    }
} 