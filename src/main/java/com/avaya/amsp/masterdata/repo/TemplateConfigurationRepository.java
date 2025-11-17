package com.avaya.amsp.masterdata.repo;

import com.avaya.amsp.domain.TemplateConfiguration;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TemplateConfigurationRepository extends JpaRepository< TemplateConfiguration,Long> {

    @EntityGraph(value = "TemplateConfiguration.withClusterAndConnection")
    @Query("SELECT t FROM TemplateConfiguration t WHERE t.active = 1")
    List<TemplateConfiguration> fetchAll();

    @Modifying
    @Transactional
    @Query("UPDATE TemplateConfiguration t SET t.active = 0 WHERE t.id = :id")
    void removeById(@Param("id") Long id);

    @EntityGraph(value = "TemplateConfiguration.withClusterAndConnection")
    @Query("SELECT t FROM TemplateConfiguration t WHERE t.cluster.id = :clusterId AND t.active = 1")
    List<TemplateConfiguration> findByClusterIdAndActive(@Param("clusterId") Long clusterId);


    @EntityGraph(value = "TemplateConfiguration.withClusterAndConnection")
    @Query("SELECT t FROM TemplateConfiguration t WHERE t.cluster.id = :clusterId AND t.connection.id = :connectionId AND t.active = 1")
    List<TemplateConfiguration> findByClusterAndConnection(@Param("clusterId") Long clusterId,@Param("connectionId") Long connectionId);

}
