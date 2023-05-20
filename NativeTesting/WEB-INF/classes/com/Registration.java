package com;

import java.io.File;
import java.io.FileWriter;

public class Registration {

    public boolean register(String username,String password) {

        UserBean user = new UserBean();
        user.setUserName(username);
        user.setPassword(password);
        // user.setId(id);

        File file = null;
        try{
            String userDirectory = "D:\\UserFiles";
            file = new File(userDirectory+"\\"+username);
            file.mkdir();
        
    
        }
        catch(Exception e){
            ////System.out.println(e);
            return false;
        }
        
        UserDAO userdao = new UserDAO();
        return userdao.add(user);


    }

}
