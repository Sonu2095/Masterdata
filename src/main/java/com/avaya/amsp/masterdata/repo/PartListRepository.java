package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import com.avaya.amsp.domain.Article;
import com.avaya.amsp.domain.Country;
import com.avaya.amsp.domain.PartList;

public interface PartListRepository extends JpaRepository<PartList, Long> {

}
