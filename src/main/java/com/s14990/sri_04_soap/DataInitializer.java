package com.s14990.sri_04_soap;

import com.s14990.sri_04_soap.model.Ticket;
import com.s14990.sri_04_soap.repo.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG= LoggerFactory.getLogger(DataInitializer.class);

    private final TicketRepository ticketRepository;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        Ticket t1 = Ticket.builder().flightCode("aa1")
                .flightDate(LocalDate.of(2020,05,20)).seat("1a").price(10.0).build();
        Ticket t2 = Ticket.builder().flightCode("aa1")
                .flightDate(LocalDate.of(2020,05,20)).seat("2b").price(10.0).build();
        Ticket t3 = Ticket.builder().flightCode("aa1")
                .flightDate(LocalDate.of(2020,05,20)).seat("3c").price(10.0).build();
        Ticket t4 = Ticket.builder().flightCode("bb2")
                .flightDate(LocalDate.of(2020,05,20)).seat("1a").price(10.0).build();
        Ticket t5 = Ticket.builder().flightCode("bb2")
                .flightDate(LocalDate.of(2020,05,20)).seat("2a").price(10.0).build();
        ticketRepository.saveAll(Arrays.asList(t1,t2,t3,t4,t5));
        LOG.info("Data initialized");
    }
}
