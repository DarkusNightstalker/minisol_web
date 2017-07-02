/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gkfire.web.util;

import cs.bms.bean.EntityMapBean;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Danny
 */
@FacesConverter(value = "dn.web.converter.arrayConverter")
public class ArrayConverter implements Converter {

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object entity) {
        if (entity == null) {
            return "";
        }
        return EntityMapBean.getCurrent().makeUuid(entity);
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String uuid) {
        if (uuid == null || uuid.equalsIgnoreCase("")) {
            return null;
        }
        return  EntityMapBean.getCurrent().getObject(uuid);
    }
}
