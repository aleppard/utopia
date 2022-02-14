package com.utopia;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Hello world!
 */
public class GameServlet extends HttpServlet
{
    @Override public void doGet(HttpServletRequest request,
                                HttpServletResponse response)
        throws IOException {

        response.setContentType("text/html; charset=utf-8");
        response.getWriter().print("<html>Hello world!</html>");
    }
}
