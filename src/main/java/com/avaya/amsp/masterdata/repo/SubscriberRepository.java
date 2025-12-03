package com.avaya.amsp.masterdata.repo;

import com.avaya.amsp.domain.Subscribers;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;


@Repository
public interface SubscriberRepository extends JpaRepository<Subscribers, Long>, JpaSpecificationExecutor<Subscribers> {
    // Method to find subscribers by clusterId, already exists
	
    Page<Subscribers> findByIdCluster(Long clusterId, Pageable pageable);
    
    @Query("SELECT DISTINCT s.bcsBunch FROM Subscribers s")
    List<String> findDistinctBcsBunch();

    // Method to get distinct msnMaster values
    @Query("SELECT DISTINCT s.msnMaster FROM Subscribers s")
    List<String> findDistinctMsnMaster();

    // Method to get distinct currentState values
    @Query("SELECT DISTINCT s.currentState FROM Subscribers s")
    List<String> findDistinctCurrentState();
    
    boolean existsByAreaCodeAndExtension(String areaCode, String extension);
    
    Optional<Subscribers> findByAreaCodeAndExtension(String areaCode, String extension);
    
    @Query(value = "SELECT * FROM subscribers s WHERE s.area_code = :areaCode " +
           "AND CAST(s.extension AS UNSIGNED) >= CAST(:fromExtension AS UNSIGNED) " +
           "AND CAST(s.extension AS UNSIGNED) <= CAST(:toExtension AS UNSIGNED) " +
           "AND (:status IS NULL OR s.current_state = :status) " +
           "ORDER BY CAST(s.extension AS UNSIGNED)", nativeQuery = true)
    List<Subscribers> findByAreaCodeAndExtensionRange(
        String areaCode, 
        String fromExtension, 
        String toExtension, 
        String status
    );

    @Query(value = "SELECT * FROM subscribers s WHERE s.nt_domain = :domain AND s.nt_username = :userID " +
           "ORDER BY CAST(s.extension AS UNSIGNED)", nativeQuery = true)
    List<Subscribers> findByDomainAndUser(String domain, String userID);

}
