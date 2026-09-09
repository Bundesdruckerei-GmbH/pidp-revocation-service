/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.lifecycle.adapter.in.rest;

import de.bdr.pidp.revocation.lifecycle.adapter.in.rest.api.PidMasterTokenApi;
import de.bdr.pidp.revocation.lifecycle.adapter.in.rest.api.model.ErrorResponseDTO;
import de.bdr.pidp.revocation.lifecycle.adapter.in.rest.api.model.TokenInfoDTO;
import de.bdr.pidp.revocation.lifecycle.adapter.in.rest.api.model.TokenStatusDTO;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenIDAlreadyExistsException;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenInfo;
import de.bdr.pidp.revocation.lifecycle.app.domain.TokenNotFoundException;
import de.bdr.pidp.revocation.lifecycle.app.service.PidMasterTokenService;
import de.bdr.pidp.revocation.shared.domain.StatusListRef;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PidMasterTokenController implements PidMasterTokenApi {
    private final PidMasterTokenService tokenService;

    PidMasterTokenController(PidMasterTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void submitTokenInfo(TokenInfoDTO tokenInfo) {
        var pmt = new TokenInfo(
            tokenInfo.getTokenID(),
            tokenInfo.getPseudonym(),
            tokenInfo.getExpiration().toInstant()
        );
        var statusListRef = tokenInfo.getStatusListRef();
        if (statusListRef != null) {
            var pmtStatusRef = new StatusListRef(
                statusListRef.getUri(),
                statusListRef.getIndex()
            );
            tokenService.initLifecycle(pmt, pmtStatusRef);
        } else {
            tokenService.initLifecycle(pmt, null);
        }
    }

    @Override
    public TokenStatusDTO getTokenStatus(String tokenID) {
        var status = tokenService.getLifecycleStatus(tokenID);
        var statusDTO = switch (status) {
            case VALID -> TokenStatusDTO.StatusEnum.VALID;
            case INVALID -> TokenStatusDTO.StatusEnum.INVALID;
        };
        return new TokenStatusDTO(statusDTO);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TokenIDAlreadyExistsException.class)
    ErrorResponseDTO tokenIDAlreadyExists(TokenIDAlreadyExistsException e) {
        var response = new ErrorResponseDTO();
        response.error("Lifecycle already exists");
        if (e.getMessage() != null) {
            response.details(e.getMessage());
        }
        return response;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TokenNotFoundException.class)
    ErrorResponseDTO tokenNotFound(TokenNotFoundException e) {
        var response = new ErrorResponseDTO();
        response.error("Lifecycle not found");
        if (e.getMessage() != null) {
            response.details(e.getMessage());
        }
        return response;
    }
}
