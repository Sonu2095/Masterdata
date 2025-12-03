/**
 * 
 */
package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.UserExtraInfo;

/**
 * 
 */
@Repository("masterdataUserExtraInfoRepo")
public interface UserExtraInfoRepo extends JpaRepository<UserExtraInfo, String> {

}