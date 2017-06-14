/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.electronicBook;

import gkfire.web.util.BeanUtil;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class Bean131  extends ABookBean {

    @Override
    public void generate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onLoad(boolean allowAjax) {    
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }

    @Override
    public void search() {
    }
    
}
