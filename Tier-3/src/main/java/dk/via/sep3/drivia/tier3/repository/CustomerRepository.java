package dk.via.sep3.drivia.tier3.repository;

import dk.via.sep3.drivia.tier3.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

// Only finds customers, so a reservation can never be made for an employee
public interface CustomerRepository extends JpaRepository<Customer, Integer>{
}
