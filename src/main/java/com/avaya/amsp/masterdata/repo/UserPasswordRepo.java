/**
 * 
 */
package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.UserPassword;

/**
 * 
 */
public interface UserPasswordRepo extends JpaRepository<UserPassword, String> {

}
