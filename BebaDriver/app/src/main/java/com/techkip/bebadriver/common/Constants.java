package com.techkip.bebadriver.common;

public class Constants {

    public class URLs {
        public static final String BASE_URL = "http://android.tech-kip.co.ke/Beba/";
        public static final String LOGIN = BASE_URL + "Login/driverlogin.php";
       // public static final String urlRegister = "http://192.168.43.192/Login/reg.php";
        public static final String urlRegister = BASE_URL + "Login/driverreg.php";
        public static final String Url_Send_History = "history/sendhistory.php";


        //request
        public static final String singlerequestfilter = BASE_URL + "request/singlerequest.php";
        public static final String filterrequestlat = BASE_URL + "request/request.php?rider_lat=";


        public static final String driverHistory = BASE_URL + "history/driverHistory.php?driver_id=";
        public static final String driverAccount = BASE_URL + "account/account.php?driver_id=";
        public static final String singleaccountfilter = BASE_URL + "account/singleaccount.php";



    }

    // webservice key constants
    public class Params {

        public static final String USERMAIL_KEY = "driver_id";
        public static final String PWD_KEY = "status";
    }
}

