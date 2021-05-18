package com.s14990.sri_04_soap.endpoints;

import com.s14990.sri_04_soap.config.SoapWSConfig;
import com.s14990.sri_04_soap.model.Ticket;
import com.s14990.sri_04_soap.repo.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import sri04.tickets.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Endpoint
@RequiredArgsConstructor
public class TicketsEndpoints {

    private final TicketRepository ticketRepository;
    private long cFlightId = 1;
    private long refundId = 1;

    @PayloadRoot(namespace = SoapWSConfig.TICKETS_NAMESPACE, localPart = "getTicketsRequest")
    @ResponsePayload
    public GetTicketsResponse getTickets(@RequestPayload GetTicketsRequest req) {
        List<Ticket> ticketlist = ticketRepository.findAll();
        List<TicketDto> dtoList = ticketlist.stream().map(this::convertToDto)
                .collect(Collectors.toList());
        GetTicketsResponse res = new GetTicketsResponse();
        res.getTickets().addAll(dtoList);
        return res;
    }


    @PayloadRoot(namespace = SoapWSConfig.TICKETS_NAMESPACE, localPart = "addTicketRequest")
    @ResponsePayload
    public AddTicketResponse addTicket(@RequestPayload AddTicketRequest req) {

        TicketDto dto = req.getTicket();
        Ticket t = convertToEntity(dto);
        ticketRepository.save(t);
        AddTicketResponse resp = new AddTicketResponse();
        resp.setTicketId(t.getId());
        return resp;
    }

    //Delete all tickets for this flight and calculate refund sum for all tickets
    @PayloadRoot(namespace = SoapWSConfig.TICKETS_NAMESPACE, localPart = "cancelFlightRequest")
    @ResponsePayload
    public CancelFlightResponse cancelFlight(@RequestPayload CancelFlightRequest req) {

        String flight_code = req.getRequest().getFlightCode();
        List<Ticket> ticketlist = ticketRepository.findAll().stream().filter(t -> t.getFlightCode().equals(flight_code)).collect(Collectors.toList());
        double return_sum = 0;
        CancelFlightResponse resp = new CancelFlightResponse();
        CancelFlightResponseDto rd = new CancelFlightResponseDto();
        if (!ticketlist.isEmpty()) {
            return_sum = ticketlist.stream().mapToDouble(Ticket::getPrice).sum();
            ticketRepository.deleteAll(ticketlist);
            rd.setId(cFlightId += 1);
            rd.setRefundFullSum(return_sum);
        } else {
            rd.setId(0L);
            rd.setRefundFullSum(0);
        }
        resp.setResponse(rd);
        return resp;
    }

    //Reset registered owner and date, and return refund sum
    @PayloadRoot(namespace = SoapWSConfig.TICKETS_NAMESPACE, localPart = "refundTicketRequest")
    @ResponsePayload
    public RefundTicketResponse refundTicket(@RequestPayload RefundTicketRequest req) {

        long ticket_id = req.getRequest().getTicketId();
        String flight_code = req.getRequest().getFlightCode();
        Optional<Ticket> ticket = ticketRepository.findAll().stream().
                filter(t -> t.getFlightCode().equals(flight_code) && t.getId() == ticket_id && t.getOwner() != null).findFirst();
        RefundTicketResponse resp = new RefundTicketResponse();
        RefundResponseDto rd = new RefundResponseDto();
        double return_sum = 0;
        if (ticket.isPresent()) {
            return_sum = ticket.get().getPrice();
            ticket.get().setOwner(null);
            ticket.get().setRegisterDate(null);
            ticketRepository.save(ticket.get());
            rd.setId(refundId += 1);
            rd.setReturnSum(return_sum);
        } else {
            rd.setId(0L);
            rd.setReturnSum(0);
        }
        resp.setResponse(rd);
        return resp;
    }

    //If ticket exists set new owner and register date
    @PayloadRoot(namespace = SoapWSConfig.TICKETS_NAMESPACE, localPart = "buyTicketRequest")
    @ResponsePayload
    public BuyTicketResponse buyTicket(@RequestPayload BuyTicketRequest req) throws DatatypeConfigurationException {
        long ticket_id = req.getRequest().getTicketId();
        String new_owner = req.getRequest().getOwner();
        Optional<Ticket> ticket = ticketRepository.findAll().stream().filter(t -> t.getId() == ticket_id).findFirst();
        BuyTicketResponse resp = new BuyTicketResponse();
        if (ticket.isPresent()) {
            ticket.get().setOwner(new_owner);
            ticket.get().setRegisterDate(LocalDate.now());
            ticketRepository.save(ticket.get());
            resp.setResponse(convertToDto(ticket.get()));
        } else {
            return null;
        }
        return resp;
    }


    private TicketDto convertToDto(Ticket t) {
        if (t == null) {
            return null;
        }
        try {
            TicketDto dto = new TicketDto();
            dto.setId(t.getId());
            dto.setCode(t.getFlightCode());
            dto.setFlightDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(t.getFlightDate().toString()));
            dto.setRegisterDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(t.getFlightDate().toString()));
            dto.setOwner(t.getOwner());
            dto.setPrice(t.getPrice());
            dto.setSeat(t.getSeat());
            return dto;
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Ticket convertToEntity(TicketDto dto) {
        return Ticket.builder().id(dto.getId())
                .flightCode(dto.getCode())
                .seat(dto.getSeat())
                .flightDate(dto.getFlightDate().toGregorianCalendar().toZonedDateTime().toLocalDate())
                .price(dto.getPrice())
                .owner(dto.getOwner())
                .registerDate(dto.getRegisterDate().toGregorianCalendar().toZonedDateTime().toLocalDate())
                .build();

    }
}
