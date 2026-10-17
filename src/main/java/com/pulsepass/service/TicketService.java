package com.pulsepass.service;

import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import java.util.List;
import java.util.Collection;

public interface TicketService {
    TicketResponse purchase(PurchaseTicketRequest request);
    TicketResponse findByCode(String ticketCode);
    List<TicketResponse> findByUserEmail(String email);
    List<TicketResponse> findPaidTicketsByEvent(String eventCode);
    TicketResponse cancel(String ticketCode);
    TicketResponse markAsUsed(String ticketCode);

    void authorizePurchase(String email, Long principalId, Collection<String> roles);
    void authorizeTicketRead(String ticketCode, Long principalId, Collection<String> roles);
    void authorizeTicketCancel(String ticketCode, Long principalId, Collection<String> roles);
    void authorizeUserTicketList(String email, Long principalId, Collection<String> roles);
}
