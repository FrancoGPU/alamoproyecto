package com.alamo.alquiler.controller;

import com.alamo.alquiler.model.SoporteTicket;
import com.alamo.alquiler.repository.SoporteTicketRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/soporte")
public class SoporteTicketController extends AbstractCrudController<SoporteTicket, Integer> {

    private final SoporteTicketRepository repo;

    public SoporteTicketController(SoporteTicketRepository repo) {
        this.repo = repo;
    }

    @Override
    protected JpaRepository<SoporteTicket, Integer> repo() {
        return repo;
    }
}
