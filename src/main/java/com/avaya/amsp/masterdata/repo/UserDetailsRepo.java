/**
 * 
 */
package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.UserDetails;

/**
 * 
 */
@Repository("masterdataUserDetailsRepo")
public interface UserDetailsRepo extends JpaRepository<UserDetails, String> {

}