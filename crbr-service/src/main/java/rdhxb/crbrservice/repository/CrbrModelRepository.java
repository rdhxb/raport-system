package rdhxb.crbrservice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import rdhxb.crbrservice.entity.CrbrModel;

import java.util.Optional;
import java.util.UUID;

public interface CrbrModelRepository extends JpaRepository<CrbrModel, UUID> {
    Optional<CrbrModel> findByKrsNumber(String krsNumber);
}
