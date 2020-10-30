package com.jason.designprojecttwo.Utility;

public class StatusModel {


    private String statusName;

    public StatusModel(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    @Override
    public String toString() {
        return statusName;
    }
}
