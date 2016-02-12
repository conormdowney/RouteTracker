package com.example.conor.routetracker;


/**
 * Created by Conor on 2016-01-07.
 */
public class RouteItem {

    private String startPoint;
    private String endPoint;
    private String distance;
    private String fileName;

    public RouteItem(String startPoint, String endPoint, String distance, String fileName)
    {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.distance = distance;
        this.fileName = fileName;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String newFileName)
    {
        fileName = newFileName;
    }
}
