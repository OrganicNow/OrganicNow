package com.organicnow.backend.repository;

import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.ContractFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractFileRepository extends JpaRepository<ContractFile, Long> {

    Optional<ContractFile> findByContract(Contract contract);

    boolean existsByContract(Contract contract);

    void deleteByContract(Contract contract);
}