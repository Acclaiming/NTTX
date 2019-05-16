package io.kurumi.ntt.utils;

import io.kurumi.ntt.twitter.ApiToken;
import java.util.LinkedList;
import twitter4j.Twitter;

public class AppOnlyApi {
    
    public static ApiToken API_1 = new ApiToken("NTT API 1","s45tEGC9syyTx3kHtZLgAcGAY","J8AuGmdzORQfR3xqyFMKKecO4cRm0tCLarveg51uwkrfibSDwl");
    public static ApiToken API_2 = new ApiToken("NTT API 2","yYyAKzqmYyL3Nz8qQijCUc0zZ","cU0ltCoyP55gxKQYjrMWq25QRFldbXQfWfWHtCS4IsrRkFq7uY");
    
    public static LinkedList<Twitter> apis = new LinkedList<>();
    
    static {
        
        apis.add(API_1.createAppOnlyApi());
        apis.add(API_2.createAppOnlyApi());
        
    }
    
    
}
