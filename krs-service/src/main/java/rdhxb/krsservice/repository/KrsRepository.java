package rdhxb.krsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdhxb.krsservice.model.KrsModel;

import java.util.UUID;

public interface KrsRepository extends JpaRepository<KrsModel, UUID> {
}
