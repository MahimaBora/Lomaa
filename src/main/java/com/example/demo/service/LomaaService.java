package com.example.demo.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Login;
import com.example.demo.model.Product;
import com.example.demo.model.UserInfo;
import com.example.demo.repository.LoginRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.annotation.PostConstruct;

@Service
public class LomaaService {

  public static String otp;
  public static String status;
  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private LoginRepository loginRepository;

  @Autowired
  Login login;

  @Autowired
  public JavaMailSender mailSender;

  public void verify(String mailID, String password) throws Exception {
    try {

      List < UserInfo > userInfo = loginRepository.findByMailID(mailID);
      System.out.print(userInfo);

      if (userInfo.isEmpty()) {

        LomaaService.status = "Success";
      } else {
        LomaaService.status = "Failure";

      }

    } catch (Exception e) {
      System.out.print("Already Present");
      LomaaService.status = "Failure";
      throw new Exception("Error " + e.getMessage(), e);

    }
  }

  public void verifyuser(String mailID, String name) throws Exception {
    try {
      System.out.print(mailID);
      System.out.print(name);

      Query query = new Query();

      UserInfo userInfo = mongoTemplate.findOne(query.addCriteria(Criteria.where("mailID").is(mailID)), UserInfo.class);

      System.out.print(userInfo);

      if (userInfo != null && name.equals(userInfo.getFullname())) {
        System.out.print(" Present");

        LomaaService.status = "Success";
      } else {
        System.out.print(" Presexxnt");

        LomaaService.status = "Failure";
      }

    } catch (Exception e) {
      System.out.print("Already Present");
      LomaaService.status = "Failure";
      throw new Exception("Error " + e.getMessage(), e);

    }
  }

  public void sendEmail(String name, String mail) throws Exception {
    System.out.print("eeey");

    try {
      String subject = "OTP FOR RESET PASSWORD";

      Random random = new Random();
      int otpp = 100000 + random.nextInt(900000);
      LomaaService.otp = Integer.toString(otpp);
      String body = name + " HERE'S SIX DIGIT OTP " + otpp;
      System.out.println(name);

      System.out.println(mail);
      System.out.print(body);
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom("workwebsite2000@gmail.com");
      message.setTo(mail);
      message.setText(body);
      message.setSubject(subject);

      mailSender.send(message);
      LomaaService.status = "Success";

    } catch (Exception e) {
      System.out.print("yee");

      LomaaService.status = "Failure";
      throw new Exception("Error " + e.getMessage(), e);

    }

  }

  public void userSignup(String username, String password, String fullname, String address, String phonenumber) throws Exception {
    try {

      List < UserInfo > userInfo = loginRepository.findByMailID(username);
      System.out.print(userInfo);

      if (userInfo.isEmpty()) {
        UserInfo user = new UserInfo();
        user.setMailID(username);
        user.setPassword(password);
        user.setFullname(fullname);
        user.setAddress(address);
        user.setPhonenumber(phonenumber);

        mongoTemplate.insert(user);
        LomaaService.status = "Success";
      } else {
        LomaaService.status = "Failure";

      }

    } catch (Exception e) {
      System.out.print("Already Present");
      LomaaService.status = "Failure";
      throw new Exception("Error " + e.getMessage(), e);

    }
  }


  public String userLogin(String mailID, String password) throws Exception {
    try {
      Query query = new Query();

      UserInfo userInfo = mongoTemplate.findOne(query.addCriteria(Criteria.where("mailID").is(mailID)), UserInfo.class);
      System.out.print("name is " + userInfo.getFullname());

      if (userInfo != null && password.equals(userInfo.getPassword())) {
        LomaaService.status = "Success";
        return userInfo.getFullname();
      } else {
        LomaaService.status = "Failure";
      }

    } catch (Exception e) {
      System.out.print("Not Present");
      LomaaService.status = "Failure";
//      throw new Exception("Error " + e.getMessage(), e);
    }
    return null;

  }

  //	

  public void updateuserinfo(String mailID, String fullname, String address, String phonenumber, String password) throws Exception {
    try {

      Query query = new Query();
      Update update = new Update();

      UserInfo userInfo = mongoTemplate.findOne(query.addCriteria(Criteria.where("mailID").is(mailID)), UserInfo.class);

      if (userInfo != null) {
        update.set("password", password);
        update.set("fullname", fullname);
        update.set("phonenumber", phonenumber);
        update.set("address", address);

        mongoTemplate.upsert(query, update, UserInfo.class);

        LomaaService.status = "Success";
      } else {
        LomaaService.status = "Failure";
      }

    } catch (Exception e) {
      LomaaService.status = "Failure";

      throw new Exception("Error " + e.getMessage(), e);
    }

  }

  public void userUpdatePwd(String mailID, String password) throws Exception {
    try {

      Query query = new Query();
      Update update = new Update();

      UserInfo userInfo = mongoTemplate.findOne(query.addCriteria(Criteria.where("mailID").is(mailID)), UserInfo.class);

      if (userInfo != null) {

        update.set("password", password);

        mongoTemplate.upsert(query, update, UserInfo.class);

        LomaaService.status = "Success";
      } else {
        LomaaService.status = "Failure";
      }

    } catch (Exception e) {
      LomaaService.status = "Failure";

      throw new Exception("Error " + e.getMessage(), e);
    }

  }

  public void product(String item_code, String item_name, String item_price, MultipartFile image1, MultipartFile image2, MultipartFile image3, MultipartFile image4, String selectedCheckboxValue, String item_color, String item_weight, String item_size, String item_material) throws Exception {
    try {
      Product product = new Product();
      product.setItem_code(item_code);
      product.setItem_name(item_name);
      product.setItem_price(item_price);
      product.setImage1(image1.getOriginalFilename());
      product.setImage2(image2.getOriginalFilename());
      product.setImage3(image3.getOriginalFilename());
      product.setImage4(image4.getOriginalFilename());
      product.setCategory(selectedCheckboxValue);
      product.setItem_color(item_color);
      product.setItem_weight(item_weight);
      product.setItem_size(item_size);
      product.setItem_material(item_material);
      
      
      mongoTemplate.insert(product);
      LomaaService.status = "Success";
      System.out.print("6");

    } catch (Exception e) {
      System.out.print("5");

      LomaaService.status = "Failure";
      throw new Exception("Error " + e.getMessage(), e);

    }
  }
////////////////////////////////////////////////////////////////////////
  
  }

