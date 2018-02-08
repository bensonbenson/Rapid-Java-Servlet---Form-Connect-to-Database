//Benson Tran
//CSCI 5520U
//Exercise 37.13
//Due January 24, 2018
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;

public class StaffServlet extends HttpServlet {
    private PreparedStatement preparedStatementView;
    private ResultSet resultView;
    private PreparedStatement preparedStatementInsert;
    private PreparedStatement preparedStatementUpdate;
    private PreparedStatement preparedStatementCheckID;
    private String id;
    private String lastName;
    private String firstName;
    private String mi;
    private String address;
    private String city;
    private String state;
    private String phone;
    private String email;
    private String viewBtn;
    private String insertBtn;
    private String updateBtn;
    private String resetBtn;
    
    public void init() throws ServletException{
        initializeJdbc();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;");
        PrintWriter out = response.getWriter(); 
            
        //Get parameters from the client
        id = request.getParameter("id");
        lastName = request.getParameter("lastName");
        firstName = request.getParameter("firstName");
        mi = request.getParameter("mi");
        address = request.getParameter("address");
        city = request.getParameter("city");
        state = request.getParameter("state");
        phone = request.getParameter("phone");
        email = request.getParameter("email");
        viewBtn = request.getParameter("view");
        insertBtn = request.getParameter("insert");
        updateBtn = request.getParameter("update");
        resetBtn = request.getParameter("reset");
        
        
        try{
            if ((id.isEmpty())){
                printForm(request, response, "ID entry cannot be empty!");
            }
            else
            {
                //View button clicked
                if("View".equals(viewBtn))
                {
                view(id);
                
                if(resultView.next())
                {   
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title> Staff Registration </title>");
                        out.println("<body>");
                        out.println("<fieldset>");
                        out.println("<legend>View, Insert, or Update Staff</legend>");
                        out.println("<form method = \"POST\" action = \"StaffServlet\">");
                        out.println("<p>ID <input type = \"text\" name = \"id\" value =" + id + "><font color = \"#FF0000\">*</font></p>");
                        out.println("<p>Last Name <input type = \"text\" name = \"lastName\" value =" + resultView.getString(1) + ">&nbsp;" +
                            "First Name <input type = \"text\" name = \"firstName\" value =" + resultView.getString(2) + ">&nbsp;" +
                            "Middle Initial <input type = \"text\" name = \"mi\" size = \"1\" maxlength = \"1\" value =" + resultView.getString(3) + ">" +
                            "</p>");
                        out.println("<p>Address <input type = \"text\" name = \"address\" value =" + resultView.getString(4) + "></p>");
                        out.println("<p>City <input type = \"text\" name = \"city\" value =" + resultView.getString(5) + "> &nbsp;" +
                            "State <input type = \"text\" name = \"state\" size = \"2\" maxlength = \"2\" value =" + resultView.getString(6) + "></p>");
                        out.println("<p>Telephone <input type = \"text\" name = \"phone\" maxlength = \"10\" value =" + resultView.getString(7) + "> &nbsp;" +
                            "Email <input type = \"text\" name = \"email\" value =" + resultView.getString(8) + "></p>");
                        out.println("<p><input type = \"submit\" name = \"view\" value = \"View\"> &nbsp;" +
                            "<input type = \"submit\" name = \"insert\" value = \"Insert\"> &nbsp;" +
                            "<input type = \"submit\" name = \"update\" value = \"Update\"> &nbsp;" +
                            "<input type = \"reset\" name = \"reset\" value = \"Reset\"></p>");
                        out.println("<p><font color = \"#FF0000\">* required field</font></p>");
                        out.println("</form>");
                        out.println("</fieldset>");
                        out.println("</body>");
                        out.println("</head>");
                        out.println("</html>");
                }
                else
                {
                    //ID is not in DB
                    printForm(request, response, "ID not found");
                }
                }
                
                //Insert button clicked
                if("Insert".equals(insertBtn))
                {
                    if (!isIdInDb(id))
                    {
                        insert(id, lastName, firstName, mi, address, city, state, phone, email);
                        printForm(request, response, firstName + " " + lastName + " has been added successfully.");
                    }
                    else
                    {
                        //ID is already in DB
                        printForm(request, response, "ID " + id + " is a duplicate in Database.");
                    }
                }
                
                //Update button clicked
                if("Update".equals(updateBtn))
                {
                    if (isIdInDb(id))
                    {
                        update(id, lastName, firstName, mi, address, city, state, phone, email);
                        printForm(request, response, firstName + " " + lastName + " has been updated.");
                    }
                    else
                    {
                        //ID is not in DB
                        printForm(request, response, "ID " + id + " is not in Database.");
                    }
                }
                
                //Reset button clicked
                if("Reset".equals(resetBtn))
                {
                    id = "";
                    firstName = "";
                    lastName = "";
                    mi = "";
                    address = "";
                    city = "";
                    state = "";
                    phone = "";
                    email = "";
                    printForm(request, response, "");
                }
            }
        }
        catch(Exception ex){
            printForm(request, response, "Error: " + ex.getMessage());
        }
        finally{
            out.close();
        }
    }
    
    //Initialize Database
    private void initializeJdbc(){
        try{
            //Load Driver
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Driver loaded.");
            
            //Connect Database
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/javabook", "root", "rootpass");
            System.out.println("Database connected.");
            
            //Prepared statement for view
            String queryView = "select lastname, firstname, mi, address, city, state, telephone, email from staff where id = ?";
            preparedStatementView = connection.prepareStatement(queryView);
        
            //Prepared statement for insert
            String queryInsert = "insert into staff values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            preparedStatementInsert = connection.prepareStatement(queryInsert);

            //Prepared statement for update
            String queryUpdate = "update staff set lastname = ?, firstname = ?, mi = ?, address = ?, city = ?, state = ?, telephone = ?, email= ? where id = ?";
            preparedStatementUpdate = connection.prepareStatement(queryUpdate);
            
            //Prepared statement to find ID in DB
            String queryID = "select id from staff where id = ?";
            preparedStatementCheckID = connection.prepareStatement(queryID);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    //View
    private void view(String id) throws SQLException{
        preparedStatementView.setString(1, id);
        resultView = preparedStatementView.executeQuery();
    }
    
    //Insert
    private void insert(String id, String lName, String fName, String mi, String address, String city, String state, String phone, String email) throws SQLException{
        preparedStatementInsert.setString(1, id);
        preparedStatementInsert.setString(2, lName);
        preparedStatementInsert.setString(3, fName);
        preparedStatementInsert.setString(4, mi);
        preparedStatementInsert.setString(5, address);
        preparedStatementInsert.setString(6, city);
        preparedStatementInsert.setString(7, state);
        preparedStatementInsert.setString(8, phone);
        preparedStatementInsert.setString(9, email);
        preparedStatementInsert.executeUpdate();
    }
    
    //Update
    private void update(String id, String lName, String fName, String mi, String address, String city, String state, String phone, String email) throws SQLException{
        preparedStatementUpdate.setString(1, lName);
        preparedStatementUpdate.setString(2, fName);
        preparedStatementUpdate.setString(3, mi);
        preparedStatementUpdate.setString(4, address);
        preparedStatementUpdate.setString(5, city);
        preparedStatementUpdate.setString(6, state);
        preparedStatementUpdate.setString(7, phone);
        preparedStatementUpdate.setString(8, email);
        preparedStatementUpdate.setString(9, id);
        preparedStatementUpdate.executeUpdate();
    }
    
    //Check if ID is in DB, returns true if ID is in DB
    private boolean isIdInDb(String id) throws SQLException{
        preparedStatementCheckID.setString(1, id);
        resultView = preparedStatementCheckID.executeQuery();
        if(resultView.next())
        {
            return true;
        }
        return false;
    }
    
    //Rewrite page form to print out statements
    private void printForm(HttpServletRequest request, HttpServletResponse response, String message) throws ServletException, IOException{
        response.setContentType("text/html;");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title> Staff Registration </title>");
        out.println("<body>");
        out.println("<fieldset>");
        out.println("<legend>View, Insert, or Update Staff</legend>");
        out.println("<form method = \"POST\" action = \"StaffServlet\">");
        out.println("<p>ID <input type = \"text\" name = \"id\" value =" + id + "><font color = \"#FF0000\">*</font></p>");
        out.println("<p>Last Name <input type = \"text\" name = \"lastName\" value =" + lastName + ">&nbsp;" +
            "First Name <input type = \"text\" name = \"firstName\" value =" + firstName + ">&nbsp;" +
            "Middle Initial <input type = \"text\" name = \"mi\" size = \"1\" maxlength = \"1\" value =" + mi + ">" +
            "</p>");
        out.println("<p>Address <input type = \"text\" name = \"address\" value =" + address + "></p>");
        out.println("<p>City <input type = \"text\" name = \"city\" value =" + city + "> &nbsp;" +
            "State <input type = \"text\" name = \"state\" size = \"2\" maxlength = \"2\" value =" + state + "></p>");
        out.println("<p>Telephone <input type = \"text\" name = \"phone\" maxlength = \"10\" value =" + phone + "> &nbsp;" +
            "Email <input type = \"text\" name = \"email\" value =" + email + "></p>");
        out.println("<p><input type = \"submit\" name = \"view\" value = \"View\"> &nbsp;" +
            "<input type = \"submit\" name = \"insert\" value = \"Insert\"> &nbsp;" +
            "<input type = \"submit\" name = \"update\" value = \"Update\"> &nbsp;" +
            "<input type = \"submit\" name = \"reset\" value = \"Reset\"></p>");
        out.println("<p><font color = \"#FF0000\">* required field</font></p>");
        out.println("</form>");
        out.println("</fieldset>");
        out.println(message);
        out.println("</body>");
        out.println("</head>");
        out.println("</html>");
        
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
