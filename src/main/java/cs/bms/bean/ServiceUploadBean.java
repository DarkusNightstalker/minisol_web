/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.Actor;
import cs.bms.model.Company;
import cs.bms.model.DataCompany;
import cs.bms.model.IdentityDocument;
import cs.bms.model.User;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IDataCompanyService;
import cs.bms.service.interfac.IDistrictService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.service.interfac.IUserService;
import gkfire.auditory.Auditory;
import gkfire.web.util.BeanUtil;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ServiceUploadBean implements java.io.Serializable {

    @ManagedProperty(value = "#{identityDocumentService}")
    private IIdentityDocumentService identityDocumentService;
    @ManagedProperty(value = "#{actorService}")
    private IActorService actorService;
    @ManagedProperty(value = "#{companyService}")
    private ICompanyService companyService;
    @ManagedProperty(value = "#{districtService}")
    private IDistrictService districtService;
    @ManagedProperty(value = "#{userService}")
    private IUserService userService;
    @ManagedProperty(value = "#{dataCompanyService}")
    private IDataCompanyService dataCompanyService;
    @ManagedProperty(value = "#{sincroDataBean}")
    private SincroDataBean sincroDataBean;

    public void updateData() {

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String jsonData = params.get("data_client");
        JSONArray json = new JSONArray(jsonData);
        IdentityDocument dni = (IdentityDocument) identityDocumentService.getByHQL("FROM IdentityDocument idd WHERE idd.length_ = ?", new Short("8"));
        IdentityDocument ruc = (IdentityDocument) identityDocumentService.getByHQL("FROM IdentityDocument idd WHERE idd.length_ = ?", new Short("11"));
        User user = userService.getById(1);
        Company company = (Company) companyService.getByHQL("FROM Company c WHERE c.code LIKE ?", json.getJSONObject(0).getString("HeadquarterCode"));
        for (int i = 0; i < json.length(); i++) {
            JSONObject customer = json.getJSONObject(i);
            String identityNumber = customer.getString("IdentityNumber");
            Long actorId = (Long) actorService.getByHQL("SELECT a.id FROM Actor a WHERE  a.identityNumber LIKE ?", identityNumber);
            if (actorId == null) {
//                actorService.updateHQL(jsonData)
                Actor actor = new Actor();
                actor.setIdentityDocument(identityNumber.length() == 8 ? dni : ruc);
                actor.setIdentityNumber(identityNumber);
                actor.setName(customer.getString("Name"));
                actor.setAddress(customer.getString("Address"));
                actor.setPoints(customer.getLong("DiscountPoints"));
                Auditory.make(actor, user);
                actorService.saveOrUpdate(actor);
                actorId = actor.getId();
            }

            Long dataCompanyId = (Long) dataCompanyService.getByHQL("SELECT dc.id FROM DataCompany dc WHERE dc.customer.id = ? AND dc.company = ?", actorId, company);
            if (dataCompanyId == null) {
                DataCompany data = new DataCompany();
                data.setCode(customer.getString("Code"));
                data.setCustomer(new Actor(actorId));
                data.setPoints(customer.getLong("DiscountPoints"));
                data.setCompany(company);
                dataCompanyService.saveOrUpdate(data);

                Long currentPointsActor = (Long) actorService.getByHQL("SELECT a.points FROM Actor a WHERE a.id = ?", actorId);
                currentPointsActor += data.getPoints();
                try {
                    actorService.updateHQL("UPDATE Actor SET points = ? WHERE id = ?", currentPointsActor, actorId);
                } catch (Exception ex) {
                    Logger.getLogger(ServiceUploadBean.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
                
            } else {
                Long currentPointsData = (Long) actorService.getByHQL("SELECT a.points FROM DataCompany a WHERE a.id = ?", dataCompanyId);
                Long currentPointsActor = (Long) actorService.getByHQL("SELECT a.points FROM Actor a WHERE a.id = ?", actorId);

                Long difference = customer.getLong("DiscountPoints") - currentPointsData;
                currentPointsActor += difference;
                try {
                    actorService.updateHQL("UPDATE Actor SET points = ? WHERE id = ?", currentPointsActor, actorId);
                } catch (Exception ex) {
                    Logger.getLogger(ServiceUploadBean.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            }
        }
//        sincroDataBean.addActiveSession();
        while (true) {
//            if (sincroDataBean.allowedContinueOperating()) {
//                break;
//            }
            if (true) {
                break;
            }
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServiceUploadBean.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        }
        try {
            dataCompanyService.updateHQL("UPDATE DataCompany dc SET dc.points = dc.customer.points WHERE dc.company = ?", company);
            company.setLastDataUpdate(new Date());
            Auditory.make(company, user);

            companyService.saveOrUpdate(company);
            JSONArray jsonResponse = new JSONArray();
            List<Object[]> dataList = dataCompanyService.listHQL("SELECT dc.code,dc.customer.identityNumber,dc.points FROM DataCompany dc WHERE dc.company = ?", company);
            for (Object[] item : dataList) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("code", item[0]);
                jsonItem.put("identityNumber", item[1]);
                jsonItem.put("points", item[2]);
                jsonResponse.put(jsonItem);
            }
            HttpServletResponse response = BeanUtil.getResponse();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());
            response.getWriter().flush();
//            sincroDataBean.addCompleteSession();
            System.out.println(jsonResponse);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception ex) {
            Logger.getLogger(ServiceUploadBean.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @return the identityDocumentService
     */
    public IIdentityDocumentService getIdentityDocumentService() {
        return identityDocumentService;
    }

    /**
     * @param identityDocumentService the identityDocumentService to set
     */
    public void setIdentityDocumentService(IIdentityDocumentService identityDocumentService) {
        this.identityDocumentService = identityDocumentService;
    }

    /**
     * @return the actorService
     */
    public IActorService getActorService() {
        return actorService;
    }

    /**
     * @param actorService the actorService to set
     */
    public void setActorService(IActorService actorService) {
        this.actorService = actorService;
    }

    /**
     * @return the companyService
     */
    public ICompanyService getCompanyService() {
        return companyService;
    }

    /**
     * @param companyService the companyService to set
     */
    public void setCompanyService(ICompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * @return the districtService
     */
    public IDistrictService getDistrictService() {
        return districtService;
    }

    /**
     * @param districtService the districtService to set
     */
    public void setDistrictService(IDistrictService districtService) {
        this.districtService = districtService;
    }

    /**
     * @return the userService
     */
    public IUserService getUserService() {
        return userService;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }

    /**
     * @return the dataCompanyService
     */
    public IDataCompanyService getDataCompanyService() {
        return dataCompanyService;
    }

    /**
     * @param dataCompanyService the dataCompanyService to set
     */
    public void setDataCompanyService(IDataCompanyService dataCompanyService) {
        this.dataCompanyService = dataCompanyService;
    }

    /**
     * @return the sincroDataBean
     */
    public SincroDataBean getSincroDataBean() {
        return sincroDataBean;
    }

    /**
     * @param sincroDataBean the sincroDataBean to set
     */
    public void setSincroDataBean(SincroDataBean sincroDataBean) {
        this.sincroDataBean = sincroDataBean;
    }
}
