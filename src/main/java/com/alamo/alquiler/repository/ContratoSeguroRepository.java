package com.alamo.alquiler.repository;

import com.alamo.alquiler.model.ContratoSeguro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratoSeguroRepository extends JpaRepository<ContratoSeguro, Integer> {}

