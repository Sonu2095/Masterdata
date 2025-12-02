package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avaya.amsp.domain.Country;
import com.avaya.amsp.domain.Shipping;
import com.avaya.amsp.domain.ShippingType;
import com.avaya.amsp.domain.Site;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.ShippingAddressDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.CountryRepository;
import com.avaya.amsp.masterdata.repo.ShippingAddressRepository;
import com.avaya.amsp.masterdata.repo.ShippingAddressTypeRepository;
import com.avaya.amsp.masterdata.repo.SiteRepository;
import com.avaya.amsp.masterdata.service.iface.ShippingAddressServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShippingAddressService implements ShippingAddressServiceIface {

	@Autowired
	private ShippingAddressRepository shippingRepo;

	@Autowired
	private ShippingAddressTypeRepository shippingTypeRepo;

	@Autowired
	private CountryRepository countryRepo;

	@Autowired
	private SiteRepository siteRepo;

	@Autowired
	private ModelMapper mapper;

	@Override
	@org.springframework.transaction.annotation.Transactional
	@AuditLog(action = "INSERT",entity = "Shipping",functionality = "Insert new Shipping/Assembling Address")
	public boolean saveAddress(ShippingAddressDto addressDto) {
		// Shipping address = mapper.map(addressDto, Shipping.class);
		Shipping address = new Shipping();
		address.setCompanyName(addressDto.getCompanyName());
		address.setConsignee(addressDto.getConsignee());
		address.setBuilding(addressDto.getBuilding());
		address.setCity(addressDto.getCity());
		address.setContactPartner(addressDto.getContactPartner());

		Country country = countryRepo.findById(addressDto.getCountry().getId()).get();
		address.setCountry(country);

		address.setDescription(addressDto.getDescription());
		address.setLogCreatedBy(addressDto.getLogCreatedBy());
		address.setLogUpdatedBy(addressDto.getLogUpdatedBy());
		address.setPhone(addressDto.getPhone());
		address.setPostcode(addressDto.getPostcode());
		address.setRoom(addressDto.getRoom());

		ShippingType shippingType = shippingTypeRepo.findById(addressDto.getShippingType().getId()).get();
		address.setShippingType(shippingType);

		Site site = siteRepo.findById(addressDto.getIdSite()).get();
		address.setSite(site);

		address.setStreet(addressDto.getStreet());

		address.setActive((long) 1);
		address = shippingRepo.save(address);
		return true;
	}

	@AuditLog(action = "delete",entity = "Shipping",functionality = "delete existing Shipping/Assembling Address")
	@Override
	public boolean deleteAddress(long id) {
		log.info("deleting shipping address record with ID {}", id);
		Optional<Shipping> record = shippingRepo.findById(id);
		record.ifPresentOrElse(value -> {
			value.setActive((long) 0);
			shippingRepo.save(value);
		}, () -> {
			log.info("shipping address not found");
			throw new ResourceNotFoundException(String.format("Shipping address with Id %s not found ", id));
		});
		return true;
	}

	@Override
	public List<ShippingAddressDto> getAddresses() {
		Iterable<Shipping> addresses = shippingRepo.findAll();
		ArrayList<ShippingAddressDto> addressesDto = new ArrayList<>();
		for (Shipping address : addresses) {
			log.debug("Fetched " + address + " from DB");
			if (address.getActive() != 0) {
				addressesDto.add(mapper.map(address, ShippingAddressDto.class));
			}
		}
		log.info("Got list : {}", addressesDto);
		return addressesDto;
	}

	@Override
	public List<ShippingAddressDto> searchAddressesBy(ShippingAddressDto addressDto) {
		Example<Shipping> example = Example.of(mapper.map(addressDto, Shipping.class));
		log.debug("Example : {}", example);
		List<Shipping> addresses = shippingRepo.findAll(example);
		log.info("Got addresses count: {}", addresses.size());
		ArrayList<ShippingAddressDto> addressesDto = new ArrayList<>();
		for (Shipping address : addresses) {
			if (address.getActive() != 0) {
				log.debug("Fetched " + address + " from DB");
				addressesDto.add(mapper.map(address, ShippingAddressDto.class));
			}
		}

		return addressesDto;

	}

	@Override
	public ShippingAddressDto getAddress(Long id) {
		Optional<Shipping> address = shippingRepo.findById(id);
		ShippingAddressDto addressDto = null;
		if (address.isPresent() && address.get().getActive() != 0) {
			addressDto = mapper.map(address.get(), ShippingAddressDto.class);
		}
		log.debug("Got addressDto: {}", addressDto);
		return addressDto;
	}

	@Override
	@Transactional
	@AuditLog(action = "Update",entity = "Shipping",functionality = "Update Shipping/Assembling Address")
	public boolean updateAddress(ShippingAddressDto dto) {
		log.info("ShippingAddressServices.updateAddress() Started");
		if (dto.getId() == null) {
			log.warn("No ID provided in payload");
			return false;
		}

		Optional<Shipping> optionalShipping = shippingRepo.findById(dto.getId());
		if (!optionalShipping.isPresent()) {
			log.warn("Shipping address with id {} not found", dto.getId());
			return false;
		}

		Shipping address = optionalShipping.get();
		address.setCompanyName(dto.getCompanyName());
		address.setConsignee(dto.getConsignee());
		address.setStreet(dto.getStreet());
		address.setCity(dto.getCity());
		address.setPostcode(dto.getPostcode());
		address.setBuilding(dto.getBuilding());
		address.setRoom(dto.getRoom());
		address.setContactPartner(dto.getContactPartner());
		address.setPhone(dto.getPhone());
		address.setDescription(dto.getDescription());
		address.setActive((long) dto.getActive());

		address.setLogCreatedBy(dto.getLogCreatedBy());
		address.setLogUpdatedBy(dto.getLogUpdatedBy());

		if (dto.getCountry() != null && Long.valueOf(dto.getCountry().getId()) != null) {
			countryRepo.findById(dto.getCountry().getId()).ifPresent(address::setCountry);
		}

		if (dto.getShippingType() != null && Long.valueOf(dto.getShippingType().getId()) != null) {
			shippingTypeRepo.findById(dto.getShippingType().getId()).ifPresent(address::setShippingType);
		}

		if (dto.getIdSite() != null) {
			siteRepo.findById(dto.getIdSite()).ifPresent(address::setSite);
		}

		// Save the updated entity
		shippingRepo.save(address);
		log.info("ShippingAddressServices.updateAddress() Ended");
		return true;
	}

}
