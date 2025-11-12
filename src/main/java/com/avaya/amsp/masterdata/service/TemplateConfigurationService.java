package com.avaya.amsp.masterdata.service;


import com.avaya.amsp.domain.TemplateConfiguration;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.TemplateConfigurationDTO;
import com.avaya.amsp.masterdata.repo.TemplateConfigurationRepository;
import com.avaya.amsp.masterdata.service.iface.TemplateConfigurationServiceIface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TemplateConfigurationService implements TemplateConfigurationServiceIface {

    @Autowired
    private TemplateConfigurationRepository repository;

    @Override
    public List< TemplateConfigurationDTO > getAll() {

        return repository.fetchAll().stream()
                 .map(TemplateConfigurationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List< TemplateConfigurationDTO > fetchByClusterId(Long clusterId) {
        return repository.findByClusterIdAndActive(clusterId).stream()
                .map(TemplateConfigurationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @AuditLog(action = "INSERT",entity = "TemplateConfiguration",functionality = "Insert new Template")
    public void create(TemplateConfigurationDTO dto) {
        TemplateConfiguration entity = dto.toEntity();
        entity.setActive(1);
        repository.save(entity);
    }

    @Override
    @AuditLog(action = "Update",entity = "TemplateConfiguration",functionality = "update existing Template")
    public void update(Long id, TemplateConfigurationDTO dto) {

        repository.findById(id).ifPresentOrElse(existing->{
            dto.updateEntity(existing);
            repository.save(existing);
        },()->{
            log.info("TemplateConfiguration with ID {} not found",id);
        });
    }

    @Override
    @AuditLog(action = "delete",entity = "TemplateConfiguration",functionality = "delete existing Template")
    public void delete(Long id) {
        repository.removeById(id);
    }


    @Override
    public List< TemplateConfigurationDTO > fetchByClusterAndConnection(Long clusterId, Long connectionId) {
        return repository.findByClusterAndConnection(clusterId,connectionId).stream()
                .map(TemplateConfigurationDTO::fromEntity)
                .collect(Collectors.toList());
    }


}
