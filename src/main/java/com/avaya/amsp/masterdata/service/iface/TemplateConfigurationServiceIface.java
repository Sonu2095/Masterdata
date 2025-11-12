package com.avaya.amsp.masterdata.service.iface;

import com.avaya.amsp.masterdata.dtos.TemplateConfigurationDTO;

import java.util.List;
import java.util.Optional;

public interface TemplateConfigurationServiceIface {

    public List< TemplateConfigurationDTO > getAll();

    public List< TemplateConfigurationDTO > fetchByClusterId(Long clusterId);

    public void create(TemplateConfigurationDTO dto);

    public void update(Long id, TemplateConfigurationDTO dto);

    public void delete(Long id);

    public List< TemplateConfigurationDTO > fetchByClusterAndConnection(Long clusterId,Long connectionId);


};


