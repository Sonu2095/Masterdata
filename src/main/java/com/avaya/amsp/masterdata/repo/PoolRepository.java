package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.Pool;

public interface PoolRepository extends JpaRepository<Pool, Long> {

	public Pool findByName(String name);

	public List<Pool> findByActive(Long active);

}
