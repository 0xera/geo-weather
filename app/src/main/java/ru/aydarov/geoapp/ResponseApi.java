package ru.aydarov.geoapp;

public class ResponseApi {
    public Main main;

    public ResponseApi(Main main) {
        this.main = main;
    }


    class Main {
        public float temp;

        public Main(float temp) {
            this.temp = temp;
        }
    }

}


