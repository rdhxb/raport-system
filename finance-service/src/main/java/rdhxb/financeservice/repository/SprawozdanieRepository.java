package rdhxb.financeservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdhxb.financeservice.entity.SprawozdanieModel;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SprawozdanieRepository extends JpaRepository<SprawozdanieModel, UUID> {
    SprawozdanieModel findFirstByKrsOrderByOkresDoDesc(String krs);

    List<SprawozdanieModel> findAllByKrsOrderByOkresDoDesc(String krsNum);


    SprawozdanieModel findByOkresDoAndKrs(LocalDate okresDo, String krs);
}
