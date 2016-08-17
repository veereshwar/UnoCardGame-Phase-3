
package sa42.Team9.Conn;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author E0015359
 */

@WebFilter(urlPatterns = "/api/*", dispatcherTypes = DispatcherType.REQUEST)
public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse httpResp = (HttpServletResponse) response; 
        httpResp.setHeader("Access-Control-Allow-Origin", "*");
        
        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {
       
    }


    
    
        
}
