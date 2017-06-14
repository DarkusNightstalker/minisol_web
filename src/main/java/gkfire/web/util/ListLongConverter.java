/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gkfire.web.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Jhoan Brayam
 */
@FacesConverter("gkfire.converter.ListLong")
public class ListLongConverter implements Converter {

    @SuppressWarnings("unchecked")
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {

        if (value == null || value.length() < 1) {
            return Collections.EMPTY_LIST;
        }
        String[] strSplit = value.split("::");
        List<Long> result = new ArrayList();
        for (String str : strSplit) {
            result.add(Long.parseLong(str));
        }
        return result;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
        if (value == null) {
            return "";
        }
        List<Long> entry = (List<Long>) value;
        return StringUtils.join(entry, "::");

    }
}
