package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Secrets {

    public static final String TRAB_PROPS_PATH="trab.props";
    public static final String PROP_ASSIGN_OP ="=";
    public static final String SECRETS_DELIMITER="###";
    public static final String SECRETS_ASSIGN_OP="-";


    public static String secretFrom(String serviceName, String propertyName) {
        try(BufferedReader in = new BufferedReader(new FileReader(TRAB_PROPS_PATH))){
            String prop;
            while(!(prop = in.readLine()).contains(propertyName));
            String[] secrets = prop.split(PROP_ASSIGN_OP)[1].split(SECRETS_DELIMITER);
            for(String secret : secrets){
                if(secret.contains(serviceName))
                    return secret.split(SECRETS_ASSIGN_OP, 2)[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
