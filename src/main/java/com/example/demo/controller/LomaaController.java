package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Login;
import com.example.demo.model.Product;
import com.example.demo.model.UserInfo;
import com.example.demo.service.Contact;
import com.example.demo.service.LomaaService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.razorpay.Card;
import com.razorpay.Payment;

@Controller

public class LomaaController {

  @Autowired
  private LomaaService service;
  @Autowired
  private Login login;
  @Autowired
  MongoOperations mongoOperations;
  @Autowired
  private Contact mail;
  public static String OTPmail;
  
  private static long otpTimestamp;

  @RequestMapping(value = "/category", method = RequestMethod.GET)
  public String hmepage() {
    return "currency.html";

  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  //login
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String loginpage(Model model, HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    if (session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn")) {
      model.addAttribute("message", session.getAttribute("name"));
      return "index.html";
    }
    return "index.html";
  }

  @RequestMapping(value = "/loginsubmit", method = RequestMethod.POST)
  public String loginMethod(HttpServletRequest request, HttpServletResponse response, Model model,
    String mailID, String password) throws Exception {
    String name = service.userLogin(mailID, password);
    if (LomaaService.status == "Success") {
      HttpSession session = request.getSession();
      session.setAttribute("name", name);
      session.setAttribute("mail", mailID);
      session.setAttribute("loggedIn", true);

      model.addAttribute("message", mailID);
      return "redirect:/";
    } else {
      model.addAttribute("message", "Invalid Credentials");
      return "index.html";
    }
  }

 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  //signup
  @RequestMapping(value = "/signup", method = RequestMethod.POST)

  public String Verify(HttpServletRequest request, Model model, String mailID, String password) throws Exception {

    service.verify(mailID, password);

    if (LomaaService.status == "Success") {
      model.addAttribute("message", "verified");
      HttpSession session = request.getSession();
      session.setAttribute("signup_mailID", mailID);
      session.setAttribute("signup_password", password);

      return "index.html";

    } else {
      model.addAttribute("message", "MailID Already Present");
      return "index.html";

    }

  }

  @RequestMapping(value = "/signupsubmit", method = RequestMethod.POST)

  public String SignupMethod(RedirectAttributes RedirectAttributes, HttpServletRequest request, Model model, String fullname, String address, String phonenumber) throws Exception {
    model.addAttribute("fullname", fullname);
    HttpSession session = request.getSession();
    String name = (String) session.getAttribute("signup_mailID");
    String password = (String) session.getAttribute("signup_password");

    service.userSignup(name, password, fullname, address, phonenumber);

    if (LomaaService.status == "Success") {

      RedirectAttributes.addFlashAttribute("loginagain", "Please Log In");

      return "redirect:/";

    } else {

      //error page
      return "redirect:/";

    }

  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  //forgot password
  @RequestMapping(value = "/verifyuser", method = RequestMethod.POST)

  public String Verifyforgotpwd(HttpServletRequest request, Model model, String mailID, String name) throws Exception {
    service.verifyuser(mailID, name);
    HttpSession session = request.getSession();
//    session.setAttribute("name", name);

    LomaaController.OTPmail = mailID;
    if (LomaaService.status == "Success") {
      service.sendEmail(name, mailID);

      if (LomaaService.status == "Success") {
        model.addAttribute("message", "otpsent");
        otpTimestamp = System.currentTimeMillis();
        return "index.html";

      } else {
        model.addAttribute("message", "Failed to Send/ Incorrect Mail-Id");
        return "index.html";

      }

    } else {
      model.addAttribute("message", "Wrong Details");
      return "index.html";

    }

  }


  //Verifing user otp
  @RequestMapping(value = "/verifyOTP", method = RequestMethod.POST)
  public String verifyOTP(Model model, String otp) {

      if (LomaaService.otp.equals(otp)) {
    	  
    	  long currentTime = System.currentTimeMillis();
          	if (currentTime - otpTimestamp <= 60000) {
          	    System.out.print(currentTime);

	        model.addAttribute("message", "Successfully VERIFIED");
	        return "index.html";

      }
          	 model.addAttribute("message", "OTP Expired");
 	        return "index.html";
      }
      else {
          model.addAttribute("message", "Incorrect OTP");
          return "index.html";
      }


 
  }

  // allowing user to set new password
  @RequestMapping(value = "/newpassword", method = RequestMethod.POST)
  public String newpassword(RedirectAttributes RedirectAttributes, HttpServletRequest request, Model model, String password) throws IOException {

    try {
      service.userUpdatePwd(LomaaController.OTPmail, password);

      if (LomaaService.status == "Success") {
        HttpSession session = request.getSession();
        RedirectAttributes.addFlashAttribute("loginagain", "Please Log In");

        return "redirect:/";

      } else {
        model.addAttribute("message", "Please try again");
        return "index.html";
      }

    } catch (Exception e) {
      System.out.print(e);

    }

    return "index.html";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  //updateProfile
  @RequestMapping(value = "/updateProfile")
  public String show(RedirectAttributes RedirectAttributes, HttpServletResponse response, HttpServletRequest request, Model model, String fullname, String address, String phonenumber, String password) throws Exception {
    HttpSession session = request.getSession();
System.out.print(session.getAttribute("mail"));
    Query query = new Query();
    query.addCriteria(Criteria.where("mailID").is(session.getAttribute("mail")));

    UserInfo data = mongoOperations.findOne(query, UserInfo.class);
    model.addAttribute("data", data);

    String mailID = (String) session.getAttribute("mail");

    //////
    
    if (session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn")) {
        model.addAttribute("name", session.getAttribute("name"));
      }
    
    ///////
    
    if (fullname != null && address != null && phonenumber != null && password != null) {
      service.updateuserinfo(mailID, fullname, address, phonenumber, password);
      model.addAttribute("message", "Updated please login again");

      RedirectAttributes.addFlashAttribute("loginagain", "Successfully! Please Login Again");

      logout(request, response);

      return "redirect:/";
      //	  }
      //		

    } else {
      return "account.html";
    }

  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  //adminlogin
  @RequestMapping(value = "/adminpanel", method = RequestMethod.GET)
  //	@PostMapping(value="/login")
  public String adminpanel() {
    return "admin.html";
  }

  @RequestMapping(value = "admin/submit", method = RequestMethod.POST)
  public String adminpanel(Model model, String softtoys, String homeFurnishing, String decorativeItems, String item_code, String item_name, String item_color, String item_size, String item_weight, String item_material, String item_price, MultipartFile image1, MultipartFile image2, MultipartFile image3,MultipartFile image4) throws IOException {
    

    
    String selectedCheckboxValue="";
    if (softtoys!= null) {
        selectedCheckboxValue = "softtoys";
    } else if (homeFurnishing!= null) {
        selectedCheckboxValue = "homeFurnishing";
    } else if (decorativeItems!= null) {
        selectedCheckboxValue = "decorativeItems";
    }
    
    System.out.print(selectedCheckboxValue);
    try {

        service.product(item_code, item_name, item_price, image1, image2, image3, image4, selectedCheckboxValue,item_color,item_weight,item_size,item_material);
        System.out.print(selectedCheckboxValue);
      if (LomaaService.status == "Success") {
        String savedir = "src\\main\\resources\\static\\images";
        
        Path path1 = Paths.get(savedir + File.separator + image1.getOriginalFilename());
        Files.copy(image1.getInputStream(), path1, StandardCopyOption.REPLACE_EXISTING);
        
        Path path2 = Paths.get(savedir + File.separator + image2.getOriginalFilename());
        Files.copy(image2.getInputStream(), path2, StandardCopyOption.REPLACE_EXISTING);
        
        Path path3 = Paths.get(savedir + File.separator + image3.getOriginalFilename());
        Files.copy(image3.getInputStream(), path3, StandardCopyOption.REPLACE_EXISTING);
        
        Path path4 = Paths.get(savedir + File.separator + image4.getOriginalFilename());
        Files.copy(image4.getInputStream(), path4, StandardCopyOption.REPLACE_EXISTING);
        System.out.print("cjsusbs");

        model.addAttribute("message", "uploaded successfully");

      } else {
          System.out.print("cjssssssusbs");

        model.addAttribute("message", "Image ID already exist");

      }

    } catch (Exception e) {
      model.addAttribute("message","Image already present in folder");

    }

    return "admin.html";
  }
  
  @RequestMapping(value = "/admin/delete", method = RequestMethod.POST)
  public String AdmindeleteItem(Model model, String item_code) {
	  try {
		  boolean documentExists = mongoOperations.exists(Query.query(Criteria.where("item_code").is(item_code)), Product.class);

		  if(documentExists) {
		  mongoOperations.remove(Query.query(Criteria.where("item_code").is(item_code)), Product.class);
	      model.addAttribute("dltmessage", "Successfully deleted");

		    return "admin.html";
	  }
		  else {
		      model.addAttribute("dltmessage", "Image ID doesn't exist");
			    return "admin.html";
		  }

		  }
	  catch(Exception e){
	      model.addAttribute("dltmessage", "Some Error Occured");
		    return "admin.html";
	  }
	 
		  

  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  //productpage
  @RequestMapping(value = "/productpage", method = RequestMethod.GET)
  public String productpage(Model model, String linkValue, HttpServletRequest request) {
    HttpSession session = request.getSession();
    
    Criteria criteria = Criteria.where("category").is(linkValue);

    // Use the criteria object in your query to fetch only the documents that meet the criteria
    List<Product> data = mongoOperations.find(Query.query(criteria), Product.class);

    if(data.isEmpty()){
    		
        model.addAttribute("info", "images/c.png");
    }else {
        model.addAttribute("data", data);

    }
    

   System.out.print(data);


    if (session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn")) {
      model.addAttribute("name", session.getAttribute("name"));
    }

    return "softtoy.html";
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  //Productpreview
  @RequestMapping(value = "/softtoy", method = RequestMethod.POST)
  public String softtoy(Model model, String message, HttpServletRequest request) {

    String urlString = message;
    String[] parts = urlString.split("/");
    String filename = parts[parts.length - 1].replaceAll(",", "");

    Query query = new Query();
    query.addCriteria(Criteria.where("image1").is(filename));
    Product product = mongoOperations.findOne(query, Product.class);
    model.addAttribute("data", product);
//    model.addAttribute("name", "code");
    HttpSession session = request.getSession();
    if (session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn")) {
        model.addAttribute("name", session.getAttribute("name"));
      }

    return "preview.html";
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
String repeat_item;
boolean flag=false;
  //addtocart
  @RequestMapping(value = "/addtocart", method = RequestMethod.POST)
  public String addToCart(Model model, @RequestParam("code") String itemId, HttpServletResponse response, HttpServletRequest request) {
      HttpSession session = request.getSession();
      if (session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn")) {
          Cookie[] cookies = request.getCookies();
          if (cookies != null) {
              for (Cookie cookie : cookies) {
                  if (cookie.getName().equals("shopping_cart")) {
                      String decodedValue = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                      String[] itemIds = decodedValue.split(",");
                      for (String existingItemId : itemIds) {
                          if (existingItemId.equals(itemId)) {                        	 
                              // Item already exists in cart, do not add again
                        	  flag=true;
                        	  repeat_item=itemId;
                              return "redirect:/cart";
                          }
                      }
                      // Item not found in cart, add to cart
                      String shoppingCartData = decodedValue + "," + itemId;
                      String encodedShoppingCartData = URLEncoder.encode(shoppingCartData, StandardCharsets.UTF_8);
                      cookie.setValue(encodedShoppingCartData);
                      cookie.setMaxAge(24 * 60 * 60); // Set expiration time to 1 day
                      response.addCookie(cookie);
                      return "redirect:/cart";
                  }
              }
          }
          // No shopping cart cookie found, create new cookie with item ID
          String encodedShoppingCartData = URLEncoder.encode(itemId, StandardCharsets.UTF_8);
          Cookie cookie = new Cookie("shopping_cart", encodedShoppingCartData);
          cookie.setMaxAge(24 * 60 * 60); // Set expiration time to 1 day
          response.addCookie(cookie);
          return "redirect:/cart";
      } else {
          return "login.html";
      }
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  //cart
  @RequestMapping(value = "/cart")
  public String cart(Model model, HttpServletResponse response, HttpServletRequest request,   @RequestParam(value = "remove", required = false) String removeItem) {   

    HttpSession session = request.getSession();
    if (session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn")) {
    	
    	    if (session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn")) {
    	        model.addAttribute("name", session.getAttribute("name"));
    	      }
    	// Check for removeItem parameter
        if (removeItem != null && !removeItem.isEmpty()) {
          Cookie[] cookies = request.getCookies();
          if (cookies != null) {
            for (Cookie cookie: cookies) {
              if (cookie.getName().equals("shopping_cart")) {
                String decodedValue = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                String[] items = decodedValue.split(",");
                List<String> updatedItems = new ArrayList<String>();
                for (String item: items) {
                  if (!item.equals(removeItem)) {
                    updatedItems.add(item);
                  }
                }
                String updatedCartValue = String.join(",", updatedItems);
                cookie.setValue(URLEncoder.encode(updatedCartValue, StandardCharsets.UTF_8));
                cookie.setMaxAge(24 * 60 * 60); // Set expiration time to 1 day
                response.addCookie(cookie);
                break;
              }
            }
          }
        }	
    	
    // Code for fetching and displaying	
      Cookie[] cookies = request.getCookies();
      if (cookies == null || cookies.length == 0) {
        // Cookie is null or empty
//        model.addAttribute("info", "Cart Is Empty");
        model.addAttribute("data", "");
      } else {
        // Cookie is not null and has data
    	  if(flag==true) {
      		System.out.println("itemwhichisrep "+repeat_item);
        	  model.addAttribute("inc",repeat_item);
        	  repeat_item="";
        	  flag=false;
      	}
      	
        String decodedValue = "";
        for (Cookie cookie: cookies) {
          if (cookie.getName().equals("shopping_cart")) {
            String shoppingCartData = cookie.getValue();
            if (shoppingCartData != null && !shoppingCartData.isEmpty()) {
              decodedValue = URLDecoder.decode(shoppingCartData, StandardCharsets.UTF_8);
              break;
            }
          }
        }

        if (decodedValue.isEmpty()) {
          // Decoded value is empty
            model.addAttribute("info", "images/cart_1.png");

          model.addAttribute("data", "");
        } else {
          // Decoded value has data, process it
          String[] items = decodedValue.split(",");
          List < Product > cartItems = new ArrayList < > ();
          for (String itemId: items) {
            Query query = new Query();
            query.addCriteria(Criteria.where("item_code").is(itemId));
            Product product = mongoOperations.findOne(query, Product.class);
            cartItems.add(product);
          }
          model.addAttribute("data", cartItems);
        }
      }

      return "cart.html";
    } else {
      return "login.html";
    }

  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //logout
  @RequestMapping(value = "/logout", method = RequestMethod.GET)
  public String logout(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie: cookies) {
          if (cookie.getName().equals("shopping_cart")) {
            cookie.setValue("");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
            break;
          }
        }
      }
    }
    return "redirect:/";
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  
  @RequestMapping(value="/checkout")
  public String checkout(Model model, @RequestParam String name, @RequestParam String phoneNumber, @RequestParam String address, @RequestParam String comment, @RequestParam Map<String, String> formData, HttpServletRequest request) throws MessagingException {
 	 HttpSession session = request.getSession();
 	 ArrayList < Product > cartItems = new ArrayList < > ();
 	 ArrayList<String> attachments=new ArrayList<String>();

 	
 	 
 	 if (session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn")) {
 		 
 		 System.out.print(session.getAttribute("mail"));
 		 System.out.println(session.getAttribute("name"));
 		 
 		 Cookie[] cookies = request.getCookies();
 		 String decodedValue = "";
 	        for (Cookie cookie: cookies) {
 	          if (cookie.getName().equals("shopping_cart")) {
 	            String shoppingCartData = cookie.getValue();
 	  		    System.out.println("items"+shoppingCartData);

 	  		
 	            if (shoppingCartData != null && !shoppingCartData.isEmpty()) {
 	              decodedValue = URLDecoder.decode(shoppingCartData, StandardCharsets.UTF_8);
 	 	  		    System.out.println("decodedValue"+decodedValue); 	 	  		    
 	 	  		 if (decodedValue.startsWith(",")) {
 	 	  			decodedValue = decodedValue.substring(1);
 	             }

 	              break;
 	            }
 	          }
 	        }

 	        if (decodedValue.isEmpty()) {
 	          model.addAttribute("info","cart is empty");

 	         
 	        } else {
 	          // Decoded value has data, process it
 	          String[] items = decodedValue.split(",");

 	          for (String itemId: items) {
 	            Query query = new Query();
 	            query.addCriteria(Criteria.where("item_code").is(itemId));
 	            Product product = mongoOperations.findOne(query, Product.class);
 	            cartItems.add(product);		            
 	            
 	         //  String Qty = formData.get(itemId);
 	          // System.out.println("Quantity of ItemCode "+itemId+": "+ Qty);
    		    System.out.println("xscdvsfbgrnhtmjyk,u"+ product.getImage1());
 	          
 	            String path="./src/main/resources/static/images/"+product.getImage1();
 	            attachments.add(path);
 	          }

 	        }
    		    System.out.println(cartItems);
    		    System.out.println("images are"+attachments);
    		    
    		    StringBuilder str = new StringBuilder();
    		    
    		    str.append("Customer Details:").append("<br>");
    		    str.append("Name: ").append(name).append("<br>");
    		    str.append("PhoneNumber: ").append(phoneNumber).append("<br>");
    		    str.append("Address: ").append(address).append("<br>");
    		    str.append("Message: ").append(comment).append("<br>");

    		    
 		 mail.sendEmail("workwebsite2000@gmail.com", "Cart Items",str, formData, cartItems, session.getAttribute("name"), session.getAttribute("mail"), attachments);
 	 }
 	 
 	return "redirect:/checkoutPage";
// 	 return "checkout.html";
  }
  
  @RequestMapping(value = "/checkoutPage")
  public String checkoutPage(Model model, HttpServletRequest request) {
	  HttpSession session = request.getSession();
	  String info="Thankyou! "+ session.getAttribute("name") + " for placing oder with LOMAA. You will receive an order confirmation mail at "+session.getAttribute("mail")+".";
      model.addAttribute("info",info);
      model.addAttribute("data", "images/orderreceived.png");
  return "checkout.html";
  }

   
  
  

@RequestMapping(value = "/aboutus")
public String aboutus(Model model, HttpServletRequest request) {
	HttpSession session = request.getSession();
    if (session.getAttribute("loggedIn") != null && (Boolean) session.getAttribute("loggedIn")) {
        model.addAttribute("name", session.getAttribute("name"));
      }
return "aboutus.html";

}

  




}