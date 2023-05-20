<%@page import="com.Registration" %>

<%        
String username = request.getParameter("username");
String password = request.getParameter("password");

response.addHeader("Access-Control-Allow-Origin", "*");

Registration registeration = new Registration();
boolean registered = registeration.register(username,password);
out.println(registered);

%>