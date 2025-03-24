package com.akida.ecommerce.registration;

public record PwdRequest (
    String current,
    String newPWD,
    String renew
){}
