package rest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider  //This will ensure that the filter is used "automatically"
@PreMatching
public class CorsRequestFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestCtx) throws IOException {
        // When HttpMethod comes as OPTIONS, just acknowledge that it accepts...
        System.out.println("HTTP Method (OPTIONS) - Detected!");
        if (requestCtx.getRequest().getMethod().equals("OPTIONS")) {
            // Just send a OK response back to the browser.
            // The response goes through the chain of applicable response filters.
            requestCtx.abortWith(Response.status(Response.Status.OK).build());
        }
    }
} 