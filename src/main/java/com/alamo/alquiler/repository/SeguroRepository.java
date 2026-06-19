package com.alamo.alquiler.repository;

import com.alamo.alquiler.model.Seguro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguroRepository extends JpaRepository<Seguro, Integer> {}

