package com.example.bluetooth;

public class Knn_Date {
    private String coordinate="kkkkk";
    private double distance=1000;
    private double[] rssi = new double[3];

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double[] getRssi() {
        return rssi;
    }

    public void setRssi(double[] rssi) {
        this.rssi = rssi;
    }
}
