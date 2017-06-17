/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.Company;
import cs.bms.model.User;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IDocumentNumberingService;
import cs.bms.service.interfac.IPermissionService;
import cs.bms.service.interfac.ISpecialPermissionService;
import cs.bms.service.interfac.IUserService;
import gkfire.web.bean.AbstractSessionBean;
import gkfire.web.util.AbstractImport;
import gkfire.web.util.BeanUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Darkus
 */
@ManagedBean
@SessionScoped
public class SessionBean extends AbstractSessionBean<User> {

    @ManagedProperty(value = "#{userService}")
    private IUserService userService;
    @ManagedProperty(value = "#{permissionService}")
    private IPermissionService permissionService;
    @ManagedProperty(value = "#{companyService}")
    private ICompanyService companyService;
    @ManagedProperty(value = "#{documentNumberingService}")
    private IDocumentNumberingService documentNumberingService;
    @ManagedProperty(value = "#{specialPermissionService}")
    private ISpecialPermissionService specialPermissionService;
    @ManagedProperty(value = "#{appBean}")
    private AppBean appBean;
    private Authentication authentication;
    private AbstractImport import_;
    private String topLeftName;
    private String avatarURL;
    private boolean superAdmin;
    private Company currentCompany;
    private CompanySearcher companySearcher;
    private List<Object[]> documentNumberings;

    @Override
    public void onLoad() {
        try {
            loadable.onLoad(false);
        } catch (NullPointerException e) {
        }
        if (BeanUtil.isAjaxRequest()) {
            return;
        }
        loadPermissions();
        getCompanySearcher().update();
    }

    @Override
    public boolean authorize(String codesJoin) {
        if (currentUser.getSuperUser()) {
            return true;
        } else {
            return super.authorize(codesJoin); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @PostConstruct
    public void init() {
        setCompanySearcher(new CompanySearcher());
        setSuperAdmin(false);
        setAuthentication(new Authentication());
    }
    
    @Override
    public void printErrors(PrintWriter writer) {
        appBean.printErrors(writer);
    }

    @Override
    public String addError(Throwable e) {
        return appBean.addError(e,currentUser);
    }

    @Override
    public String printErrorStack() {
        return appBean.printErrorStack();
    }

    public void setCurrentCompanyById(Integer id) {
        setCurrentCompany(getCompanyService().getById(id));
        setDocumentNumberings(getDocumentNumberingService().getDataByUser(currentUser, getCurrentCompany().getRuc()));
    }

    @Override
    protected void loadPermissions() {
        permissions = getPermissionService().getPermissionCodeFromUser(currentUser);
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        try {
//           this.setCurrentCompany(currentUser.getInvolved().getDependency());
            this.setTopLeftName(currentUser.getUsername());
            this.setSuperAdmin(false);
            loadPermissions();
            ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getSession().setAttribute("user", currentUser);
        } catch (NullPointerException e) {
            if (currentUser != null) {
                this.setSuperAdmin(true);
                this.setTopLeftName(currentUser.getUsername());
                setAvatarURL("/assets/img/avatars/no-image.png");
                permissions = Collections.EMPTY_LIST;
                ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getSession().setAttribute("user", currentUser);
            }
        }
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    /**
     * @return the topLeftName
     */
    public String getTopLeftName() {
        return topLeftName;
    }

    /**
     * @param topLeftName the topLeftName to set
     */
    public void setTopLeftName(String topLeftName) {
        this.topLeftName = topLeftName;
    }

    /**
     * @return the avatarURL
     */
    public String getAvatarURL() {
        return avatarURL;
    }

    /**
     * @param avatarURL the avatarURL to set
     */
    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
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
     * @return the permissionService
     */
    public IPermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the currentCompany
     */
    public Company getCurrentCompany() {
        return currentCompany;
    }

    /**
     * @param currentCompany the currentCompany to set
     */
    public void setCurrentCompany(Company currentCompany) {
        this.currentCompany = currentCompany;
    }

    /**
     * @return the companySearcher
     */
    public CompanySearcher getCompanySearcher() {
        return companySearcher;
    }

    /**
     * @param companySearcher the companySearcher to set
     */
    public void setCompanySearcher(CompanySearcher companySearcher) {
        this.companySearcher = companySearcher;
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
     * @return the import_
     */
    public AbstractImport getImport_() {
        return import_;
    }

    /**
     * @param import_ the import_ to set
     */
    public void setImport_(AbstractImport import_) {
        this.import_ = import_;
    }

    /**
     * @return the appBean
     */
    public AppBean getAppBean() {
        return appBean;
    }

    /**
     * @param appBean the appBean to set
     */
    public void setAppBean(AppBean appBean) {
        this.appBean = appBean;
    }

    // <editor-fold defaultstate="collapsed" desc="Authentication Class">    
    public final class Authentication implements java.io.Serializable {

        private String username;
        private String password;
        private boolean remember;

        private boolean validLogin;
        private String messageLogin;
        private String titleLogin;

        Authentication() {
            refresh();
            remember();
        }

        private void remember() {
            FacesContext fc = FacesContext.getCurrentInstance();
            Cookie[] cookies = ((HttpServletRequest) (fc.getExternalContext().getRequest())).getCookies();
            if (cookies != null && cookies.length > 0) {
                for (int i = 0; i < cookies.length; i++) {
                    String cookieName = cookies[i].getName();
                    String cookieValue = cookies[i].getValue();
                    if (cookieName.equals("LionHeart_DEV_Y29udGFiaWxpZGFk_USR")) {
                        username = cookieValue;
                    } else if (cookieName.equals("LionHeart_DEV_Y29udGFiaWxpZGFk_PWD")) {
                        password = cookieValue;
                    } else if (cookieName.equals("LionHeart_DEV_Y29udGFiaWxpZGFk_CHK")) {
                        remember = "true".equals(cookieValue);
                        if (!remember) {
                            username = "";
                            password = "";
                        } else if (remember) {
                            System.out.println("Here in doLogin() line 99");
                        }
                    }
                }
            }
        }

        private void removeCookies() {
            FacesContext fc = FacesContext.getCurrentInstance();
            Cookie cookieUsername = new Cookie("LionHeart_DEV_Y29udGFiaWxpZGFk_VVNS", "");
            Cookie cookiePassword = new Cookie("LionHeart_DEV_Y29udGFiaWxpZGFk_UFdE", "");
            Cookie cookieRemember = new Cookie("LionHeart_DEV_Y29udGFiaWxpZGFk_Q0hL", "");
            cookieUsername.setMaxAge(0);
            cookiePassword.setMaxAge(0);
            cookieRemember.setMaxAge(0);
            cookieUsername.setPath("/");
            cookiePassword.setPath("/");
            cookieRemember.setPath("/");
            BeanUtil.getResponse().addCookie(cookieUsername);
            BeanUtil.getResponse().addCookie(cookiePassword);
            BeanUtil.getResponse().addCookie(cookieRemember);
        }

        void refresh() {
            validLogin = true;
            remember = false;
        }

        public String login() {
            validLogin = true;
            try {
                String username = this.username.trim();
                String password = this.password.trim();
                User currentUser = getUserService().login(username, password);
                if (currentUser == null) {
                    throw new IndexOutOfBoundsException();
                }
                SessionBean.this.setCurrentUser(currentUser);
                if (remember) {
                    Cookie cookieUsername = new Cookie("LionHeart_DEV_Y29udGFiaWxpZGFk_VVNS", username);
                    Cookie cookiePassword = new Cookie("LionHeart_DEV_Y29udGFiaWxpZGFk_UFdE", password);
                    Cookie cookieRemember = new Cookie("LionHeart_DEV_Y29udGFiaWxpZGFk_Q0hL", remember ? "true" : "false");
                    cookieUsername.setMaxAge(3600);
                    cookiePassword.setMaxAge(3600);
                    cookieRemember.setMaxAge(3600);
                    BeanUtil.getResponse().addCookie(cookieUsername);
                    BeanUtil.getResponse().addCookie(cookiePassword);
                    BeanUtil.getResponse().addCookie(cookieRemember);
                } else {
                    removeCookies();
                    Cookie cookieRemember = new Cookie("LionHeart_DEV_Y29udGFiaWxpZGFk_Q0hL", remember ? "true" : "false");
                    cookieRemember.setMaxAge(3600);
                    BeanUtil.getResponse().addCookie(cookieRemember);
                }
                return "index?faces-redirect=true";
            } catch (IndexOutOfBoundsException e) {
                validLogin = false;

                getMessages().add(new PNotifyMessage("Error de Autenticacion", "La combinacion usuario/contraseña no es correcta", PNotifyMessage.Type.Danger, "fa fa-warning shake animated"));

                return "login?faces-redirect=true";
            } catch (Exception e) {
                e.printStackTrace();
                validLogin = false;
                getMessages().add(new PNotifyMessage(e.getClass().getName(), e.getMessage(), PNotifyMessage.Type.Danger, "fa fa-warning shake animated"));

//                messageLogin = messageLogin.replace("\n", " \\n ");
                return "login?faces-redirect=true";
            }
        }

        public String logout() {
            removeCookies();
            HttpSession session = gkfire.web.util.BeanUtil.getSession();
            session.invalidate();
            refresh();
            return "login?faces-redirect=true";
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isValidLogin() {
            return validLogin;
        }

        public void setValidLogin(boolean validLogin) {
            this.validLogin = validLogin;
        }

        public String getMessageLogin() {
            return messageLogin;
        }

        public void setMessageLogin(String messageLogin) {
            this.messageLogin = messageLogin;
        }

        /**
         * @return the titleLogin
         */
        public String getTitleLogin() {
            return titleLogin;
        }

        /**
         * @param titleLogin the titleLogin to set
         */
        public void setTitleLogin(String titleLogin) {
            this.titleLogin = titleLogin;
        }

        /**
         * @return the remember
         */
        public boolean isRemember() {
            return remember;
        }

        /**
         * @param remember the remember to set
         */
        public void setRemember(boolean remember) {
            this.remember = remember;
        }
    }

    //</editor-fold>
    public class CompanySearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            if (getCurrentUser().getSuperUser()) {
                data = getCompanyService().getBasicData();
            } else {
                List list = getSpecialPermissionService().listHQL("SELECT sp.identifier FROM SpecialPermission sp join sp.users u WHERE u.id = ? AND sp.entityName LIKE ?", getCurrentUser().getId(), Company.class.getSimpleName());
                data = getCompanyService().getDataByList(list);
            }
            if (data.size() == 1) {
                setCurrentCompanyById((Integer) data.get(0)[0]);
            }
        }

        public List<Object[]> getData() {
            return data;
        }
    }

    /**
     * @return the documentNumberingService
     */
    public IDocumentNumberingService getDocumentNumberingService() {
        return documentNumberingService;
    }

    /**
     * @param documentNumberingService the documentNumberingService to set
     */
    public void setDocumentNumberingService(IDocumentNumberingService documentNumberingService) {
        this.documentNumberingService = documentNumberingService;
    }

    /**
     * @return the documentNumberings
     */
    public List<Object[]> getDocumentNumberings() {
        return documentNumberings;
    }

    /**
     * @param documentNumberings the documentNumberings to set
     */
    public void setDocumentNumberings(List<Object[]> documentNumberings) {
        this.documentNumberings = documentNumberings;
    }

    /**
     * @return the specialPermissionService
     */
    public ISpecialPermissionService getSpecialPermissionService() {
        return specialPermissionService;
    }

    /**
     * @param specialPermissionService the specialPermissionService to set
     */
    public void setSpecialPermissionService(ISpecialPermissionService specialPermissionService) {
        this.specialPermissionService = specialPermissionService;
    }
}
