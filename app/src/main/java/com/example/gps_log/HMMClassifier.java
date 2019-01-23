package com.example.gps_log;

public class HMMClassifier {
    public int getDiscreteSpeed(float speed) {
        int discreteSpeed = 0;

        if (speed <= 1.34112) {    //  3 mph = 1.34112 m/s
            discreteSpeed = 0; //stopped
        } else if (speed >= 8.9408) {        // 20 mph = 8.9408 m/s
            discreteSpeed = 2; //freeflow
        } else {
            discreteSpeed = 1; //accelerating
        }
        return discreteSpeed;
    }
}
