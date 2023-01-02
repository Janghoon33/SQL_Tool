package com.jh.tool.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="sw_database_reject")
public class sw_database_reject {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long policy_id;
	
	@ManyToOne
	@JoinColumn(name="database_id", referencedColumnName = "database_id")
	private sw_database sw_database;
	
	@Column
	private String client_ip;
	
}
