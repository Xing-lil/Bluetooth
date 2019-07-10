package com.example.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class KNN {
    public final int AP = 3;
    public final int BLOCK = 15;
    public Knn_Date[] data = new Knn_Date[BLOCK];


    public void init(InputStream is) throws IOException {

        for (int i = 0; i < BLOCK; i++) {
            data[i] = new Knn_Date();
        }
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader readTxt = new BufferedReader(reader);
        String textLine = "";
        String str = "";
        while ((textLine = readTxt.readLine()) != null) {
            //str += " " + textLine;
            str += textLine + " ";
        }

        String[] numbersArray = str.split(" ");//括号里还可以改成空格，即读取用空格隔开的数据
        int j = 0;
        for (int i = 0; i < numbersArray.length-1; i += 5) {  //一次循环是一个block
            data[j].setCoordinate(numbersArray[i] + "," + numbersArray[i + 1]);
            data[j].setRssi(new double[]{Integer.parseInt(numbersArray[i + 2]), Integer.parseInt(numbersArray[i + 3]), Integer.parseInt(numbersArray[i + 4])});
            j++;
        }




    }

    public void knn_sort()//冒泡排序,根据距离找到最近的距离
    {
//        for(int i=0;i<BLOCK;i++){
//            Log.e("bubble",i+" "+data[i].getDistance());
//        }
        for (int i = 0; i < BLOCK; i++) {
            for (int j = 0; j < BLOCK - 1-i; j++) {
                if (data[j].getDistance() > data[j + 1].getDistance()) {
                    Knn_Date x = data[j];
                    data[j] = data[j + 1];
                    data[j + 1] = x;
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
//            Log.e("Euclid","distance:"+distance);
            data[i].setDistance(distance);
        }
    }
}
