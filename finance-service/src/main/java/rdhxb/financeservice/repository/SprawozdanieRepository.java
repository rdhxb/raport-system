package rdhxb.financeservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdhxb.financeservice.entity.SprawozdanieModel;

import java.util.UUID;

public interface SprawozdanieRepository extends JpaRepository<SprawozdanieModel, UUID> {
    SprawozdanieModel findFirstByKrsOrderByOkresDoDesc(String krs);
}
