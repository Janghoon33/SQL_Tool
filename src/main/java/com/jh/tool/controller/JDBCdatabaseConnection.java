package com.jh.tool.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.tool.model.JSData;
import com.jh.tool.model.JSData2;
import com.jh.tool.model.sw_connection_log;
import com.jh.tool.model.sw_database;
import com.jh.tool.model.sw_database_reject;
import com.jh.tool.model.sw_execute_log;
import com.jh.tool.repo.CRUDRepository;
import com.jh.tool.repo.QueryLOGRepository;
import com.jh.tool.repo.ConLOGRepository;
import com.jh.tool.repo.IpRejectRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RestController
@EnableAutoConfiguration
@RequestMapping(value = "/api")
@Repository
public class JDBCdatabaseConnection {
	
	
	String query;
	HttpSession session;
	Connection conn = null;
	ResultSet rs = null;
	PreparedStatement psmt = null;
	int i = 0;
	
	HttpServletRequest request;

	InetAddress local;

	sw_database db;
	sw_connection_log conlog;
	sw_execute_log ex_log;

	
	@Autowired
	CRUDRepository dataRepo;
	
	@Autowired
	ConLOGRepository conlogRepo;
	
	@Autowired
	QueryLOGRepository queryRepo;
	
	@Autowired
	IpRejectRepository ipRepo;
	
	// Json
	private ObjectMapper objectMapper = new ObjectMapper();
	// Token key
	private static final String secretKey = Base64.getEncoder().encodeToString("????????????".getBytes());
	
	// create token
	public String createToken() {
		
		
		i++;
		Map<String, Object> headers = new HashMap<>();
		
		String jwt = Jwts.builder()
				.setHeader(headers)
				.claim("token_id",i)
				.signWith(SignatureAlgorithm.HS256, secretKey)
				.compact();
		
		// ?????? ??? ????????? ?????????
		System.out.println("token_id : " +i);
		
		return jwt;
	}
	
	public Claims getInfo(String jwt) {
		Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt).getBody();
		return claims;
	}
	
	// ????????? IP ?????? ?????????
	public static String getClientIp(HttpServletRequest req) {
		String ip = req.getHeader("X-Forwarded-For");
		if(ip == null ) ip = req.getRemoteAddr();
		return ip;
	}
	
	
	// database Connect 
	@PostMapping("/connect/{database_id}")
	public Boolean dbCon(HttpServletRequest req,@PathVariable(value="database_id") Long database_id, sw_connection_log conlog2) {
		
		//sw_database_reject ipReject = ipRepo.findById(database_id).orElseThrow();
		
		sw_database db = dataRepo.findById(database_id).orElseThrow();
		String url=null;
		String user = db.getUsername();
		String password = db.getPassword();
		String jwt = null;
		String ip = null;
		try {
			if(db.getType()==1) {
				url = "jdbc:postgresql://"+db.getIp()+"/"+db.getDatabase();
			}
			else {
				url = "jdbc:mariadb://"+db.getIp()+"/"+db.getDatabase();
			}
			
			conn = DriverManager.getConnection(url, user, password);
			ip = getClientIp(req);
//			if(ip.equals(ipReject.getClient_ip())) {
//				System.out.println("?????? IP : "+ipReject.getClient_ip());
//				conn = null;
//				System.out.println("????????? IP?????????");
//			}
			
			if(db.getType() == 1) {
				System.out.println("Connected to the PostgreSQL server successfully.");
			} else {
				System.out.println("Connected to the MariaDB server successfully.");
			}
			
			if(conn != null) {
				
				conlog2.setConnect_date(LocalDateTime.now());
				conlog2.setClient_ip(ip);
				conlog2.setResult(true);
				conlogRepo.save(conlog2);
				jwt = createToken();
				session = req.getSession();
				Claims claims = getInfo(jwt);
				String tk = claims.toString();
				session.setAttribute("jwt", tk);
				System.out.println("connect ?????? ?????? ????????? : "+tk);// -> ????????? ??? ??? ??????????????? ????????? ????????? 

		} 
			else {
			System.out.println("Failed to make connection!");
		}
	} catch (Exception e) {
		System.out.println(e.getMessage());
	}
		return true;
	}
	
	// connection close
	@PostMapping("/discon")
	public Boolean dbClose(HttpServletRequest req, @RequestBody JSData2 jsdata2) {
		String jwt = null;
		String token_id = jsdata2.getToken_id();
		session = req.getSession();
		jwt = (String)session.getAttribute("jwt");
		System.out.println(jwt);
		
		System.out.println("discon : "+jwt);

		if(jwt != token_id) {

			try {
					if(rs != null) rs.close();
					if(psmt != null) psmt.close();
					if(conn != null) conn.close();
					
					System.out.println("DB ?????? ??????");
				}
			 catch (Exception e) {
				e.printStackTrace();
			 }
		}
		
		return true;
	}
	
	
	// select
	@PostMapping("select")
	public ResponseEntity<sw_execute_log> dbSelect(@RequestBody JSData jsdata, sw_execute_log ex_log, HttpServletRequest req) {

		
//		System.out.println("database_id : "+jsdata.getDatabase_id()); database_id ????????? ?????????
//		System.out.println("query : "+jsdata.getQuery()); ????????? ????????? ?????????
		Long database_id = jsdata.getDatabase_id();
		String query = jsdata.getQuery();
		
		sw_database db = dataRepo.findById(database_id).orElseThrow();
		String ip = null;
		try {
			ip = getClientIp(req);
			
			psmt = conn.prepareStatement(query);
			ResultSet rs = psmt.executeQuery();
			// executeQuery() : select?????? ????????? ?????? Resultset??? ???????????? ?????? ??? ??????
			
			// System.out.println(query); ?????? ????????? ?????????
			
			ex_log.setClient_ip(ip);
			ex_log.setExec_date(LocalDateTime.now());
			ex_log.setSql_text(query);
			ex_log.setSql_type("select");
			ex_log.setResult(true);
			ex_log.setMessage("????????? ?????????????????????");
			queryRepo.save(ex_log);
			while(rs.next()) {
				System.out.println("query?????? : "+rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ip = getClientIp(req);
			
			ex_log.setClient_ip(ip);
			ex_log.setExec_date(LocalDateTime.now());
			ex_log.setSql_text(query);
			ex_log.setSql_type("select");
			ex_log.setResult(false);
			ex_log.setMessage("?????? ????????? ?????????????????????");
			queryRepo.save(ex_log);
		}
		System.out.println("-select-");
		sw_execute_log selectQuerydata = queryRepo.save(ex_log);
		return ResponseEntity.ok(selectQuerydata);
	}
	
	// insert,delete,update
		@PostMapping("/cud")
		public ResponseEntity<sw_execute_log> dbCUD(@RequestBody JSData jsdata, sw_execute_log ex_log2, HttpServletRequest req) {
			
			System.out.println("database_id : "+jsdata.getDatabase_id());
			System.out.println("query : "+jsdata.getQuery());
			Long database_id = jsdata.getDatabase_id();
			String query = jsdata.getQuery();
			
			sw_database db = dataRepo.findById(database_id).orElseThrow();
			String ip = null;
			try {
				psmt = conn.prepareStatement(query);
				ip = getClientIp(req);
				if(conn != null) {
					
					
					int rs = psmt.executeUpdate();
					// select??? ????????? ?????? ?????? ?????? ??? ???????????? ?????? 
					// insert/delete/update??? ????????? ???????????? ?????? ??????, create/drop??? -1??????
					System.out.println(rs);
					if(rs == -1) {
						System.out.println("Create or Drop ????????? ?????????????????????");
						ex_log2.setClient_ip(ip);
						ex_log2.setExec_date(LocalDateTime.now());
						ex_log2.setSql_text(query);
						String querytype = query.split(" ")[0];
						ex_log2.setSql_type(querytype);
						ex_log2.setResult(true);
						ex_log2.setMessage("????????? ?????????????????????");
						queryRepo.save(ex_log2);
						
					}
					else {
						System.out.println("Insert or Delete or Update ????????? ?????????????????????");
						ex_log2.setClient_ip(ip);
						ex_log2.setExec_date(LocalDateTime.now());
						ex_log2.setSql_text(query);
						String querytype = query.split(" ")[0];
						ex_log2.setSql_type(querytype);
						ex_log2.setResult(true);
						ex_log2.setMessage("????????? ?????????????????????");
						queryRepo.save(ex_log2);
					}
				} 
				else {
					
					System.out.println("Failed to make connection!");
					ex_log2.setClient_ip(ip);
					ex_log2.setExec_date(LocalDateTime.now());
					ex_log2.setSql_text(query);
					ex_log2.setSql_type("create or drop or insert or delete or update");
					ex_log2.setResult(false);
					ex_log2.setMessage("?????? ????????? ?????????????????????");
					queryRepo.save(ex_log2);
				}
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
			sw_execute_log cudQuerydata = queryRepo.save(ex_log2);
			return ResponseEntity.ok(cudQuerydata);
		}
	
		
		
		
		
}
