/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.enumerated.UoMType;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@ApplicationScoped
public class AppBean implements java.io.Serializable {
    
    private final UoMType[] uomTypes;

    public AppBean() {
        uomTypes = UoMType.values();
    }

    /**
     * @return the uomTypes
     */
    public UoMType[] getUomTypes() {
        return uomTypes;
    }
    
    
}
