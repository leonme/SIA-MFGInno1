package com.sap.sia.wechat.util;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;



public class CORSFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request,
        ContainerResponse response) {

        response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHttpHeaders().add("Access-Control-Allow-Headers",
            "Origin, Content-Type, Accept, Authorization");
//        response.getHttpHeaders().add("Access-Control-Allow-Credentials","true");
        //response.getHttpHeaders().add("Access-Control-Allow-Methods",
          //  "GET, POST, PUT, DELETE, OPTIONS, HEAD");

        return response;
    }
}