package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avaya.amsp.domain.RoleAssignment;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {

    @Query("""
        SELECT ra.costCenter.name
        FROM RoleAssignment ra
        WHERE ra.user.username = :username AND ra.role = com.avaya.amsp.domain.enums.RoleGroup.CC_RESP
        """)
    List<String> findCostCentersForUser(@Param("username") String username);
}
