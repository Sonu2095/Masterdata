package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.PoolOrderIdSequence;

/**
 * 
 */
public interface PoolOrderIdSequenceRepo extends JpaRepository<PoolOrderIdSequence, Long> {

}
