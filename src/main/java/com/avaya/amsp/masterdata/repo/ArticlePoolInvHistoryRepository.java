package com.avaya.amsp.masterdata.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.ArticlePoolInventoryHistory;

public interface ArticlePoolInvHistoryRepository extends JpaRepository<ArticlePoolInventoryHistory, Long>{

	@Query(value="select h from ArticlePoolInventoryHistory h where h.pool.id=:poolId")
	public List<ArticlePoolInventoryHistory> findByPoolId(String poolId);

	public Optional<List<ArticlePoolInventoryHistory>> findByReasonTextAndOperation(String reasonText, String operation);
}


