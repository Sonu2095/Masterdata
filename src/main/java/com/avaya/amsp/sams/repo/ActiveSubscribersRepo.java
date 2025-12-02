/**
 * 
 */
package com.avaya.amsp.sams.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.avaya.amsp.domain.ActiveSubscribers;

/**
 * 
 */
public interface ActiveSubscribersRepo extends JpaRepository<ActiveSubscribers, String>, JpaSpecificationExecutor<ActiveSubscribers> {

    Optional<List<ActiveSubscribers>> findByAreaCodeAndExtension(String areaCode, String extension);
}
