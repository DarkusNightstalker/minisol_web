/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.Actor;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.IProductSalePriceService;
import cs.bms.service.interfac.IStockService;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.util.Pagination;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class GenerateSaleBean implements java.io.Serializable {

    @ManagedProperty(value = "#{actorService}")
    private IActorService actorService;
    @ManagedProperty(value = "#{stockService}")
    private IStockService stockService;
    @ManagedProperty(value = "#{productSalePriceService}")
    private IProductSalePriceService productSalePriceService;
    private Integer currentCompany;
    private Actor customer;

    private CustomerSearcher customerSearcher;
    private ProductSearcher productSearcher;

    @PostConstruct
    public void init() {
        customerSearcher = new CustomerSearcher();
        productSearcher = new ProductSearcher();
    }

    public void setProductSearcher(ProductSearcher productSearcher) {
        this.productSearcher = productSearcher;
    }

    public ProductSearcher getProductSearcher() {
        return productSearcher;
    }

    public void setProductSalePriceService(IProductSalePriceService productSalePriceService) {
        this.productSalePriceService = productSalePriceService;
    }

    public IProductSalePriceService getProductSalePriceService() {
        return productSalePriceService;
    }

    public void setCurrentCompany(Integer currentCompany) {
        this.currentCompany = currentCompany;
    }

    public Integer getCurrentCompany() {
        return currentCompany;
    }

    public IStockService getStockService() {
        return stockService;
    }

    public void setStockService(IStockService stockService) {
        this.stockService = stockService;
    }

    public CustomerSearcher getCustomerSearcher() {
        return customerSearcher;
    }

    public void setCustomerSearcher(CustomerSearcher customerSearcher) {
        this.customerSearcher = customerSearcher;
    }

    public Actor getCustomer() {
        return customer;
    }

    public void setCustomer(Actor customer) {
        this.customer = customer;
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

    public class ProductSearcher implements java.io.Serializable {

        private String terms;
        private Pagination<Object[]> pagination;
        private OrderFactory orderFactory;
        private ProjectionList projectionList;
        private AliasList aliasList;

        public ProductSearcher() {
            pagination = new Pagination<>(stockService);
            pagination.setRows(5);
            orderFactory = new OrderFactory(new OrderList());
            orderFactory.setDefaultOrder(Order.asc("p.name"));
            projectionList = Projections.projectionList()
                    .add(Projections.property("p.id"))
                    .add(Projections.property("p.image"))
                    .add(Projections.property("p.name"))
                    .add(Projections.property("uom.id"))
                    .add(Projections.property("uom.abbr"))
                    .add(Projections.property("quantity"))
                    .add(Projections.property("uom.id"));
            aliasList = new AliasList();
            aliasList.add("product", "p");
            aliasList.add("uom", "uom");
            aliasList.add("company", "c");
            terms = "";
        }

        public void search() {
            terms = terms.trim();
            CriterionList criterionList = new CriterionList()
                    ._add(Restrictions.eq("p.active", true))
                    ._add(Restrictions.eq("c.id", currentCompany));
            if (terms.length() != 0) {
                criterionList.add(
                        Restrictions.or(
                                Restrictions.like("p.barcode", terms, MatchMode.START),
                                Restrictions.like("p.name", terms, MatchMode.START).ignoreCase()
                        )
                );
            }
            pagination.search(1, aliasList, criterionList, projectionList, orderFactory.make());
            pagination.getData().stream().forEach((item) -> {
                item[6] = productSalePriceService.getByHQL("SELECT psp.price FROM ProductSalePrice psp WHERE psp.uom.id = ? AND psp.company.id = ? AND psp.product.id = ? ORDER BY psp.quantity", item[3], currentCompany, item[0]);
            });
        }

        /**
         * @return the terms
         */
        public String getTerms() {
            return terms;
        }

        /**
         * @param terms the terms to set
         */
        public void setTerms(String terms) {
            this.terms = terms;
        }

        /**
         * @return the pagination
         */
        public Pagination<Object[]> getPagination() {
            return pagination;
        }

        /**
         * @return the orderFactory
         */
        public OrderFactory getOrderFactory() {
            return orderFactory;
        }

        /**
         * @param pagination the pagination to set
         */
        public void setPagination(Pagination<Object[]> pagination) {
            this.pagination = pagination;
        }

        /**
         * @param orderFactory the orderFactory to set
         */
        public void setOrderFactory(OrderFactory orderFactory) {
            this.orderFactory = orderFactory;
        }

        /**
         * @return the projectionList
         */
        public ProjectionList getProjectionList() {
            return projectionList;
        }

        /**
         * @param projectionList the projectionList to set
         */
        public void setProjectionList(ProjectionList projectionList) {
            this.projectionList = projectionList;
        }

        /**
         * @return the aliasList
         */
        public AliasList getAliasList() {
            return aliasList;
        }

        /**
         * @param aliasList the aliasList to set
         */
        public void setAliasList(AliasList aliasList) {
            this.aliasList = aliasList;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="CustomerSearcher">
    public class CustomerSearcher implements java.io.Serializable {

        private String identityNumber;

        public void searchCustomer() {
            identityNumber = identityNumber.trim();
            customer = (Actor) actorService.getByHQL("FROM Actor a WHERE a.identityNumber LIKE ?", identityNumber);
            if (customer == null) {
                customer = searchInRENIEC();
                if (customer == null) {
                    new PNotifyMessage("Sujeto inexistente", "No existe nadie con ese documento de identidad", PNotifyMessage.Type.Danger, "fa fa-times shaked animated").execute();
                    return;
                }
                customer.setCustomer(Boolean.TRUE);
                actorService.saveOrUpdate(customer);
            } else {
            }
            customer.setCustomer(Boolean.TRUE);
        }

        private Actor searchInRENIEC() {
            return null;
        }

        /**
         * @return the identityNumber
         */
        public String getIdentityNumber() {
            return identityNumber;
        }

        /**
         * @param identityNumber the identityNumber to set
         */
        public void setIdentityNumber(String identityNumber) {
            this.identityNumber = identityNumber;
        }
    }
    //</editor-fold>

}
