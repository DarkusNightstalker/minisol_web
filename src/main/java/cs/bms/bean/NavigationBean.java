/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import gkfire.web.bean.INavigationBean;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author CORE i7
 */
@ManagedBean
@SessionScoped
public class NavigationBean implements INavigationBean{
    private String content = "/pages/home.xhtml";
    private String javascriptMenu = "Menu.change('#form-nav-menu\\\\:home',false)";
    private String topMenú ="home-opt";
    
    @Override
    public String getContent() {
        return content;
    }
    
    @Override
    public void setContent(String content) {
        this.content = content;
    }

    
    @Override
    public String getJavascriptMenu() {
        return javascriptMenu;
    }
    
    @Override
    public void setJavascriptMenu(String javascriptMenu) {
        this.javascriptMenu = javascriptMenu;
    }

    /**
     * @return the topMenú
     */
    public String getTopMenú() {
        return topMenú;
    }

    /**
     * @param topMenú the topMenú to set
     */
    public void setTopMenú(String topMenú) {
        this.topMenú = topMenú;
    }
    
}
