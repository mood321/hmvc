package com.he.controller;


import com.he.annaotation.MoodAutowired;
import com.he.annaotation.MoodController;
import com.he.annaotation.MoodRequestMapping;
import com.he.annaotation.MoodRequestParam;
import com.he.service.TestService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@MoodController
@MoodRequestMapping("/mood")
public class TestController {

    @MoodAutowired("moodService")
    private TestService moodService;


    @MoodRequestMapping("/query")
    public void query( HttpServletResponse response,HttpServletRequest request,
                      @MoodRequestParam("name") String name, @MoodRequestParam("age") Integer age
    ) {
       
        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            String res = moodService.query(name, age);
            writer.write("TestController.query===>"+res);

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            writer.close();
        }
    }

}
