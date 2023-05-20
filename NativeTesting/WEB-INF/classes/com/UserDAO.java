package com;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UserDAO {
    private Connection connection = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    private Connection getConnection() throws SQLException {
        Connection newConnection;
        newConnection = Connect.getInstance().getConnection();
        return newConnection;
    }

    public boolean add(UserBean user) {
       
        // String insertUserQuery = "INSERT INTO usersauthentication VALUES(?, encode(digest(?,'sha256'),'hex'))";
        // String insertUserQuery = "INSERT INTO usersauthentication VALUES(?, crypt('?', gen_salt('bf'))";

        //INSERT INTO usersauthentication (username, password) VALUES ('myuser', crypt('mypassword', gen_salt('bf')));
        String insertUserQuery = "INSERT INTO usersauthentication VALUES(?,?)";
        String insertUserRoleQuery = "INSERT INTO user_roles VALUES(?,?)";
        String checkUser = "SELECT * FROM usersauthentication WHERE username=? AND password=?";

        try {
            connection = getConnection();

            preparedStatement = connection.prepareStatement(checkUser);
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getPassword());

            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {

                preparedStatement.close();
                resultSet.close();
                // ////System.out.println("RESULT : sdkv msdvjsdv jsdv jdvn jsdn ");
                preparedStatement = connection.prepareStatement(insertUserQuery);
                preparedStatement.setString(1, user.getUserName());
                preparedStatement.setString(2, user.getPassword());

                preparedStatement.executeUpdate();

                preparedStatement.close();
                resultSet.close();

                preparedStatement = connection.prepareStatement(insertUserRoleQuery);
                preparedStatement.setString(1, user.getUserName());
                preparedStatement.setString(2, "user");

                preparedStatement.executeUpdate();

                return true;

            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connection != null) {
                    // connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean validateLogin(String username, String password) {
        try {
            connection = getConnection();

            System.out.println("username : "+username+" password: "+password);
        
            String selectQuery = "select username from usersauthentication where username = ? and password=?)";

            preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();
            ////System.out.println("username : " + username + " password: " + password);

            if (resultSet.next()) {
                System.out.println("true!");
                return true;

            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connection != null) {
                    // connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public ArrayList<String> getUsers() {
        try {
            String selectQuery = "SELECT username from user_roles where role=?";
            connection = getConnection();

            preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, "user");
            resultSet = preparedStatement.executeQuery();

            ArrayList<String> users = new ArrayList<>();

            while (resultSet.next()) {

                users.add("\"" + resultSet.getString("username") + "\"");
            }

            return users;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void removeUser(String username) {
        try {
            String deleteQuery = "DELETE from usersauthentication where username=?";
            connection = getConnection();

            preparedStatement = connection.prepareStatement(deleteQuery);
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getUserFiles(String username) {

        try {
            String selectQuery = "SELECT filename from usersFiles where username=? or commonfile=true";
            connection = getConnection();

            preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();

            ArrayList<String> filenames = new ArrayList<>();
            while (resultSet.next()) {
                filenames.add("\"" + resultSet.getString("filename") + "\"");
            }

            ////System.out.println("filenames present");
            ////System.out.println(filenames);

            return filenames;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void removeFile(String username, String filename) {

        Path removeUserFilePath = Paths.get("D:\\UserFiles\\" + username + "\\" + filename);
        boolean isSymbolicLink = Files.isSymbolicLink(removeUserFilePath);
        if (isSymbolicLink) {
            ////System.out.println("File is a symbolic link");
        } else {
            ////System.out.println("File is not a symbolic link");
        }

        try {
            Files.delete(removeUserFilePath);
            ////System.out.println("Symbolic link deleted successfully!");

        } catch (IOException e) {
            ////System.out.println("Error deleting the symbolic link!");
            e.printStackTrace();
        }

    }

    public void removeUserFile(String username, String filename) throws IOException {
        String deleteFileQuery = "DELETE FROM usersFiles where username=? and filename=?";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(deleteFileQuery);

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, filename);

            preparedStatement.executeUpdate();

            String deleteCommonQuery = "DELETE FROM usersFiles where filename=? and commonfile=?";
            preparedStatement = connection.prepareStatement(deleteCommonQuery);

            preparedStatement.setString(1, filename);
            preparedStatement.setBoolean(2, true);

            int rowsDeleted = preparedStatement.executeUpdate();

            if (rowsDeleted > 0) {
                addPrivateFile(filename, username);
            } else {
                removeFile(username, filename);
            }

        }

        catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void addPrivateFile(String filename, String username) throws IOException {

        // Get the path to the common file
        Path commonFilePath = Paths.get("D:\\UserFiles\\CommonFiles\\" + filename);

        for (String users : getUsers()) {

            String user = users.replace("\"", "");
            if (!user.equals(username)) {
                ////System.out.println("private user: " + user);
                insertData(user, filename);
            }
        }

        removeFile(username, filename);

    }

    public void insertData(String username, String filename) {
        String insertQuery = "INSERT INTO usersFiles values(?,?)";
        
        System.out.println("insert : " + username);
        try {

            connection = getConnection();
            preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, filename);

            preparedStatement.executeUpdate();
        }

        catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void insertUserFiles(String username, String filename) throws IOException {

        ////System.out.println(username + "___" + filename);
        String commonDir = "D:\\UserFiles\\CommonFiles\\" + filename;
        String userDir = "D:\\UserFiles\\" + username;
        String fileContent = "";
        File commonFile = new File(commonDir);
        if (commonFile.exists()) {
            fileContent = new String(Files.readAllBytes(Paths.get(commonDir)));
        }

        File file = new File(userDir + "\\" + filename);

        if (!file.exists()) {

            if (!(fileContent.equals(""))) {
                FileWriter writer = new FileWriter(userDir + "\\" + filename);
                writer.write(fileContent);
                writer.flush();
                writer.close();
            }
        }

        insertData(username, filename);

    }

    public void uploadCommon(String filename) {
        String insertQuery = "INSERT into usersfiles values(?,?,?)";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, "Admin");
            preparedStatement.setString(2, filename);
            preparedStatement.setBoolean(3, true);

            preparedStatement.executeUpdate();
        }

        catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // public int validate(boolean role) {

    // String userDirectory = "D:\\UserFiles";

    // if (role == true) {
    // for (String username : this.getUsers()) {
    // File programFile = new File(
    // userDirectory + "\\" + username.replace("\"", ""));

    // if (!programFile.exists()) {
    // if (programFile.mkdir()) {
    // ////System.out.println("Directory is created!");
    // } else {
    // ////System.out.println("Failed to create directory!");
    // }
    // } else {
    // ////System.out.println("Directory already exists.");
    // }
    // }
    // return 2;
    // } else {
    // return 1;
    // }

    // }
}
