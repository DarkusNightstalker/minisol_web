/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import gkfire.web.util.BeanUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author Darkus Nightmare
 */
@ManagedBean
@ViewScoped
public class EntityMapBean implements java.io.Serializable {

    private final Map<Object, String> entities;

    public EntityMapBean() {
        System.out.println("Creando MapaEntidad");
        entities = new HashMap();
    }

    public synchronized String makeUuid(Object o) {
        if (!entities.containsKey(o)) {
            String uuid = UUID.randomUUID().toString();
            entities.put(o, uuid);
            System.out.println(entities.size());
            return uuid;
        } else {
            return entities.get(o);
        }
    }

    public Object getObject(String uuid) {
        for (Map.Entry<Object, String> entry : entities.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    @PreDestroy
    public void destroy(){
        entities.clear();
        System.gc();
    }

    public static EntityMapBean getCurrent() {
        EntityMapBean result = BeanUtil.getCurrentInstance().getApplication().evaluateExpressionGet(BeanUtil.getCurrentInstance(), "#{entityMapBean}", EntityMapBean.class);
        return result;
    }
}
