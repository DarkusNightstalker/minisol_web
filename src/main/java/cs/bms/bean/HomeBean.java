/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import gkfire.web.bean.ILoadable;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class HomeBean implements ILoadable, Serializable {

    @Override
    public void onLoad(boolean allowAjax) {
    }
//
//    @ManagedProperty(value = "#{classifierService}")
//    private IClassifierService classifierService;
//    @ManagedProperty(value = "#{fundingSourceService}")
//    private IFundingSourceService fundingSourceService;
//    @ManagedProperty(value = "#{budgetCeilingService}")
//    private IBudgetCeilingService budgetCeilingService;
//    @ManagedProperty(value = "#{bneItemService}")
//    private IBNeItemService bneItemService;
//    @ManagedProperty(value = "#{sessionBean}")
//    private SessionBean sessionBean;
//
//    private List<FundingSource> fundingSources;
//    private List<Classifier> classifiers;
//    private Classifier selectedClassifier;
//
//    private Ceiling ceiling;
//    private Expected expected;
//
//    @PostConstruct
//    public void init() {
//        ceiling = new Ceiling();
//        expected = new Expected();
//    }
//
//    @Override
//    public void onLoad(boolean allowAjax) {
//
//        if (BeanUtil.isAjaxRequest() && !allowAjax) {
//            return;
//        }
//        if (!sessionBean.isSuperAdmin()) {
//            AliasList aliasList = new AliasList();
//            OrderList orderList = new OrderList();
//            orderList.add(Order.asc("code"));
//            fundingSources = getFundingSourceService().addRestrictionsVariant(orderList, aliasList);
//
//            aliasList = new AliasList();
//            aliasList.add("classifierType", "ct");
//            orderList = new OrderList();
//            orderList.add(Order.asc("path"));
//            CriterionList criterionList = new CriterionList()
//                    ._add(Restrictions.eq("ct.codeDigit", new Short("2")))
//                    ._add(Restrictions.eq("active", true));
//            classifiers = getClassifierService().addRestrictionsVariant(aliasList, criterionList, orderList);
//            setSelectedClassifier(classifiers.get(0));
//            ceiling.refresh();
//            expected.refresh();
//        }
//    }
//
//    /**
//     * @return the classifierService
//     */
//    public IClassifierService getClassifierService() {
//        return classifierService;
//    }
//
//    /**
//     * @param classifierService the classifierService to set
//     */
//    public void setClassifierService(IClassifierService classifierService) {
//        this.classifierService = classifierService;
//    }
//
//    /**
//     * @return the fundingSourceService
//     */
//    public IFundingSourceService getFundingSourceService() {
//        return fundingSourceService;
//    }
//
//    /**
//     * @param fundingSourceService the fundingSourceService to set
//     */
//    public void setFundingSourceService(IFundingSourceService fundingSourceService) {
//        this.fundingSourceService = fundingSourceService;
//    }
//
//    /**
//     * @return the budgetCeilingService
//     */
//    public IBudgetCeilingService getBudgetCeilingService() {
//        return budgetCeilingService;
//    }
//
//    /**
//     * @param budgetCeilingService the budgetCeilingService to set
//     */
//    public void setBudgetCeilingService(IBudgetCeilingService budgetCeilingService) {
//        this.budgetCeilingService = budgetCeilingService;
//    }
//
//    /**
//     * @return the sessionBean
//     */
//    public SessionBean getSessionBean() {
//        return sessionBean;
//    }
//
//    /**
//     * @param sessionBean the sessionBean to set
//     */
//    public void setSessionBean(SessionBean sessionBean) {
//        this.sessionBean = sessionBean;
//    }
//
//    /**
//     * @return the selectedClassifier
//     */
//    public Classifier getSelectedClassifier() {
//        return selectedClassifier;
//    }
//
//    /**
//     * @param selectedClassifier the selectedClassifier to set
//     */
//    public void setSelectedClassifier(Classifier selectedClassifier) {
//        this.selectedClassifier = selectedClassifier;
//    }
//
//    /**
//     * @return the bneItemService
//     */
//    public IBNeItemService getBneItemService() {
//        return bneItemService;
//    }
//
//    /**
//     * @param bneItemService the bneItemService to set
//     */
//    public void setBneItemService(IBNeItemService bneItemService) {
//        this.bneItemService = bneItemService;
//    }
//
//    /**
//     * @return the fundingSources
//     */
//    public List<FundingSource> getFundingSources() {
//        return fundingSources;
//    }
//
//    /**
//     * @param fundingSources the fundingSources to set
//     */
//    public void setFundingSources(List<FundingSource> fundingSources) {
//        this.fundingSources = fundingSources;
//    }
//
//    /**
//     * @return the classifiers
//     */
//    public List<Classifier> getClassifiers() {
//        return classifiers;
//    }
//
//    /**
//     * @param classifiers the classifiers to set
//     */
//    public void setClassifiers(List<Classifier> classifiers) {
//        this.classifiers = classifiers;
//    }
//
//    /**
//     * @return the ceiling
//     */
//    public Ceiling getCeiling() {
//        return ceiling;
//    }
//
//    /**
//     * @param ceiling the ceiling to set
//     */
//    public void setCeiling(Ceiling ceiling) {
//        this.ceiling = ceiling;
//    }
//
//    /**
//     * @return the expected
//     */
//    public Expected getExpected() {
//        return expected;
//    }
//
//    /**
//     * @param expected the expected to set
//     */
//    public void setExpected(Expected expected) {
//        this.expected = expected;
//    }
//
//    public class Ceiling implements java.io.Serializable {
//
//        private Map<Long, List<BudgetCeiling>> budgetCeilings;
//        private Map<Long, Double> budgetCeilingsTotal;
//
//        public void refresh() {
//
//            Dependency current = getSessionBean().getCurrentDependency();
//            budgetCeilings = new HashMap();
//            budgetCeilingsTotal = new HashMap();
//            for (Classifier classifier : getClassifiers()) {
//                double total = 0.0;
//                CriterionList criterionList = new CriterionList();
//                criterionList.add(Restrictions.eq("dependency", current));
//                criterionList.add(Restrictions.eq("classifier", classifier));
//                criterionList.add(Restrictions.eq("year", getSessionBean().getOperationYear()));
//                List<BudgetCeiling> data = getBudgetCeilingService().addRestrictions(criterionList);
//                for (BudgetCeiling item : data) {
//                    total += item.getQuantity();
//                }
//                budgetCeilingsTotal.put(classifier.getId(), total);
//                budgetCeilings.put(classifier.getId(), data);
//            }
//
//        }
//
//        public BudgetCeiling getBy(FundingSource fs) {
//
//            for (BudgetCeiling item : budgetCeilings.get(selectedClassifier.getId())) {
//                if (fs.getId() == item.getFundingSource().getId().longValue()) {
//                    return item;
//                }
//            }
//            return null;
//        }
//
//        /**
//         * @return the budgetCeilings
//         */
//        public Map<Long, List<BudgetCeiling>> getBudgetCeilings() {
//            return budgetCeilings;
//        }
//
//        /**
//         * @param budgetCeilings the budgetCeilings to set
//         */
//        public void setBudgetCeilings(Map<Long, List<BudgetCeiling>> budgetCeilings) {
//            this.budgetCeilings = budgetCeilings;
//        }
//
//        /**
//         * @return the budgetCeilingsTotal
//         */
//        public Map<Long, Double> getBudgetCeilingsTotal() {
//            return budgetCeilingsTotal;
//        }
//
//        /**
//         * @param budgetCeilingsTotal the budgetCeilingsTotal to set
//         */
//        public void setBudgetCeilingsTotal(Map<Long, Double> budgetCeilingsTotal) {
//            this.budgetCeilingsTotal = budgetCeilingsTotal;
//        }
//    }
//
//    public class Expected implements Serializable {
//
//        private Map<Long, List<Object[]>> budgetExpected;
//
//        public void refresh() {
//            budgetExpected = new HashMap();
//            AliasList aliasList = new AliasList();
//            aliasList.add("bneSchedules", "bs");
//            aliasList.add("poiActivity", "poia");
//            aliasList.add("classifier", "c");
//            aliasList.add("poia.poi", "poi");
//            aliasList.add("fundingSource", "fs");
//
//            ProjectionList projectionList = Projections.projectionList()
//                    .add(Projections.sum("bs.quantity"));
//            for (Classifier classifier : getClassifiers()) {
//                List<Object[]> temp = new ArrayList();
//                for (FundingSource fundingSource : fundingSources) {
//                    CriterionList criterionList = new CriterionList()
//                            ._add(Restrictions.eq("poi.year", getSessionBean().getOperationYear()))
//                            ._add(Restrictions.eq("poi.dependency", getSessionBean().getCurrentDependency()))
//                            ._add(Restrictions.like("c.path", classifier.getPath(), MatchMode.START))
//                            ._add(Restrictions.eq("fundingSource", fundingSource));
//                    Object result = getBneItemService().addRestrictionsVariant(aliasList, projectionList, criterionList).get(0);
//                    if (result == null) {
//                        result = 0.0;
//                    }
//                    temp.add(new Object[]{fundingSource.getId(), result});
//                }
//
//                budgetExpected.put(classifier.getId(), temp);
//            }
//        }
//
//        public Object[] getBy(FundingSource fs) {
//
//            for (Object[] item : budgetExpected.get(selectedClassifier.getId())) {
//                if (Objects.equals(fs.getId().intValue(), item[0])) {
//                    return item;
//                }
//            }
//            return null;
//        }
//
//        /**
//         * @return the budgetExpected
//         */
//        public Map<Long, List<Object[]>> getBudgetExpected() {
//            return budgetExpected;
//        }
//
//        /**
//         * @param budgetExpected the budgetExpected to set
//         */
//        public void setBudgetExpected(Map<Long, List<Object[]>> budgetExpected) {
//            this.budgetExpected = budgetExpected;
//        }
//
//    }
}
