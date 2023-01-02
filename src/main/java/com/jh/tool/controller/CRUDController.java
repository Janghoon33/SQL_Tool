package com.jh.tool.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jh.tool.model.sw_database;
import com.jh.tool.repo.CRUDRepository;

@RestController
@EnableAutoConfiguration
@RequestMapping(value = "/api")
public class CRUDController {
	
	@Autowired
	CRUDRepository dataRepo;
	
	
	// 조회(select)
	@GetMapping("/database")
	public List<sw_database> getAllCustomer(){
		return dataRepo.findAll();
	}
	
	@GetMapping("/database/{database_id}")
	public ResponseEntity<sw_database> getCustomerById(@PathVariable(value = "database_id") Long database_id) {
		sw_database data = dataRepo.findById(database_id).orElseThrow();
		return ResponseEntity.ok().body(data);
	}
	// ResponseEntity : body와 헤더 정보, 상태 코드, Map, List 등을 다양한 객체를 담을 수 있음
	// 404나 500 에러 같은 Http상태 코드와 데이터를 함께 전송 가능
	
	
	// 생성(create)
	@PostMapping("/database")
	public boolean createCustomer(@Validated @RequestBody sw_database user) {
		dataRepo.save(user);
		return true;
	}
	
	// 수정(update)
	@PutMapping("/database/{database_id}")
	public ResponseEntity<sw_database> updateCustomer(@PathVariable(value = "database_id") Long database_id, 
			@Validated @RequestBody sw_database userDetails) {
		sw_database user2 = dataRepo.findById(database_id)
				.orElseThrow();
		
		//customer2.setDatabase_id(userDetails.getDatabase_id());
		user2.setType(userDetails.getType());
		user2.setIp(userDetails.getIp());
		user2.setPort(userDetails.getPort());
		user2.setDatabase(userDetails.getDatabase());
		user2.setUsername(userDetails.getUsername());
		user2.setPassword(userDetails.getPassword());
		final sw_database updateUser = dataRepo.save(user2);
		return ResponseEntity.ok(updateUser);
	}
	
	// 삭제(delete)
	@DeleteMapping("/database/{database_id}")
	public Map<String,Boolean> deleteCustomer(@PathVariable(value = "database_id") Long database_id){
		sw_database data = dataRepo.findById(database_id).orElseThrow();
		dataRepo.delete(data);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return response;
	}
	
	
}
