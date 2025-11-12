/**
 * 
 */
package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.UserDetails;

/**
 * 
 */
public interface UserDetailsRepo extends JpaRepository<UserDetails, String> {

}
