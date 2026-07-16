package com.alamo.alquiler.repository;

import com.alamo.alquiler.model.SoporteTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoporteTicketRepository extends JpaRepository<SoporteTicket, Integer> {
}
