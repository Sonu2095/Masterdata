package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avaya.amsp.domain.Language;

public interface LanguageRepository extends JpaRepository<Language, String> {

}
