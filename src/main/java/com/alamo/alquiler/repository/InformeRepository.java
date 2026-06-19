package com.alamo.alquiler.repository;

import com.alamo.alquiler.model.Informe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InformeRepository extends JpaRepository<Informe, Integer> {}

