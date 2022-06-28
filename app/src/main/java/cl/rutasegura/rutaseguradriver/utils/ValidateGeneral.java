package cl.rutasegura.rutaseguradriver.utils;

import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Created by ISMOY BELIZAIRE on 08/06/2022.
 */
public class ValidateGeneral {

    //Validacion regex para los campos
    public  boolean validarletras(String datos) {
        return datos.matches("[a-zA-Z-ñÑ ]*");
    }

    //Validacion Patterns para el campo email
    public boolean validaremail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
}
