package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ActiveSubscribers;
import com.avaya.amsp.domain.OrderItem;
import com.avaya.amsp.domain.PbxCluster;
import com.avaya.amsp.domain.PbxNumberLock;
import com.avaya.amsp.domain.PbxNumberRange;
import com.avaya.amsp.domain.PbxPhoneNumberType;
import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.dtos.PbxNumberRangeDto;
import com.avaya.amsp.masterdata.dtos.PhoneNumberRangeDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ActiveSubscribersRepository;
import com.avaya.amsp.masterdata.repo.OrderItemRepository;
import com.avaya.amsp.masterdata.repo.PbxClusterRepository;
import com.avaya.amsp.masterdata.repo.PbxNumberLockRepository;
import com.avaya.amsp.masterdata.repo.PbxNumberRangeRepository;
import com.avaya.amsp.masterdata.service.iface.PbxNumberRangeIface;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PbxNumberRangeService implements PbxNumberRangeIface {

	@Autowired
	private PbxNumberRangeRepository pbxNumberRangeRepo;
	
	@Autowired
	private PbxNumberLockRepository pbxNumberLockRepository;
	
	@Autowired
	private ActiveSubscribersRepository activeSubscribersRepository;
	
	@Autowired
	private PbxClusterRepository pbxClusterRepository;
	
	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	ModelMapper mapper;

	@Override
	public List<PhoneNumberRangeDto> fetchPbxNumberRangeByPbxSystem(Long id) {
		
		log.info("fetching pbx number range by given pbx system id {} ", id);
		List<PhoneNumberRangeDto> pbxDtoList = new ArrayList<PhoneNumberRangeDto>();
		List<PbxNumberRange> pbxNumberRangeList = pbxNumberRangeRepo.findByIdPbxSystemAndActive(id.intValue(),1);
		 //AtomicInteger counter;
		if (pbxNumberRangeList != null && !pbxNumberRangeList.isEmpty()) {
			
			pbxNumberRangeList.forEach(pbxNumberRange -> {
				AtomicInteger counter= new AtomicInteger(1);
				PhoneNumberRangeDto phNoRangeDto = new PhoneNumberRangeDto();				 
				phNoRangeDto.setId(pbxNumberRange.getId());
				long idPbxSystem = pbxNumberRange.getIdPbxSystem();
				phNoRangeDto.setPbxId(idPbxSystem);
				phNoRangeDto.setPhoneNumType(pbxNumberRange.getPhoneNumberType().getName());
				phNoRangeDto.setRangeFrom(pbxNumberRange.getRangeFrom());
				phNoRangeDto.setRangeTo(pbxNumberRange.getRangeTo());
				phNoRangeDto.setRemark(pbxNumberRange.getRemark());
				long idPbxCluster = pbxNumberRange.getIdPbxCluster();
				phNoRangeDto.setPbxClusterId(idPbxCluster);
				PbxCluster pbxCluster = pbxClusterRepository.findById(idPbxCluster).orElse(null);
				if(Objects.nonNull(pbxCluster)) {
					phNoRangeDto.setPbxClusterName(pbxCluster.getName());
					phNoRangeDto.setAreaCode(pbxCluster.getAreacode());
				}
				
				//AvailablePhoneNumbers
				List<Integer> availableNumbers = IntStream.rangeClosed(pbxNumberRange.getRangeFrom(),
						pbxNumberRange.getRangeTo()).boxed().collect(Collectors.toList());
															
				//find used or subscribed phoneNumber ->which to be deleted from availableNumbers
				
				List<ActiveSubscribers> activeSubscribersList = activeSubscribersRepository.findByPbxClusterIdAndPbxSystemId(idPbxCluster, idPbxSystem);
				if(CollectionUtils.isNotEmpty(activeSubscribersList)) {
					activeSubscribersList.forEach(e-> {
						Integer usedNumber = safeStringToInt(e.getExtension());
						availableNumbers.removeIf(num->num.equals(usedNumber));
					});
				}
				
				// locked number
				// find phoneNumberLock detail by given idPbxCluster and remove lockedNo from availableNumbers					
				List<PbxNumberLock> pbxNumberLockList = pbxNumberLockRepository.findBypbxCluster_Id(idPbxCluster);
				if (CollectionUtils.isNotEmpty(pbxNumberLockList)) {
					pbxNumberLockList.forEach(phNumberLock -> {
						boolean flag= isInRange(Integer.valueOf(phNumberLock.getPhoneNumber()), pbxNumberRange.getRangeFrom(), pbxNumberRange.getRangeTo());
						if(flag)  
							phNoRangeDto.setLocked(counter.getAndIncrement());
							availableNumbers.removeIf(num->num.equals(Integer.valueOf(phNumberLock.getPhoneNumber())));
					});
				}
				
				//Find Reserve phone number and delete it from available phNumber list
				List<OrderItem> orderItemList = orderItemRepository.findByPbxSystemId(idPbxSystem);
				if (CollectionUtils.isNotEmpty(orderItemList)) {
					orderItemList.forEach(orderItem->{
						Integer reservedNumber = safeStringToInt(orderItem.getPhoneNumber());
						availableNumbers.removeIf(num->num.equals(reservedNumber));
					});
				}
								
				phNoRangeDto.setAvailablePhoneNumbers(availableNumbers);
				
				pbxDtoList.add(phNoRangeDto);
		
			});			
		}else {
			log.info("no active records found");
		}
		return pbxDtoList;
	}
	
	public static boolean isInRange(int phoneNumber,int fromRange, int toRange) {
		boolean flag = false;
        flag = (phoneNumber >= fromRange && phoneNumber <= toRange);
        return flag;
    }
	
	 public static Integer safeStringToInt(String str) {
	        if (str != null && str.matches("\\d+")) { // only digits
	            return Integer.parseInt(str);
	        }
	        return null; 
	    }
	
	
	@Override
	public List<PbxNumberRangeDto> fetchAllPbxNumberRange() {

		log.info("fetching available pbx number range");

		List<PbxNumberRangeDto> pbxList = new ArrayList<PbxNumberRangeDto>();
		List<PbxNumberRange> pbxNumberList = pbxNumberRangeRepo.findAll();

		if (pbxNumberList != null && !pbxNumberList.isEmpty()) {

			pbxNumberList.forEach(pbxRange -> {

				if (pbxRange.getActive() != 0) {

					PbxNumberRangeDto pbxDto = new PbxNumberRangeDto();

					pbxDto.setId(pbxRange.getId());
					pbxDto.setPbxId(pbxRange.getIdPbxSystem());
					pbxDto.setPhoneNumType(pbxRange.getPhoneNumberType().getId());
					pbxDto.setRangeFrom(pbxRange.getRangeFrom());
					pbxDto.setRangeTo(pbxRange.getRangeTo());
					pbxDto.setRemark(pbxRange.getRemark());
					pbxDto.setPbxClusterId(pbxRange.getIdPbxCluster());
					pbxDto.setLogCreatedBy(pbxRange.getLogCreatedBy());
					pbxDto.setLogCreatedOn(pbxRange.getLogCreatedOn());
					pbxDto.setLogUpdatedBy(pbxRange.getLogUpdatedBy());
					pbxDto.setLogUpdatedOn(pbxRange.getLogUpdatedOn());

					pbxList.add(pbxDto);
				}

			});
		} else {
			log.info("no active records found");
		}
		return pbxList;
	}

	@PBXTechAuditLog(action = "insert",entity = "PbxNumberRange",functionality = "PBX Add New Phone Number Range")
	@Override
	public String createPbxNumberRange(PbxNumberRangeDto pbxNumberRangeDto) {
		Long inputId = pbxNumberRangeDto.getId();
		Long existingPbxNumber = pbxNumberRangeRepo.fetchPbxNumberRange(inputId);
		if (existingPbxNumber != null && (existingPbxNumber.intValue() == inputId.intValue())) {
			return String.format("PBX number range ID %d already exists.", inputId);
		}
		log.info("adding new pbx range to database");
		PbxNumberRange pbxRecord = mapper.map(pbxNumberRangeDto, PbxNumberRange.class);
		if (pbxNumberRangeDto.getPbxClusterId() == 0) { 
			return "PbxClusterId zero not applicable "+ pbxNumberRangeDto.getPbxClusterId();
		}
		 pbxRecord.setIdPbxCluster(pbxNumberRangeDto.getPbxClusterId());
		if (pbxNumberRangeDto.getPbxId() != null) { 
			pbxRecord.setIdPbxSystem(pbxNumberRangeDto.getPbxId());
		}

		pbxRecord.setLogCreatedBy(pbxNumberRangeDto.getLogCreatedBy());
		pbxRecord.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));

		PbxPhoneNumberType phonetype = new PbxPhoneNumberType();
		phonetype.setId(pbxNumberRangeDto.getPhoneNumType());
		pbxRecord.setPhoneNumberType(phonetype);

		pbxRecord.setRangeFrom(pbxNumberRangeDto.getRangeFrom());
		pbxRecord.setRangeTo(pbxNumberRangeDto.getRangeTo());
		pbxRecord.setRemark(pbxNumberRangeDto.getRemark());
		pbxRecord.setActive(1);

		PbxNumberRange pbxRecordData = pbxNumberRangeRepo.save(pbxRecord);
		log.info("pbx number range added  {}", pbxRecordData.getId());
		return "pbx number range created " + pbxRecordData.getId();
	}

	@PBXTechAuditLog(action = "update",entity = "PbxNumberRange",functionality = "PBX update existing Phone Number Range")
	@Override
	public void updatePbxNumberRange(PbxNumberRangeDto pbxNumberRangeDto) {

		log.info("updating pbx number range record with ID {}", pbxNumberRangeDto.getId());

		Optional<PbxNumberRange> pbxRecord = pbxNumberRangeRepo.findById(pbxNumberRangeDto.getId());
		pbxRecord.ifPresentOrElse(value -> {

			if (value.getActive() != 0) {
				value.setIdPbxCluster(pbxNumberRangeDto.getPbxClusterId());
				value.setIdPbxSystem(pbxNumberRangeDto.getPbxId());
				value.setLogUpdatedBy(pbxNumberRangeDto.getLogUpdatedBy());
				value.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
				value.setRemark(pbxNumberRangeDto.getRemark());
				value.setRangeFrom(pbxNumberRangeDto.getRangeFrom());
				value.setRangeTo(pbxNumberRangeDto.getRangeTo());
				PbxPhoneNumberType phonetype = new PbxPhoneNumberType();
				phonetype.setId(pbxNumberRangeDto.getPhoneNumType());
				value.setPhoneNumberType(phonetype);
				PbxNumberRange pbxUpdateRecord = pbxNumberRangeRepo.save(value);

				log.info("updated pbx record having id {}", pbxUpdateRecord.getId());

			} else {
				log.info("record is present, but not in active state, so specific record can't be updated");
			}
		}, () -> {
			log.info("pbx record not found");
			throw new ResourceNotFoundException(
					String.format("pbx record with Id %s not found ", pbxNumberRangeDto.getId()));
		});
	}
    
	/*@PBXTechAuditLog(
		    action = "update",
		    entity = "PbxNumberRange",
		    functionality = "PBX update existing Phone Number Range"
		)
		@Override
		@Transactional  // Important: ensure this method is transactional
		public void updatePbxNumberRange(PbxNumberRangeDto pbxNumberRangeDto) {
		    log.info("Updating PBX number range record with ID {}", pbxNumberRangeDto.getId());

		    Optional<PbxNumberRange> pbxRecordOpt = pbxNumberRangeRepo.findById(pbxNumberRangeDto.getId());

		    pbxRecordOpt.ifPresentOrElse(record -> {
		        if (record.getActive() != 0) {

		            // Update entity fields directly
		            record.setIdPbxCluster(pbxNumberRangeDto.getPbxClusterId());
		            record.setIdPbxSystem(pbxNumberRangeDto.getPbxId());
		            record.setLogUpdatedBy(pbxNumberRangeDto.getLogUpdatedBy());
		            record.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		            record.setRemark(pbxNumberRangeDto.getRemark());
		            record.setRangeFrom(pbxNumberRangeDto.getRangeFrom());
		            record.setRangeTo(pbxNumberRangeDto.getRangeTo());

		            // Update relationship
		            PbxPhoneNumberType phoneType = new PbxPhoneNumberType();
		            phoneType.setId(pbxNumberRangeDto.getPhoneNumType());
		            record.setPhoneNumberType(phoneType);

		            // No need to call save() here — Hibernate will flush automatically
		            log.info("PBX record with ID {} updated successfully", record.getId());

		        } else {
		            log.info("Record is present but inactive; cannot update this record.");
		        }
		    }, () -> {
		        log.warn("PBX record not found for ID {}", pbxNumberRangeDto.getId());
		        throw new ResourceNotFoundException(
		            String.format("PBX record with ID %s not found", pbxNumberRangeDto.getId())
		        );
		    });
		}*/
	@PBXTechAuditLog(action = "delete",entity = "PbxNumberRange",functionality = "PBX delete Phone Number Range")
	@Override
	public void deletePbxNumberRange(Long pbxId) {
		log.info("deleting pbx number range record with ID {}", pbxId);
		Optional<PbxNumberRange> record = pbxNumberRangeRepo.findById(pbxId);
		record.ifPresentOrElse(value -> {
			value.setActive(0);
			pbxNumberRangeRepo.save(value);
		}, () -> {
			log.info("pbx record not found");
			throw new ResourceNotFoundException(String.format("pbxnumberRange with Id %s not found ", pbxId));
		});

	}

}
