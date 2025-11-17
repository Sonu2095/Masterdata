package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.Connection;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

	public Connection findByName(String name);

	@Query(value = "SELECT C from CONNECTION C LEFT JOIN FETCH C.connectionPortType LEFT JOIN FETCH C.connectionPortType.portType where C.active=1 ORDER BY C.id ASC")
	public List<Connection> findAll();

	@Query(value = "SELECT C from CONNECTION C where C.active=1 ORDER BY C.id ASC")
	public List<Connection> findArticleByConnection();

}
