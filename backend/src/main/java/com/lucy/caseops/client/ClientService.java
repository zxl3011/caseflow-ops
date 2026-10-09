package com.lucy.caseops.client;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PARTNER', 'LAWYER')")
    public ClientPageResponse list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "page must be zero or greater and size must be between 1 and 100"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("name").ascending().and(Sort.by("id").ascending())
        );
        Page<Client> clientPage = clientRepository.findAll(pageable);
        return ClientPageResponse.from(clientPage);
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
