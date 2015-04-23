/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.neotech.loltoast.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.http.*;

public class MyServlet extends HttpServlet {

    String responseText = "";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Please use the form to POST to this url");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String region = req.getParameter("region");
        String name = req.getParameter("name");
        resp.setContentType("text/plain");
        if (region == null || name == null) {
            resp.getWriter().println("Please enter a region and name");
        }

        responseText = "";

        // Make LoL API call and return response
        // Example: https://euw.api.pvp.net/api/lol/euw/v1.4/summoner/by-name/Sin9ularity?api_key=74f28a6e-c769-40c4-b44a-a65608196ca1
        //String baseURL = "https://euw.api.pvp.net/api/lol/euw/v1.4/summoner/by-name/";
        String baseHTTP = "https://";
        String mainURL = ".api.pvp.net/api/lol/";
        String remURL = "/v1.4/summoner/by-name/";
        String endURL = "?api_key=";

        InputStream app_properties_stream = this.getServletContext().getResourceAsStream("/WEB-INF/MyServlet.properties");
        Properties app_properties = new Properties();
        app_properties.load(app_properties_stream);
        String lolKey = app_properties.getProperty("lolKey");

        String fullURL = baseHTTP + region + mainURL + region + remURL + name + endURL + lolKey;

        try {
            URL url = new URL(fullURL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                responseText = responseText + line;
            }
            reader.close();

        } catch (MalformedURLException e) {
            responseText = "Malformed URL Exception";
        } catch (IOException e) {
            responseText = "IO Exception";
        }

        resp.getWriter().print(responseText);
    }
}
