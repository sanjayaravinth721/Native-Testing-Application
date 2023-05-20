<%@page import="com.Login" %>

<%        
String username = request.getParameter("j_username");
String password = request.getParameter("j_password");

response.setContentType("text/plain"); 

response.addHeader("Access-Control-Allow-Origin", "*");

Login login = new Login();
boolean val = login.loginUser(username,password);
session.setAttribute("username",username);
out.println(val);
%>

