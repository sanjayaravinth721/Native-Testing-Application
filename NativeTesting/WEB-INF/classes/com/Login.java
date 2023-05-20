package com;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Login {
  public boolean loginUser(String username, String password) {

    UserDAO userDAO= new UserDAO();
    
    boolean auth = userDAO.validateLogin(username,password);
    System.out.println("yes!");
    if(auth){
      System.out.println("user validation success!");
      
    }
    else {
      System.out.println("user validation not success!");
    }

    return auth;
  }

}
