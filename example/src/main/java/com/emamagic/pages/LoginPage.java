package com.emamagic.pages;

import com.emamagic.model.Auth;
import com.emamagic.util.AbstractNavigable;
import com.emamagic.navigator.Navigator;

public class LoginPage extends AbstractNavigable {

    @Override
    public void display() {
        Navigator.navToHomePage("hi ", new Auth("emamagic"));
    }

}
