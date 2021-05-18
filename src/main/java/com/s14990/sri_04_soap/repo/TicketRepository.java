package com.s14990.sri_04_soap.repo;


import com.s14990.sri_04_soap.model.Ticket;
import org.springframework.data.repository.CrudRepository;


import java.util.List;

public interface TicketRepository extends CrudRepository<Ticket,Long> {
    List<Ticket> findAll();
}
