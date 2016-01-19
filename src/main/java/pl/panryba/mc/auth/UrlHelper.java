/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author PanRyba.pl
 */
public class UrlHelper {
    public static String getUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        
        BufferedReader bufferedreader =
                new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
        
        try {
            String response = bufferedreader.readLine();
            return response;
        }
        finally {
            bufferedreader.close();
        }
    }
}
