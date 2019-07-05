package com.example.bluetooth;


public class KNN {
    public Knn_Date[] data = new Knn_Date[3];
    public final int AP = 3;
    public final int BLOCK = 3;

    public void init() {
        data[0] = new Knn_Date();
        data[1] = new Knn_Date();
        data[2] = new Knn_Date();
        data[0].setCoordinate("3,1");
        data[1].setCoordinate("3,2");
        data[2].setCoordinate("3,3");

        data[0].setRssi(new double[]{76, 84, 74});
        data[1].setRssi(new double[]{86, 86, 76});
        data[2].setRssi(new double[]{89, 87, 64});
    }

    public void knn_sort()//冒泡排序,根据距离找到最近的距离
    {
        for (int i = 0; i < AP; i++) {
            for (int t = i; t < AP - 1; t++) {
                if (data[t].getDistance() > data[t + 1].getDistance()) {
                    Knn_Date x = data[t];
                    data[t] = data[t + 1];
                    data[t + 1] = x;
                }
            }
        }
    }

    public String knn_result() {
        knn_sort();
        return data[0].getCoordinate();
    }

    public void Euclid(double[] rssi_li) {
        for (int i = 0; i < BLOCK; i++) {
            //套公式
            double a = Math.abs((data[i].getRssi())[0] - rssi_li[0]);
            double b = Math.abs((data[i].getRssi())[0] - rssi_li[1]);
            double c = Math.abs((data[i].getRssi())[0] - rssi_li[2]);
            double d = Math.pow(a, 2) + Math.pow(b, 2) + Math.pow(c, 2);
            double distance = d;
            data[i].setDistance(distance);
        }


    }
}


