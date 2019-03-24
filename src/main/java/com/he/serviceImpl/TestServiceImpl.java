package com.he.serviceImpl;


import com.he.annaotation.MoodService;
import com.he.service.TestService;

@MoodService("moodService")
public class TestServiceImpl implements TestService {


   public String query(String name, int age) {
        return "name=="+name+",age==="+age;
    }


}
