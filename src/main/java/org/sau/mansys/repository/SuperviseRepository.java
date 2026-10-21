package org.sau.mansys.repository;

import org.sau.mansys.models.Supervise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SuperviseRepository extends JpaRepository<Supervise, Long> {

    @Query("""
           SELECT sv FROM Supervise sv
           JOIN FETCH sv.adviser a
           LEFT JOIN FETCH a.headAdviser
           JOIN FETCH sv.study
           ORDER BY a.name, sv.student
           """)
    List<Supervise> findAllDetailed();
}
