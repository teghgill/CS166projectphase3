/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Scanner;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(esql); break;
                   case 6: viewRecentOrders(esql); break;
                   case 7: viewOrderInfo(esql); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql); break;
                   case 10: updateMenu(esql); break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql){
      try {
         Scanner scanner = new Scanner(System.in);
         System.out.println("Enter username: ");
         String login = scanner.nextLine();
        
         System.out.println("Enter password: ");
         String password = scanner.nextLine();
        
         System.out.println("Enter phone number: ");
         String phoneNum = scanner.nextLine();

         String query = "INSERT INTO users (login, password, role, favoriteItems, phoneNum) VALUES ('" + login + "', '" + password + "', 'customer', NULL, '" + phoneNum + "');";
      
         esql.executeUpdate(query);
         System.out.println("User created successfully!");

      } catch (Exception e) {
         System.err.println("Error - Unable to create user");
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
      try {
         Scanner scanner = new Scanner(System.in);

         System.out.println("Enter username: ");
         String login = scanner.nextLine();

         System.out.println("Enter password: ");
         String password = scanner.nextLine();

         String query = "SELECT * FROM users WHERE login = '" + login + "' AND password = '" + password + "';";
         int userCount = esql.executeQuery(query);

         if (userCount > 0) {
            System.out.println("Logged in successfully!");
            return login;
         } else {
            System.out.println("Invalid username or password");
            return null;
         }
      }
      catch (Exception e) {
         System.err.println("Error - Unable to log in");
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql) {
      try {
         Scanner scanner = new Scanner(System.in);
         System.out.println("Enter username: ");
         String login = scanner.nextLine();
         String query = "SELECT login, phoneNum, role, favoriteItems FROM users WHERE login = '" + login + "';";
         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         if (result.isEmpty()) {
            System.out.println("User not found");
            return;
         }

         System.out.println("Username: " + result.get(0).get(0));
         System.out.println("Phone Number: " + result.get(0).get(1));
         System.out.println("Role: " + result.get(0).get(2));
         System.out.println("Favorite Items: " + result.get(0).get(3));
      
      } 

      catch (Exception e) {
         System.out.println("Error: Unable to retrieve profile.");
      }
   }
   
   public static void updateProfile(PizzaStore esql) {
      try {
         Scanner scanner = new Scanner(System.in);
         boolean manager = false; 

         System.out.println("Enter login: ");
         String login = scanner.nextLine();

         String managerQuery = "SELECT role FROM users WHERE login = '" + login + "';";
         List<List<String>> managerResult = esql.executeQueryAndReturnResult(managerQuery);

         if (!managerResult.isEmpty() && managerResult.get(0).size() > 0) {
            String result = managerResult.get(0).get(0).trim();
            if (result.equalsIgnoreCase("manager")) {  
               manager = true;
            }
         }

         if (manager == true) {
            System.out.println("Enter login to be updated: ");
            String userToBeUpdated = scanner.nextLine();

            System.out.println("What would you like to update?");
            System.out.println("1. Phone Number");
            System.out.println("2. Password");
            System.out.println("3. Favorite Items");  
            System.out.println("4. Login");
            System.out.println("5. Role");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
               case 1:
                  System.out.println("Enter new phone number: ");
                  String newPhoneNum = scanner.nextLine();
                  String updatePhoneNumQuery = "UPDATE users SET phoneNum = '" + newPhoneNum + "' WHERE login = '" + userToBeUpdated + "';";
                  esql.executeUpdate(updatePhoneNumQuery);
                  break;

               case 2:
                  System.out.println("Enter new password: ");
                  String newPassword = scanner.nextLine();
                  String updatePasswordQuery = "UPDATE users SET password = '" + newPassword + "' WHERE login = '" + userToBeUpdated + "';";
                  esql.executeUpdate(updatePasswordQuery);
                  break;

               case 3:
                  System.out.println("Enter new favorite items: ");
                  String newFavoriteItems = scanner.nextLine();
                  String updateFavoriteItemsQuery = "UPDATE users SET favoriteItems = '" + newFavoriteItems + "' WHERE login = '" + userToBeUpdated + "';";
                  esql.executeUpdate(updateFavoriteItemsQuery);
                  break;

               case 4:
                  System.out.println("Enter new login: ");
                  String newLogin = scanner.nextLine();
                  String updateLoginQuery = "UPDATE users SET login = '" + newLogin + "' WHERE login = '" + userToBeUpdated + "';";
                  esql.executeUpdate(updateLoginQuery);
                  break;

               case 5:
                  System.out.println("Enter new role: ");
                  String newRole = scanner.nextLine();
                  String updateRoleQuery = "UPDATE users SET role = '" + newRole + "' WHERE login = '" + userToBeUpdated + "';";
                  esql.executeUpdate(updateRoleQuery);
                  break;

               default:
                  System.out.println("Invalid choice");
                  break;
            }

         }

         else {
            System.out.println("What would you like to update?");
            System.out.println("1. Phone Number");
            System.out.println("2. Password");
            System.out.println("3. Favorite Items");  

            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
               case 1:
                  System.out.println("Enter new phone number: ");
                  String newPhoneNum = scanner.nextLine();
                  String updatePhoneNumQuery = "UPDATE users SET phoneNum = '" + newPhoneNum + "' WHERE login = '" + login + "';";
                  esql.executeUpdate(updatePhoneNumQuery);
                  break;

               case 2:
                  System.out.println("Enter new password: ");
                  String newPassword = scanner.nextLine();
                  String updatePasswordQuery = "UPDATE users SET password = '" + newPassword + "' WHERE login = '" + login + "';";
                  esql.executeUpdate(updatePasswordQuery);
                  break;

               case 3:
                  System.out.println("Enter new favorite items: ");
                  String newFavoriteItems = scanner.nextLine();
                  String updateFavoriteItemsQuery = "UPDATE users SET favoriteItems = '" + newFavoriteItems + "' WHERE login = '" + login + "';";
                  esql.executeUpdate(updateFavoriteItemsQuery);
                  break;
               default:
                  System.out.println("Invalid choice");
                  break;
            }
         }
      }

      catch (Exception e) {
         System.out.println("Error: Unable to update profile.");
      }
   }

   public static void viewMenu(PizzaStore esql) {
      try {
         String query = "SELECT * FROM Items;";
         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         if (result.isEmpty()) {
            System.out.println("Menu is empty");
            return;
         }

         for (int i = 0; i < result.size(); i++) {
            System.out.println("Item: " + result.get(i).get(0));
            System.out.println("Ingredients: " + result.get(i).get(1));
            System.out.println("Type of Item: " + result.get(i).get(2));
            System.out.println("Price: " + result.get(i).get(3));
            System.out.println("Description: " + result.get(i).get(4));
            System.out.println("-------------------------------------------------");
         }

         System.out.println("Would you like to filter your search? (Y/N)");
         Scanner scanner = new Scanner(System.in);

         String choice = scanner.nextLine();
         if (choice.equalsIgnoreCase("Y")) {
            System.out.println("Would you like to filter by price or type?");
            System.out.println("1. Price");
            System.out.println("2. Type");
            System.out.println("3. Both");
            int filterChoice = scanner.nextInt();
            scanner.nextLine();

            if (filterChoice == 1) {
               System.out.println("Enter maximum price: ");
               int maxPrice = scanner.nextInt();
               scanner.nextLine();
               System.out.println("Would you like to sort by highest to lowest price or lowest to highest price?");
               System.out.println("1. Highest to lowest");
               System.out.println("2. Lowest to highest");
               System.out.println("3. Neither");
               int sortChoice = scanner.nextInt();
               scanner.nextLine();
               String filterPriceQuery = "";
               if (sortChoice == 1) filterPriceQuery = "SELECT * FROM Items WHERE price <= " + maxPrice + " ORDER BY price DESC;";
               else if (sortChoice == 2) filterPriceQuery = "SELECT * FROM Items WHERE price <= " + maxPrice + " ORDER BY price ASC;";
               else filterPriceQuery = "SELECT * FROM Items WHERE price <= " + maxPrice + ";";
               List<List<String>> filteredResult = esql.executeQueryAndReturnResult(filterPriceQuery);

               if (filteredResult.isEmpty()) {
                  System.out.println("No items found within that price range");
                  return;
               }

               for (int i = 0; i < filteredResult.size(); i++) {
                  System.out.println("Item: " + filteredResult.get(i).get(0));
                  System.out.println("Ingredients: " + filteredResult.get(i).get(1));
                  System.out.println("Type of Item: " + filteredResult.get(i).get(2));
                  System.out.println("Price: " + filteredResult.get(i).get(3));
                  System.out.println("Description: " + filteredResult.get(i).get(4));
                  System.out.println("-------------------------------------------------");
               }
            }

            else if (filterChoice == 2) {
            System.out.println("Enter type of item: ");
            String itemType = scanner.nextLine().trim();
            System.out.println("Would you like to sort by highest to lowest price or lowest to highest price?");
            System.out.println("1. Highest to lowest");
            System.out.println("2. Lowest to highest");
            System.out.println("3. Neither");
            int sortChoice = scanner.nextInt();
            scanner.nextLine();
            String filterTypeQuery = "SELECT * FROM Items WHERE LOWER(TRIM(typeOfItem)) = LOWER(TRIM('" + itemType + "'));";
            if (sortChoice == 1) filterTypeQuery = "SELECT * FROM Items WHERE LOWER(TRIM(typeOfItem)) = LOWER(TRIM('" + itemType + "')) ORDER BY price DESC;";
            else if (sortChoice == 2) filterTypeQuery = "SELECT * FROM Items WHERE LOWER(TRIM(typeOfItem)) = LOWER(TRIM('" + itemType + "')) ORDER BY price ASC;";
            List<List<String>> filteredResult = esql.executeQueryAndReturnResult(filterTypeQuery);

               if (filteredResult.isEmpty()) {
                  System.out.println("No items found within that type");
                  return;
               }

               for (int i = 0; i < filteredResult.size(); i++) {
                  System.out.println("Item: " + filteredResult.get(i).get(0));
                  System.out.println("Ingredients: " + filteredResult.get(i).get(1));
                  System.out.println("Type of Item: " + filteredResult.get(i).get(2));
                  System.out.println("Price: " + filteredResult.get(i).get(3));
                  System.out.println("Description: " + filteredResult.get(i).get(4));
                  System.out.println("-------------------------------------------------");
               }
           }

            else if (filterChoice == 3) {
               System.out.println("Enter type of item: ");
               String itemType = scanner.nextLine().trim();
               System.out.println("Enter maximum price: ");
               int maxPrice = scanner.nextInt();
               System.out.println("Would you like to sort by highest to lowest price or lowest to highest price?");
               System.out.println("1. Highest to lowest");
               System.out.println("2. Lowest to highest");
               System.out.println("3. Neither");
               int sortChoice = scanner.nextInt();
               scanner.nextLine();
               String filterTypeQuery = "SELECT * FROM Items WHERE price <= " + maxPrice + " AND LOWER(TRIM(typeOfItem)) = LOWER(TRIM('" + itemType + "'));";
               if (sortChoice == 1) filterTypeQuery = "SELECT * FROM Items WHERE price <= " + maxPrice + " AND LOWER(TRIM(typeOfItem)) = LOWER(TRIM('" + itemType + "')) ORDER BY price DESC;";
               else if (sortChoice == 2) filterTypeQuery = "SELECT * FROM Items WHERE price <= " + maxPrice + " AND LOWER(TRIM(typeOfItem)) = LOWER(TRIM('" + itemType + "')) ORDER BY price ASC;";
               List<List<String>> filteredResult = esql.executeQueryAndReturnResult(filterTypeQuery);

               if (filteredResult.isEmpty()) {
                  System.out.println("No items found within that type");
                  return;
               }

               for (int i = 0; i < filteredResult.size(); i++) {
                  System.out.println("Item: " + filteredResult.get(i).get(0));
                  System.out.println("Ingredients: " + filteredResult.get(i).get(1));
                  System.out.println("Type of Item: " + filteredResult.get(i).get(2));
                  System.out.println("Price: " + filteredResult.get(i).get(3));
                  System.out.println("Description: " + filteredResult.get(i).get(4));
                  System.out.println("-------------------------------------------------");
               }
         }

            else {
               System.out.println("Invalid choice");
            }
         }
      }

      catch (Exception e) {
         System.out.println("Error: Unable to view menu.");
      }
   }

   public static void placeOrder(PizzaStore esql) {}

   public static void viewAllOrders(PizzaStore esql) {}

   public static void viewRecentOrders(PizzaStore esql) {}

   public static void viewOrderInfo(PizzaStore esql) {}

   public static void viewStores(PizzaStore esql) {}

   public static void updateOrderStatus(PizzaStore esql) {}

   public static void updateMenu(PizzaStore esql) {}

   public static void updateUser(PizzaStore esql) {}


}//end PizzaStore

