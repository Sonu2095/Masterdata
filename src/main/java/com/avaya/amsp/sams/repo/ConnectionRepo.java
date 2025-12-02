package com.avaya.amsp.sams.repo;

import com.avaya.amsp.domain.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConnectionRepo extends JpaRepository< Connection,Long > {

	@Query(value = "SELECT C from CONNECTION C where C.id = :id and C.active=1 ORDER BY C.id ASC")
	public Optional<Connection> findConnectionById(Long id);
	
    @Query(value = "SELECT C from CONNECTION C where LOWER(C.name)= :connectionType and C.active=1 ORDER BY C.id ASC")
    public Optional<Connection> findConnectionByNames(String connectionType);

}
