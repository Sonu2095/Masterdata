/**
 * 
 */
package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.util.Streamable;

import com.avaya.amsp.domain.PoolOrderFiles;

/**
 * 
 */
public interface PoolOrderFilesRepo extends JpaRepository<PoolOrderFiles, String> {
	
	@Query("SELECT pof FROM PoolOrderFiles pof WHERE pof.poolOrderItem.id = :orderId")
	List<PoolOrderFiles> findByOrderId(String orderId);

	PoolOrderFiles findByFileId(String fileId);
	 
	/*@Query("SELECT count(*) FROM OrderFiles WHERE amspFileId = :amspFileId") Long
	 * countByamspFileId(String amspFileId);
	 */

}
