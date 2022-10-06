package com.utopia;

import java.io.IOException;

import java.util.List;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet to add a new avatar.
 */
public class AvatarServlet extends HttpServlet
{
    private AvatarService service = new AvatarService();
    private AuthorisationService authorisationService =
        new AuthorisationService();
    
    /**
     * Add new avatars.
     *
     * POST /avatar.json
     */
    @Override public void doPost(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        authorisationService.checkAuthorised(request);

        response.setContentType("application/json; charset=utf-8");

        final ObjectMapper mapper = new ObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        final List<Avatar> avatars =
            mapper.readValue(request.getReader(),
                             new TypeReference<List<Avatar>>() {
                             });
        service.add(avatars);
    }
}
