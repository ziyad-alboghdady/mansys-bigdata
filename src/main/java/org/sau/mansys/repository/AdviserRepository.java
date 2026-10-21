package org.sau.mansys.repository;

import org.sau.mansys.models.Adviser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdviserRepository extends JpaRepository<Adviser, Long> {

    @Query("""
           SELECT a FROM Adviser a
           LEFT JOIN FETCH a.headAdviser
           ORDER BY a.name
           """)
    List<Adviser> findAllWithHead();
}
