package com.scholaris.controller;

public abstract class BaseController {
    protected MainLayoutController mainController;

    public void setMainController(MainLayoutController mainController) {
        this.mainController = mainController;
    }
}
