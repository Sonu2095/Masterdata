package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.ActiveSubscribers;

public interface ActiveSubscribersRepository extends JpaRepository<ActiveSubscribers, String> {
	
	List<ActiveSubscribers> findByPbxClusterIdAndPbxSystemId(Long pbxClusterId, Long pbxSystemId);

}
