package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.ScepOrderStatus;
import de.trustable.ca3s.core.service.dto.ScepOrderView;
import de.trustable.ca3s.core.service.dto.Selector;
import de.trustable.ca3s.core.service.util.ScepOrderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.util.*;

import static de.trustable.ca3s.core.repository.SpecificationsHelper.*;


public final class ScepOrderSpecifications {

    static Logger logger = LoggerFactory.getLogger(ScepOrderSpecifications.class);

    static final String SORT = "sort";
    static final String ORDER = "order";

    private ScepOrderSpecifications() {
    }


    public static Page<ScepOrderView> handleQueryParamsScepOrderView(EntityManager entityManager,
                                                                     ScepOrderRepository acmeOrderRepository,
                                                                     ScepOrderUtil acmeOrderUtil,
                                                                     CriteriaBuilder cb,
                                                                     Map<String, String[]> parameterMap,
                                                                     List<String> additionalSelectionAttributes) {

        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<ScepOrder> root = query.from(ScepOrder.class);

        String sortCol = getStringValue(parameterMap.get("sort"), "id").trim();
        Selection<?> orderSelection = null;
        String orderDirection = getStringValue(parameterMap.get("order"), "asc");

        int pageOffset = getIntValue(parameterMap.get("offset"), 0);
        int pagesize = getIntValue(parameterMap.get("limit"), 20);

        ArrayList<Selection<?>> selectionList = new ArrayList<>();
        ArrayList<String> colList = new ArrayList<>();

        Map<String, List<SelectionData>> selectionMap = getSelectionMap(parameterMap);

        // retrieve all the required columns
        // 'filter' is a bit misleading, here...
        String[] columnArr = new String[0];
        if (parameterMap.containsKey("filter")) {
            String[] paramArr = parameterMap.get("filter");
            if (paramArr.length > 0) {
                columnArr = paramArr[0].split(",");
            }
        }


        // collect all selectors in a list
        List<Predicate> predList = new ArrayList<>();

        // walk thru all requested columns
        for (String col : columnArr) {
            colList.add(col);

            if (selectionMap.containsKey(col)) {
                List<SelectionData> selDataList = selectionMap.get(col);
                for (SelectionData selDataItem : selDataList) {
                    logger.debug("buildPredicate for '{}', selector '{}', value '{}' ", col, selDataItem.selector, selDataItem.value);

                    predList.add(buildPredicate(root,
                        cb,
                        query,
                        col,
                        selDataItem.selector,
                        selDataItem.value,
                        selectionList,
                        additionalSelectionAttributes));
                }
            } else {
                logger.debug("buildPredicate for '{}' without selector ", col);
                predList.add(buildPredicate(root,
                    cb,
                    query,
                    col,
                    null,
                    "",
                    selectionList,
                    additionalSelectionAttributes));
            }


            // if this is the sorting columns, save the selection
            if (col.equals(sortCol)) {
                orderSelection = selectionList.get(selectionList.size() - 1);
            }
        }

        // chain all the conditions together
        Predicate pred = null;
        for (Predicate predPart : predList) {
            // chain all the predicates
            if (pred == null) {
                pred = predPart;
            } else {
                pred = cb.and(pred, predPart);
            }
        }

        query.where(pred);

        Sort.Direction sortDir = Sort.Direction.ASC;

        // care for the ordering
        if ("asc".equalsIgnoreCase(orderDirection)) {
            query.orderBy(cb.asc((Expression<?>) orderSelection));
        } else {
            query.orderBy(cb.desc((Expression<?>) orderSelection));
            sortDir = Sort.Direction.DESC;
        }

        query.multiselect(selectionList);

        TypedQuery<Object[]> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(pagesize);
        typedQuery.setFirstResult(pageOffset);

        try {
            logger.debug("assembled query: " + typedQuery.unwrap(org.hibernate.query.Query.class).getQueryString());
        } catch (Exception e) {
            logger.debug("failed in retrieve sql query", e);
        }

        // submit the query
        List<Object[]> listResponse = typedQuery.getResultList();

        // use the result set to fill the response object
        List<ScepOrderView> acmeOrderViewList = new ArrayList<>();
        for (Object[] objArr : listResponse) {

            if (logger.isDebugEnabled() && (objArr.length != colList.size())) {
                logger.debug("objArr len {}, colList len {}", objArr.length, colList.size());
            }

            ScepOrderView acmeOrderView =  buildScepOrderViewFromObjArr(colList, objArr, acmeOrderRepository, acmeOrderUtil);
            acmeOrderViewList.add(acmeOrderView);
        }

        // start again to retrieve the row count
        Pageable pageable = PageRequest.of(pageOffset / pagesize, pagesize, sortDir, sortCol);

        Long nTotalElements;

        CriteriaQuery<Long> queryCount = cb.createQuery(Long.class);
        Root<ScepOrder> iRoot = queryCount.from(ScepOrder.class);

        List<Predicate> predCountList = new ArrayList<>();

        ArrayList<Selection<?>> selectionListCount = new ArrayList<>();

        // walk thru all requested columns
        for (String col : columnArr) {
            colList.add(col);

            if (selectionMap.containsKey(col)) {
                List<SelectionData> selDataList = selectionMap.get(col);
                for (SelectionData selDataItem : selDataList) {
                    logger.debug("buildPredicate for '{}', selector '{}', value '{}' ", col, selDataItem.selector, selDataItem.value);

                    predCountList.add(buildPredicate(iRoot,
                        cb,
                        queryCount,
                        col,
                        selDataItem.selector,
                        selDataItem.value,
                        selectionListCount,
                        additionalSelectionAttributes));
                }
            } else {
                logger.debug("buildPredicate for '{}' without selector ", col);
                predCountList.add(buildPredicate(iRoot,
                    cb,
                    queryCount,
                    col,
                    null,
                    "",
                    selectionListCount,
                    additionalSelectionAttributes));
            }

        }

        Predicate predCount = null;

        // chain all the conditions together
        for (Predicate predPart : predCountList) {
            // chain all the predicates
            if (predCount == null) {
                predCount = predPart;
            } else {
                predCount = cb.and(predCount, predPart);
            }
        }

        queryCount.select(cb.count(iRoot));

        queryCount.where(predCount);

        nTotalElements = entityManager.createQuery(queryCount).getSingleResult();
        logger.debug("buildPredicate selects {} elements ", nTotalElements);

        return new PageImpl<>(acmeOrderViewList, pageable, nTotalElements);

    }

    private static ScepOrderView buildScepOrderViewFromObjArr(ArrayList<String> colList,
                                                               Object[] objArr,
                                                              ScepOrderRepository scepOrderRepository,
                                                              ScepOrderUtil acmeOrderUtil) {
        ScepOrderView scepOrderView = new ScepOrderView();
        int i = 0;

        for( int n = 0; n < colList.size(); n++){
            if( "id".equalsIgnoreCase(colList.get(n))){
                Optional<ScepOrder> optionalScepOrder = scepOrderRepository.findById((Long) objArr[i]);
                if( optionalScepOrder.isPresent()){
                    scepOrderView = acmeOrderUtil.from(optionalScepOrder.get());
                }
            }
        }

        for (String attribute : colList) {

            if (i >= objArr.length) {
                logger.debug("attribute '{}' exceeds objArr with #{} elements ", attribute, objArr.length);
                continue;
            }
            logger.debug("attribute '{}' has value '{}'", attribute, objArr[i]);

            if ("id".equalsIgnoreCase(attribute)) {
                scepOrderView.setId((Long) objArr[i]);
            } else if ("status".equalsIgnoreCase(attribute)) {
                scepOrderView.setStatus((ScepOrderStatus) objArr[i]);
            } else if ("transId".equalsIgnoreCase(attribute)) {
                scepOrderView.setTransId((String) objArr[i]);
            } else if ("realm".equalsIgnoreCase(attribute)) {
                scepOrderView.setRealm((String) objArr[i]);
            } else if ("pipelineName".equalsIgnoreCase(attribute)) {
                scepOrderView.setPipelineName((String) objArr[i]);
            } else if ("requestedOn".equalsIgnoreCase(attribute)) {
                scepOrderView.setRequestedOn((Instant) objArr[i]);
            } else if ("requestedBy".equalsIgnoreCase(attribute)) {
                scepOrderView.setRequestedBy((String) objArr[i]);
            } else if ("subject".equalsIgnoreCase(attribute)) {
                scepOrderView.setSubject((String) objArr[i]);
            } else if ("sans".equalsIgnoreCase(attribute)) {
                scepOrderView.setSans((String) objArr[i]);
            } else {
                logger.warn("unexpected attribute '{}' from query", attribute);
            }
            i++;
        }
        return scepOrderView;
    }


    /**
     * Parse the set of selection columns and put them into a map
     *
     * @param parameterMap
     * @return
     */
    static Map<String, List<SelectionData>> getSelectionMap(Map<String, String[]> parameterMap) {

        Map<String, List<SelectionData>> selectorMap = new HashMap<>();

        for (int n = 1; n < 20; n++) {
            String paramNameAttribute = "attributeName_" + n;
            logger.debug("paramNameAttribute {} ", paramNameAttribute);

            if (parameterMap.containsKey(paramNameAttribute)) {
                String attribute = getStringValue(parameterMap.get(paramNameAttribute));
                if (attribute.length() == 0) {
                    logger.debug("paramNameAttribute {} has no value", paramNameAttribute);
                    continue;
                }
                String paramNameAttributeSelector = "attributeSelector_" + n;
                String attributeSelector = getStringValue(parameterMap.get(paramNameAttributeSelector));
                if (attributeSelector.length() == 0) {
                    logger.debug("paramNameAttributeSelector {} has no value", paramNameAttributeSelector);
                    continue;
                }

                String paramNameAttributeValue = "attributeValue_" + n;
                String attributeValue = getStringValue(parameterMap.get(paramNameAttributeValue));
                if (attributeValue.length() == 0) {
                    if (Selector.requiresValue(attributeSelector)) {
                        logger.debug("paramNameAttributeValue {} has no value", paramNameAttributeValue);
                        continue;
                    }
                }

                logger.debug("Attribute {} selecting by {} for value {}", attribute, attributeSelector, attributeValue);

                SelectionData selData = new SelectionData(attributeSelector, attributeValue);
                if (selectorMap.containsKey(attribute)) {
                    logger.debug("adding selector to exiting list for '{}'", attribute);
                    selectorMap.get(attribute).add(selData);
                } else {
                    logger.debug("creating new selector list for '{}'", attribute);
                    List<SelectionData> selectorList = new ArrayList<>();
                    selectorList.add(selData);
                    selectorMap.put(attribute, selectorList);
                }
            } else {
                break;
            }
        }
        return selectorMap;
    }

    /**
     * @param root
     * @param cb
     * @param attribute
     * @param attributeSelector
     * @param attributeValue
     * @param selectionList
     * @return
     */
    private static Predicate buildPredicate(
        Root<ScepOrder> root,
        CriteriaBuilder cb,
        CriteriaQuery<?> query,
        final String attribute,
        final String attributeSelector,
        final String attributeValue,
        List<Selection<?>> selectionList,
        List<String> selectionAttributes) {

        Predicate pred = cb.conjunction();

        if ("id".equals(attribute)) {
            addNewColumn(selectionList, root.get(ScepOrder_.id));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateLong(attributeSelector, cb, root.get(ScepOrder_.id), attributeValue);
            }
        } else if ("transId".equals(attribute)) {
            addNewColumn(selectionList, root.get(ScepOrder_.transId));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString(attributeSelector, cb, root.get(ScepOrder_.transId), attributeValue);
            }
        } else if ("status".equals(attribute)) {
            addNewColumn(selectionList, root.get(ScepOrder_.status));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateOrderStatus(attributeSelector, cb, root.get(ScepOrder_.status), attributeValue);
            }
        } else if ("realm".equals(attribute)) {
            addNewColumn(selectionList,root.get(ScepOrder_.realm));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString( attributeSelector, cb, root.get(ScepOrder_.realm), attributeValue.toLowerCase());
            }
        } else if ("pipelineName".equals(attribute)) {
            Join<ScepOrder, Pipeline> accJoin = root.join(ScepOrder_.pipeline, JoinType.LEFT);
            addNewColumn(selectionList,accJoin.get(Pipeline_.name));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString( attributeSelector, cb, accJoin.get(Pipeline_.name), attributeValue.toLowerCase());
            }
        } else if ("requestedOn".equals(attribute)) {
            addNewColumn(selectionList,root.get(ScepOrder_.requestedOn));
            if (attributeValue.trim().length() > 0) {
                pred = buildDatePredicate(attributeSelector, cb, root.get(ScepOrder_.requestedOn), attributeValue);
            }
        } else if ("requestedBy".equals(attribute)) {
            addNewColumn(selectionList,root.get(ScepOrder_.requestedBy));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString( attributeSelector, cb, root.get(ScepOrder_.requestedBy), attributeValue.toLowerCase());
            }
        } else if ("subject".equals(attribute)) {
            Join<ScepOrder, Certificate> accJoin = root.join(ScepOrder_.certificate, JoinType.LEFT);
            addNewColumn(selectionList,accJoin.get(Certificate_.subject));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString( attributeSelector, cb, accJoin.get(Certificate_.subject), attributeValue.toLowerCase());
            }
        } else if ("sans".equals(attribute)) {
            Join<ScepOrder, Certificate> accJoin = root.join(ScepOrder_.certificate, JoinType.LEFT);
            addNewColumn(selectionList,accJoin.get(Certificate_.sans));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString( attributeSelector, cb, accJoin.get(Certificate_.sans), attributeValue.toLowerCase());
            }

        } else {
            logger.warn("fall-thru clause adding 'true' condition for {} ", attribute);
        }
        return pred;
    }


private static Predicate buildPredicateOrderStatus(String attributeSelector, CriteriaBuilder cb, Path<ScepOrderStatus> path, String attributeValue) {
        if (attributeSelector == null) {
            return cb.conjunction();
        }

        ScepOrderStatus orderStatus;
        try {
            orderStatus = ScepOrderStatus.valueOf(attributeValue);
        }catch(IllegalArgumentException iae){
            logger.debug("buildPredicateAccountStatus not an ACMEStatus ", iae);
            return cb.disjunction();
        }

        if (Selector.EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicateAccountStatus equal ('{}') for value '{}'", attributeSelector, orderStatus);
            return cb.equal(path, orderStatus);
        } else if (Selector.NOT_EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicateAccountStatus notEqual ('{}') for value '{}'", attributeSelector, orderStatus);
            return cb.notEqual(path, orderStatus);
        } else {
            logger.debug("buildPredicateAccountStatus defaults to equals ('{}') for value '{}'", attributeSelector, orderStatus);
            return cb.equal(path, orderStatus);
        }

    }
}
