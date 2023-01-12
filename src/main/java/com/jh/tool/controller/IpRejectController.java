package com.jh.tool.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jh.tool.model.sw_database_reject;
import com.jh.tool.repo.IpRejectRepository;

@RestController
@RequestMapping(value="/reject")
public class IpRejectController {
	
	@Autowired
	IpRejectRepository ipRepo;
	
	// select
	@GetMapping("/all")
	public List<sw_database_reject> getAllinfo(){
		return ipRepo.findAll();
	}
	
	//create
	@PostMapping("/regiIp")
	public boolean createReject(@RequestBody sw_database_reject ipReject) {
		ipRepo.save(ipReject);
		return true;
	}
	
	// update
	@PutMapping("/{policy_id}")
	public ResponseEntity<sw_database_reject> updateReject(@PathVariable(value = "policy_id") Long policy_id,
			@Validated @RequestBody sw_database_reject rejectDetail){
		
		sw_database_reject preReject = ipRepo.findById(policy_id).orElseThrow();
		
		preReject.setClient_ip(rejectDetail.getClient_ip());
		final sw_database_reject updateReject = ipRepo.save(preReject);
		return ResponseEntity.ok(updateReject);
	}
	
	// delete
	@DeleteMapping("/delIp/{policy_id}")
	public Map<String,Boolean> deleteReject(@PathVariable(value = "policy_id")Long policy_id){
		sw_database_reject data = ipRepo.findById(policy_id).orElseThrow();
		ipRepo.delete(data);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return response;
	}
	
	
	
	
	
	
	
}
