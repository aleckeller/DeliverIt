package com.aleckeller.deliverit;

/**
 * Created by aleckeller on 2/1/17.
 */
public class AppConfig {
    private static String ip_address = "10.129.52.55";

    // Server user login url
    public static String URL_LOGIN = "http://" + ip_address + ":8888/deliverIt_api/login.php";

    // Server user register url
    public static String URL_REGISTER = "http://" + ip_address + ":8888/deliverIt_api/register.php";

    // Keep track of if facebook is logged in
    public static boolean fbLoggedIn = false;


}
