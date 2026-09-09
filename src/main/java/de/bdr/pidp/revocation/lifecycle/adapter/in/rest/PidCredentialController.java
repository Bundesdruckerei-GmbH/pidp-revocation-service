/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.in.rest;

import de.bdr.pidp.revocation.lifecycle.adapter.in.rest.api.PidCredentialApi;
import de.bdr.pidp.revocation.lifecycle.adapter.in.rest.api.model.BatchCredentialInfoDTO;
import de.bdr.pidp.revocation.lifecycle.adapter.in.rest.api.model.ErrorResponseDTO;
import de.bdr.pidp.revocation.lifecycle.app.domain.CredentialInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.PidCredentialLifecycleAlreadyExistsException;
import de.bdr.pidp.revocation.lifecycle.app.domain.PidMasterTokenNotExistException;
import de.bdr.pidp.revocation.lifecycle.app.service.PidCredentialService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class PidCredentialController implements PidCredentialApi {
    private final PidCredentialService pidCredentialService;

    PidCredentialController(PidCredentialService pidCredentialService) {
        this.pidCredentialService = pidCredentialService;
    }

    @Override
    public void submitBatchCredentialInfo(BatchCredentialInfoDTO batchCredentialInfoDTO) {
        List<CredentialInfo> credentialInfoList = batchCredentialInfoDTO.getCredentialList().stream().map(dto ->
                new CredentialInfo(
                    dto.getExpiration().toInstant(),
                    dto.getTokenStatusListRef().getUri(),
                    dto.getTokenStatusListRef().getIndex()))
            .toList();
        pidCredentialService.initPidCredentialLifecycle(batchCredentialInfoDTO.getPidMasterTokenID(), credentialInfoList);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PidMasterTokenNotExistException.class)
    ErrorResponseDTO pidMasterTokenNotExists(PidMasterTokenNotExistException e) {
        var response = new ErrorResponseDTO();
        response.error("Pid Master Token not exists");
        if (e.getMessage() != null) {
            response.details(e.getMessage());
        }
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PidCredentialLifecycleAlreadyExistsException.class)
    ErrorResponseDTO pidCredentialLifecycleAlreadyExists(PidCredentialLifecycleAlreadyExistsException e) {
        var response = new ErrorResponseDTO();
        response.error("Pid Credential Lifecycle already exists");
        if (e.getMessage() != null) {
            response.details(e.getMessage());
        }
        return response;
    }

}
