package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.PbxPhoneNumberType;

public interface PhoneNumberTypeRepository extends JpaRepository<PbxPhoneNumberType, Long> {

}
