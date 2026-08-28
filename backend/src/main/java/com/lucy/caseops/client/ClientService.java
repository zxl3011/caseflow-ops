package com.lucy.caseops.client;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('PARTNER', 'LAWYER')")
    public ClientResponse create(CreateClientRequest request) {
        String normalisedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (clientRepository.existsByEmailIgnoreCase(normalisedEmail)) {
            throw duplicateEmail();
        }

        Client client = Client.create(
                request.name().trim(),
                normalisedEmail,
                trimToNull(request.phone())
        );
        try {
            return ClientResponse.from(clientRepository.saveAndFlush(client));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A client with this email already exists",
                    exception
            );
        }
    }

    private ResponseStatusException duplicateEmail() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A client with this email already exists"
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
