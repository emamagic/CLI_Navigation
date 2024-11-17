package com.emamagic.pages;

import com.emamagic.model.Auth;
import com.emamagic.util.AbstractNavigable;
import com.emamagic.navigator.Navigator;

import java.util.Scanner;

public class LoginPage extends AbstractNavigable {
    Scanner sc = new Scanner(System.in);

    @Override
    public void display() {
        System.out.println("insert your name:");
        String name = sc.nextLine();
        Navigator.navToHomePage("hi ", new Auth(name));
    }

}
