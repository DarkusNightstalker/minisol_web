/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.enumerated.UoMType;
import cs.bms.model.User;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private List<Object[]> errors;

    public AppBean() {
        uomTypes = UoMType.values();
        errors = new ArrayList();
    }

    public void printErrors(PrintWriter writer) {
        while (!errors.isEmpty()) {
            ((Throwable)getErrors().remove(0)[0]).printStackTrace(writer);
        }
    }

    public String printError(Throwable exception) {
        StringWriter errorWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(errorWriter));
        return errorWriter.toString();
    }

    public String addError(Throwable e,User user) {
        e.printStackTrace();
        errors.add(new Object[]{e,user == null ? null : user.getUsername(),new Date()});
        return String.format("%04d", getErrors().size());
    }

    public String getCode(int index) {
        return String.format("%04d", index);
    }

    public String printErrorStack() {
        StringWriter error = new StringWriter();
        if (!errors.isEmpty()) {
            ((Throwable)getErrors().remove(0)[0]).printStackTrace(new PrintWriter(error));
        }
        return error.toString();
    }

    /**
     * @return the uomTypes
     */
    public UoMType[] getUomTypes() {
        return uomTypes;
    }

    /**
     * @return the errors
     */
    public List<Object[]> getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(List<Object[]> errors) {
        this.errors = errors;
    }

}
