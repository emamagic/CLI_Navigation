package com.emamagic.pages;

import com.emamagic.model.Auth;
import com.emamagic.util.AbstractNavigable;
import com.emamagic.annotation.Param;

public class HomePage extends AbstractNavigable {

    @Param
    private String greeting;

    @Param
    private Auth auth;

    @Override
    public void display() {
        System.out.println(greeting.concat(auth.name));
    }

}
