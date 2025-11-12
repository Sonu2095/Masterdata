package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.ShippingType;

public interface ShippingAddressTypeRepository extends JpaRepository<ShippingType, Long> {

}
