package com.pulsepass.service.impl;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.User;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {
    private static final BigDecimal GENERAL_PRICE = new BigDecimal("100.00");
    private static final BigDecimal STUDENT_DISCOUNT = new BigDecimal("0.80");
    private static final BigDecimal VIP_MULTIPLIER = new BigDecimal("1.50");
    private static final BigDecimal BACKSTAGE_MULTIPLIER = new BigDecimal("2.00");

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             EventRepository eventRepository,
                             TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketMapper = ticketMapper;
    }

    @Override
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userEmail()));
        if (!user.isActive()) {
            throw new BusinessRuleException("Inactive users cannot purchase tickets.");
        }

        Event event = eventRepository.findByEventCode(request.eventCode())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + request.eventCode()));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException("Tickets can only be purchased for PUBLISHED events.");
        }
        if (!event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot purchase a ticket for an event that already occurred.");
        }

        validateAge(user, event);

        long paidTickets = ticketRepository.countPaidTicketsByEventCode(event.getEventCode());
        if (paidTickets >= event.getVenue().getCapacity()) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
            throw new BusinessRuleException("The event has no available capacity.");
        }

        TicketType type = request.type() == null ? TicketType.GENERAL : request.type();
        BigDecimal price = calculatePrice(type);
        String ticketCode = UUID.randomUUID().toString();
        Ticket ticket = new Ticket(ticketCode, type, price, TicketStatus.PAID,
                LocalDateTime.now(), user, event);
        Ticket saved = ticketRepository.save(ticket);

        if (paidTickets + 1 >= event.getVenue().getCapacity()) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
        }

        return ticketMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse findByCode(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .map(ticketMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findByUserEmail(String email) {
        return ticketRepository.findByUser_EmailIgnoreCase(email).stream()
                .sorted(Comparator.comparing(Ticket::getPurchaseDate).reversed())
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode) {
        return ticketRepository.findByEvent_EventCodeAndStatus(eventCode, TicketStatus.PAID).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse cancel(String ticketCode) {
        Ticket ticket = getTicket(ticketCode);
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Only PAID tickets can be cancelled.");
        }
        if (!ticket.getEvent().getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("A ticket cannot be cancelled after the event date.");
        }
        ticket.setStatus(TicketStatus.CANCELLED);
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {
        Ticket ticket = getTicket(ticketCode);
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Only PAID tickets can be marked as used.");
        }
        ticket.setStatus(TicketStatus.USED);
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    private Ticket getTicket(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));
    }

    private void validateAge(User user, Event event) {
        Integer minimumAge = event.getMinimumAge();
        if (minimumAge == null || minimumAge == 0) {
            return;
        }
        if (user.getProfile() == null || user.getProfile().getBirthDate() == null) {
            throw new BusinessRuleException("User birth date is required to validate minimum age.");
        }
        LocalDate eventDate = event.getEventDate().toLocalDate();
        int age = Period.between(user.getProfile().getBirthDate(), eventDate).getYears();
        if (age < minimumAge) {
            throw new BusinessRuleException("User does not meet minimum age.");
        }
    }

    private BigDecimal calculatePrice(TicketType type) {
        BigDecimal price = switch (type) {
            case GENERAL -> GENERAL_PRICE;
            case STUDENT -> GENERAL_PRICE.multiply(STUDENT_DISCOUNT);
            case VIP -> GENERAL_PRICE.multiply(VIP_MULTIPLIER);
            case BACKSTAGE -> GENERAL_PRICE.multiply(BACKSTAGE_MULTIPLIER);
        };
        if (price.signum() < 0) {
            throw new BusinessRuleException("Ticket price cannot be negative.");
        }
        return price.setScale(2);
    }
}
