package com.jason.designprojecttwo.Utility;

public class RequestModel {

    private String uniqueID;
    private String fault;
    private String initialDiagnosis;
    private String date;
    private String status;

    private RequestModel() {
    }

    public RequestModel(String uniqueID, String fault, String initialDiagnosis, String date, String status) {
        this.uniqueID = uniqueID;
        this.fault = fault;
        this.initialDiagnosis = initialDiagnosis;
        this.date = date;
        this.status = status;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getFault() {
        return fault;
    }

    public void setFault(String fault) {
        this.fault = fault;
    }

    public String getInitialDiagnosis() {
        return initialDiagnosis;
    }

    public void setInitialDiagnosis(String initialDiagnosis) {
        this.initialDiagnosis = initialDiagnosis;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
