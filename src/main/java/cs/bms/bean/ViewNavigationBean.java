/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import gkfire.web.util.BeanUtil;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author Darkus Nightmare
 */
@ManagedBean
@ViewScoped
public class ViewNavigationBean implements java.io.Serializable {

    private String content = "/pages/home.xhtml";
    private String icon = "/assets/img/icon/default.ico";
    private String javascriptMenu = "Menu.change('#form-nav-menu\\\\:home',false)";
    private String topMenú = "home-opt";

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getJavascriptMenu() {
        return javascriptMenu;
    }

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
    public static ViewNavigationBean getCurrent() {
        return BeanUtil.getCurrentInstance().getApplication().evaluateExpressionGet(BeanUtil.getCurrentInstance(), "#{viewNavigationBean}", ViewNavigationBean.class);
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
}
