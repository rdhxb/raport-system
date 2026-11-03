package rdhxb.raportservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdhxb.raportservice.entity.Raport;

import java.util.UUID;

public interface RaportRepository extends JpaRepository<Raport, UUID> {

}
