package com.techkip.bebarider.common;

public class Constants {
    // web service url constants
    public class URLs {
        public static final String BASE_URL = "http://android.tech-kip.co.ke/Beba/";

        public static final String Url_Send_Request = BASE_URL + "request/sendrequest.php";
        public static final String LOGIN = BASE_URL + "Login/riderlogin.php";
        public static final String riderHistory = BASE_URL + "history/riderHistory.php?rider_id=";
        public static final String urlRegister = BASE_URL + "Login/riderreg.php";

    }

    // webservice key constants
    public class Params {

        public static final String RIDER_ID_KEY = "rider_id";
        public static final String STATUS_KEY = "status";
    }
}

