package rdhxb.ceidgservice.repostory;

import org.springframework.data.jpa.repository.JpaRepository;
import rdhxb.ceidgservice.entity.CeidgModel;

import java.util.UUID;

public interface CeidgRepository extends JpaRepository<CeidgModel, UUID> {
    CeidgModel findByNip(String nip);
}
