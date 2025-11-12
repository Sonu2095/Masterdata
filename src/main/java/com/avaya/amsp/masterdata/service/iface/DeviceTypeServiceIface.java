package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.DeviceTypeDto;

public interface DeviceTypeServiceIface {

	boolean saveDeviceType(DeviceTypeDto deviceType);

	List<DeviceTypeDto> getDeviceTypes();

	boolean deleteDeviceType(long id);

	List<DeviceTypeDto> searchDeviceTypeBy(DeviceTypeDto deviceType);

	DeviceTypeDto getDeviceType(Long id);

	List<DeviceTypeDto> getDeviceTypesByCluster(Long id);

	boolean updateDeviceType(DeviceTypeDto deviceTypeDto);

}