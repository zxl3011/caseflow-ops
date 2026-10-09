package com.lucy.caseops.client;

import org.springframework.data.domain.Page;

import java.util.List;

public record ClientPageResponse(
        List<ClientResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
){
    public static ClientPageResponse from(Page<Client> clientPage) {
        return new ClientPageResponse(
                clientPage.getContent()
                        .stream()
                        .map(ClientResponse::from)
                        .toList(),
                clientPage.getNumber(),
                clientPage.getSize(),
                clientPage.getTotalElements(),
                clientPage.getTotalPages()
        );
    }

}
