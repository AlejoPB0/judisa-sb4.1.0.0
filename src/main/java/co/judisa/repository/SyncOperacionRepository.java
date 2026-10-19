package co.judisa.repository;

import co.judisa.model.entity.SyncOperacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncOperacionRepository extends JpaRepository<SyncOperacion, String> {
}
