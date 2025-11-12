package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.PbxComponentDto;
import com.avaya.amsp.masterdata.dtos.PbxSpecialPortDto;

public interface PbxSpecialPortServiceIface {

	List<PbxSpecialPortDto> fetchAllPbxSpecialPorts();
	long savePbxSpecialPort(PbxSpecialPortDto pbxSpecialPortDto);
	void updatePbxSpecialPort(PbxSpecialPortDto pbxSpecialPortDto);
	void deletePbxSpecialPort(Long pbxSpecialPortId);

}
