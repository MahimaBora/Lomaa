package com.example.demo.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.demo.model.Product;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class Contact {
	
	@Autowired
	public JavaMailSender mailSender;

	public void sendEmail(String toEmail, String subject, StringBuilder str, Map<String, String> formData, ArrayList<Product> cartItems, Object name, Object mail, ArrayList<String> attachments) throws MessagingException {
	    Random random = new Random();
	    int order_ID = 100000 + random.nextInt(900000);

	    MimeMessage message1 = mailSender.createMimeMessage();
	    MimeMessageHelper mimeMessageHelper1 = new MimeMessageHelper(message1, true);
	    mimeMessageHelper1.setFrom("workwebsite2000@gmail.com");
	    mimeMessageHelper1.setTo(toEmail);

	    // Creating an HTML table for the email content
	    StringBuilder htmlContent = new StringBuilder();
	    htmlContent.append("<h3>OrderID: ").append(order_ID).append("</h3>");
	    htmlContent.append(str.toString());
	    htmlContent.append("<table border='1'>");
	    htmlContent.append("<tr><th>S.No.</th><th>Item Category</th><th>Item Code</th><th>Item Name</th><th>Item Price</th><th>Item</th><th>QTY</th></tr>");

	    int SerialNumber=1;
	    for (Product item : cartItems) {
	        String qty = formData.get(item.getItem_code());
	       

	        htmlContent.append("<tr>");
	        htmlContent.append("<td>").append(SerialNumber++).append("</td>");
	        htmlContent.append("<td>").append(item.getCategory()).append("</td>");
	        htmlContent.append("<td>").append(item.getItem_code()).append("</td>");
	        htmlContent.append("<td>").append(item.getItem_name()).append("</td>");
	        htmlContent.append("<td>").append(item.getItem_price()).append("</td>");
	        htmlContent.append("<td>").append(item.getImage1()).append("</td>");
	        htmlContent.append("<td>").append(qty).append("</td>");
	        htmlContent.append("</tr>");
	    }

	    htmlContent.append("</table>");

	    // Set the email content with the HTML table
	    mimeMessageHelper1.setText(htmlContent.toString(), true);

	    mimeMessageHelper1.setSubject(subject);

	    for (String file : attachments) {
	        File attachmentFile = new File(file);
	        if (attachmentFile.exists()) {
	            FileSystemResource fileSystem = new FileSystemResource(attachmentFile);
	            mimeMessageHelper1.addAttachment(fileSystem.getFilename(), fileSystem);
	        }
	    }

	    mailSender.send(message1);

	   sendConfirmation(name.toString(), mail.toString(), order_ID, htmlContent.toString(), attachments);
	}



   
  
  
	
    

	public void sendConfirmation (String name,String mail, int order_ID, String htmlContent, ArrayList<String> attachments) throws MessagingException
	{
		 MimeMessage message = mailSender.createMimeMessage();
		  MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
		String subject= "Order Confirmation";
		String body= "Hi "+name+"\n"+"ThankYou for Placing the Order with LOMAA "+"\n" + htmlContent+"\n"+ "Keep Shopping with LOMAA!!";
		

		System.out.println(name);

		System.out.println(mail);
		System.out.print(body);
		  mimeMessageHelper.setFrom("workwebsite2000@gmail.com");
		    mimeMessageHelper.setTo(mail);
		    mimeMessageHelper.setText(body, true);
		    mimeMessageHelper.setSubject(subject);
		    
		    for (String file : attachments) {
		        File attachmentFile = new File(file);
		        if (attachmentFile.exists()) {
		            FileSystemResource fileSystem = new FileSystemResource(attachmentFile);
		            mimeMessageHelper.addAttachment(fileSystem.getFilename(), fileSystem);
		        }
		    }
		
		
		mailSender.send(message);	
		
		
		
	}	

}

