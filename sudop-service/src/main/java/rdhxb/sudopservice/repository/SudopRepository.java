package rdhxb.sudopservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdhxb.sudopservice.entity.SudopModel;

import java.util.UUID;

public interface SudopRepository extends JpaRepository<SudopModel, UUID> {
}
