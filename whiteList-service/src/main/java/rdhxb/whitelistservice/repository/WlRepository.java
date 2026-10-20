package rdhxb.whitelistservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rdhxb.whitelistservice.entity.WlModel;

import java.util.UUID;

@Repository
public interface WlRepository extends JpaRepository<WlModel, UUID> {
    boolean existsByNip(String nip);

    WlModel getWlModelByNip(String nip);
}
