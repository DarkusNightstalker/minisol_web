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
    
    @Override
    public String getContent() {
        return ViewNavigationBean.getCurrent().getContent();
    }
    
    @Override
    public void setContent(String content) {
        ViewNavigationBean.getCurrent().setContent(content);
    }

    
    @Override
    public String getJavascriptMenu() {
        return ViewNavigationBean.getCurrent().getJavascriptMenu();
    }
    
    @Override
    public void setJavascriptMenu(String javascriptMenu) {
       ViewNavigationBean.getCurrent().setJavascriptMenu(javascriptMenu);
    }

    /**
     * @return the topMenú
     */
    public String getTopMenú() {
        return ViewNavigationBean.getCurrent().getTopMenú();
    }

    /**
     * @param topMenú the topMenú to set
     */
    public void setTopMenú(String topMenú) {
       ViewNavigationBean.getCurrent().setTopMenú(topMenú);
    }

    @Override
    public String getIcon() {        
        return ViewNavigationBean.getCurrent().getIcon();
    }

    @Override
    public void setIcon(String icon) {
        ViewNavigationBean.getCurrent().setIcon(icon);
    }
    
}
