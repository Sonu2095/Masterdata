package com.avaya.amsp.masterdata.dtos;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.Connection;
import com.avaya.amsp.domain.Site;
import com.avaya.amsp.domain.TemplateConfiguration;
import com.avaya.amsp.domain.enums.TemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TemplateConfigurationDTO {

    public Long id;

    @NotNull
    @Size(min = 1, max = 255, message = "name should not be blank")
    public String name;

    public String description;
    public String availableForRoles;

    @NotBlank(message = "user is required")
    public String user;

    @NotBlank
    @Pattern(regexp = "CM|SMGR|1XM", message = "Type must be CM/SMGR/1XM")
    public String type;

    public Long parentId;

    public Long clusterId;

    public Long connectionId;

    public String connectionName;


    public static TemplateConfigurationDTO fromEntity(TemplateConfiguration entity) {
        TemplateConfigurationDTO dto = new TemplateConfigurationDTO();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.description = entity.getDescription();
        dto.availableForRoles = entity.getAvailableForRoles();
        dto.user = entity.getLogCreatedBy();
        dto.type = entity.getType() != null ? entity.getType().getValue() : null;
        dto.parentId = entity.getParent() != null ? entity.getParent().getId() : null;
        dto.clusterId = entity.getCluster() != null ? entity.getCluster().getId() : null;
        if(entity.getConnection() != null) {
            dto.connectionId=entity.getConnection().getId();
            dto.connectionName = entity.getConnection().getName();
        }
        return dto;
    }

    public TemplateConfiguration toEntity() {
        TemplateConfiguration entity = new TemplateConfiguration();
        entity.setName(this.name);
        entity.setDescription(this.description);
        entity.setAvailableForRoles(this.availableForRoles);
        entity.setLogCreatedBy(this.user);
        entity.setType(this.type != null ? TemplateType.fromValue(this.type) : null);
        if (this.parentId != null) {
            TemplateConfiguration parent = new TemplateConfiguration();
            parent.setId(this.parentId);
            entity.setParent(parent);
        }
        if( this.clusterId != null) {
            entity.setCluster(new ClusterItem(this.clusterId));
        }
        if( this.connectionId != null) {
            entity.setConnection(new Connection(this.connectionId));
        }
        return entity;
    }

    public void updateEntity(TemplateConfiguration entity) {
        entity.setName(this.name);
        entity.setAvailableForRoles(this.availableForRoles);
        entity.setLogUpdatedBy(this.user);
        entity.setType(this.type != null ? TemplateType.fromValue(this.type) : null);
        if (this.parentId != null) {
            TemplateConfiguration parent = new TemplateConfiguration();
            parent.setId(this.parentId);
            entity.setParent(parent);
        } else {
            entity.setParent(null);
        }
        if( this.clusterId != null) {
            entity.setCluster(new ClusterItem(this.clusterId));
        }
        if( this.connectionId != null) {
            entity.setConnection(new Connection(this.connectionId));
        }
    }

}
