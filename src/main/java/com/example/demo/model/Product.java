package com.example.demo.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document("ProductInfo")
@Data
@NoArgsConstructor
public class Product {

	@Indexed(unique = true)
	private String item_code;
	private String item_name;
	private String item_price;
	private String image1;
	private String image2;
	private String image3;
	private String image4;
	private String category;
	private String item_color;
	private String item_weight;
	private String item_size;
	private String item_material;
	
}
