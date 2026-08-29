package com.lucy.caseops.client;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClientServiceTest {

    private final ClientRepository clientRepository = mock(ClientRepository.class);
    private final ClientService clientService = new ClientService(clientRepository);

    @Test
    void normalisesClientDetailsBeforeSaving() {
        when(clientRepository.existsByEmailIgnoreCase("client@example.com"))
                .thenReturn(false);
        when(clientRepository.saveAndFlush(any(Client.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClientResponse response = clientService.create(new CreateClientRequest(
                "  Example Client  ",
                "CLIENT@example.com",
                "   "
        ));

        assertThat(response.name()).isEqualTo("Example Client");
        assertThat(response.email()).isEqualTo("client@example.com");
        assertThat(response.phone()).isNull();
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void rejectsKnownDuplicateBeforeSaving() {
        when(clientRepository.existsByEmailIgnoreCase("duplicate@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> clientService.create(new CreateClientRequest(
                "Duplicate Client",
                "DUPLICATE@example.com",
                null
        )))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode().value()
                ).isEqualTo(409));

        verify(clientRepository, never()).saveAndFlush(any(Client.class));
    }

    @Test
    void translatesDatabaseRaceConflictToHttpConflict() {
        when(clientRepository.existsByEmailIgnoreCase("race@example.com"))
                .thenReturn(false);
        when(clientRepository.saveAndFlush(any(Client.class)))
                .thenThrow(new DataIntegrityViolationException("unique index conflict"));

        assertThatThrownBy(() -> clientService.create(new CreateClientRequest(
                "Race Client",
                "race@example.com",
                null
        )))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode().value()
                ).isEqualTo(409));
    }
}
