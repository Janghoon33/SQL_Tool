package com.jh.tool.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jh.tool.model.sw_database;

public interface CRUDRepository extends JpaRepository<sw_database, Long> {
//JpaRepository를 사용하면 @Repository 어노테이션 안적어도 됨
	
}
