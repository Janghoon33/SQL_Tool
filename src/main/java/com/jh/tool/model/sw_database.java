package com.jh.tool.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Setter
@Entity
@Table(name = "sw_database")
public class sw_database {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="database_id")
	private Long database_id;
	
	@Column
	private int type;
	
	@Column
	private String ip;
	
	@Column
	private int port;
	
	@Column
	private String database;
	
	@Column
	private String username;
	
	@Column
	private String password;
	
	// 객체 생성
	@Builder
	public sw_database(int type, String ip, int port, String database, String username, String password) {
		this.type=type;
		this.ip=ip;
		this.port=port;
		this.database=database;
		this.username=username;
		this.password=password;
	}

}
