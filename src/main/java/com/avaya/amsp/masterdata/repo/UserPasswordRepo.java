/**
 * 
 */
package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.UserPassword;

/**
 * 
 */
@Repository("masterdataUserPasswordRepo")
public interface UserPasswordRepo extends JpaRepository<UserPassword, String> {

}