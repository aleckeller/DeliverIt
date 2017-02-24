package com.aleckeller.deliverit;

import java.util.HashMap;

/**
 * Created by aleckeller on 2/23/17.
 */
public class Post {
    private String name;
    private String placeAddress;
    private String userAddress;
    private String orderItems;
    private String amount;
    private String placeName;

    public Post(){

    }
    public Post(String name, String placeAddress, String userAddress, String orderItems, String amount, String placeName){
        this.name = name;
        this.placeAddress = placeAddress;
        this.userAddress = userAddress;
        this.orderItems = orderItems;
        this.amount = amount;
        this.placeName = placeName;
    }

    public HashMap<String, String> getDetails(){
        HashMap<String, String> result = new HashMap<>();
        result.put("name", name);
        result.put("placeAddress", placeAddress);
        result.put("userAddress", userAddress);
        result.put("orderItems", orderItems);
        result.put("amount", amount);
        result.put("placeName", placeName);

        return result;
    }
}
