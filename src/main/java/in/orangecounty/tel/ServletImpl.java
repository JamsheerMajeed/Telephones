package in.orangecounty.tel;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by jamsheer on 3/30/15.
 */
public class ServletImpl extends HttpServlet {

    public void doGet(HttpServletRequest request,HttpServletResponse response)throws IOException{
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<h1>List Extensions</h1>");
        out.println("</body>");
        out.println("</html>");
    }
}
