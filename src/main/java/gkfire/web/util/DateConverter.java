/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gkfire.web.util;

import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Jhoan Brayam
 */
@FacesConverter("javax.faces.DateConverter")
public class DateConverter  extends DateTimeConverter {
    
    public DateConverter() {
        setPattern("dd/MM/yyyy");
    }
}
