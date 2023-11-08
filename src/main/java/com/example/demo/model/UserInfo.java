package com.example.demo.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document("UserInfo")
@Data
@NoArgsConstructor

public class UserInfo {
	
	private String mailID;
	private String password;
	private String fullname;
	private String address;
	private String phonenumber;

	

}
