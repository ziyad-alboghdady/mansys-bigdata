package org.sau.mansys.service;

import org.sau.mansys.models.Adviser;
import org.sau.mansys.models.Supervise;
import org.sau.mansys.repository.AdviserRepository;
import org.sau.mansys.repository.SuperviseRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdviserService {

    private final AdviserRepository adviserRepo;
    private final SuperviseRepository superviseRepo;

    public AdviserService(AdviserRepository adviserRepo,
                          SuperviseRepository superviseRepo) {
        this.adviserRepo = adviserRepo;
        this.superviseRepo = superviseRepo;
    }

    /* ---------- READ: served from Redis after the first call ---------- */

    @Cacheable(value = "report", key = "'all'")
    @Transactional(readOnly = true)
    public List<Supervise> report() {
        System.out.println(">>> CACHE MISS: hitting MySQL for the report");
        return superviseRepo.findAllDetailed();
    }

    @Cacheable(value = "advisers", key = "'all'")
    @Transactional(readOnly = true)
    public List<Adviser> findAll() {
        System.out.println(">>> CACHE MISS: hitting MySQL for all advisers");
        return adviserRepo.findAllWithHead();
    }

    @Cacheable(value = "advisers", key = "#id")
    @Transactional(readOnly = true)
    public Adviser findById(Long id) {
        System.out.println(">>> CACHE MISS: hitting MySQL for adviser " + id);
        return adviserRepo.findById(id).orElse(null);
    }

    /* ---------- WRITE: refresh this entry, drop the stale lists ---------- */

    @Caching(
            put = { @CachePut(value = "advisers", key = "#result.id") },
            evict = { @CacheEvict(value = "advisers", key = "'all'"),
                      @CacheEvict(value = "report", allEntries = true) }
    )
    @Transactional
    public Adviser save(Adviser adviser) {
        return adviserRepo.save(adviser);
    }

    /* ---------- DELETE: remove it everywhere ---------- */

    @Caching(evict = {
            @CacheEvict(value = "advisers", key = "#id"),
            @CacheEvict(value = "advisers", key = "'all'"),
            @CacheEvict(value = "report", allEntries = true)
    })
    @Transactional
    public void delete(Long id) {
        adviserRepo.deleteById(id);
    }
}
