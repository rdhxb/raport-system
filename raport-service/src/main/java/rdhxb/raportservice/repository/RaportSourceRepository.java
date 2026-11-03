package rdhxb.raportservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdhxb.raportservice.entity.RaportSource;

import java.util.UUID;

public interface RaportSourceRepository extends JpaRepository<RaportSource, UUID> {
}
