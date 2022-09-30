package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.service.dto.AcmeAccountView;
import de.trustable.ca3s.core.service.dto.Selector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.trustable.ca3s.core.repository.SpecificationsHelper.*;


public final class AcmeAccountSpecifications {

    static Logger logger = LoggerFactory.getLogger(AcmeAccountSpecifications.class);

    static final String SORT = "sort";
    static final String ORDER = "order";

    private AcmeAccountSpecifications() {
    }


    public static Page<AcmeAccountView> handleQueryParamsAcmeAccountView(EntityManager entityManager,
                                                                         AcmeOrderRepository acmeOrderRepository,
                                                                         AcmeContactRepository acmeContactRepository,
                                                                         CriteriaBuilder cb,
                                                                         Map<String, String[]> parameterMap,
                                                                         List<String> additionalSelectionAttributes) {

        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<AcmeAccount> root = query.from(AcmeAccount.class);

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
        List<AcmeAccountView> acmeAccountViewList = new ArrayList<>();
        for (Object[] objArr : listResponse) {

            if (logger.isDebugEnabled() && (objArr.length != colList.size())) {
                logger.debug("objArr len {}, colList len {}", objArr.length, colList.size());
            }

            AcmeAccountView acmeAccountView = buildCSRViewFromObjArr(colList, objArr, acmeOrderRepository, acmeContactRepository);
            acmeAccountViewList.add(acmeAccountView);
        }

        // start again to retrieve the row count
        Pageable pageable = PageRequest.of(pageOffset / pagesize, pagesize, sortDir, sortCol);

        Long nTotalElements;

        CriteriaQuery<Long> queryCount = cb.createQuery(Long.class);
        Root<AcmeAccount> iRoot = queryCount.from(AcmeAccount.class);

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

        return new PageImpl<>(acmeAccountViewList, pageable, nTotalElements);

    }

    private static AcmeAccountView buildCSRViewFromObjArr(ArrayList<String> colList,
                                                          Object[] objArr,
                                                          AcmeOrderRepository acmeOrderRepository,
                                                          AcmeContactRepository acmeContactRepository) {
        AcmeAccountView acmeAccountView = new AcmeAccountView();
        int i = 0;

        for (String attribute : colList) {

            if (i >= objArr.length) {
                logger.debug("attribute '{}' exceeds objArr with #{} elements ", attribute, objArr.length);
                continue;
            }
            logger.debug("attribute '{}' has value '{}'", attribute, objArr[i]);

            if ("id".equalsIgnoreCase(attribute)) {
                acmeAccountView.setId((Long) objArr[i]);
            } else if ("accountId".equalsIgnoreCase(attribute)) {
                acmeAccountView.setAccountId((Long) objArr[i]);
            } else if ("status".equalsIgnoreCase(attribute)) {
                acmeAccountView.setStatus((AccountStatus) objArr[i]);
            } else if ("realm".equalsIgnoreCase(attribute)) {
                acmeAccountView.setRealm((String) objArr[i]);
            } else if ("createdOn".equalsIgnoreCase(attribute)) {
                acmeAccountView.setCreatedOn((Instant) objArr[i]);
            } else if ("publicKeyHash".equalsIgnoreCase(attribute)) {
                acmeAccountView.setPublicKeyHash((String) objArr[i]);
            } else if ("termsOfServiceAgreed".equalsIgnoreCase(attribute)) {
                acmeAccountView.setTermsOfServiceAgreed((Boolean) objArr[i]);
            } else if ("contactUrls".equalsIgnoreCase(attribute)) {
                List<AcmeContact> acmeContactList = acmeContactRepository.findByAccountId((Long) objArr[i]);
                logger.debug("acme account #íd {} has #{} contacts", objArr[i], acmeContactList.size());

                List<String> contactList = new ArrayList<>();
                for(AcmeContact acmeContact: acmeContactList){
                    contactList.add(acmeContact.getContactUrl());
                }
                acmeAccountView.setContactUrls(contactList.toArray(new String[0]));
            } else if ("orderCount".equalsIgnoreCase(attribute)) {
                long orderCount = acmeOrderRepository.countByAccountId((Long) objArr[i]);
                logger.debug("acme account #íd {} has #{} orders", objArr[i], orderCount);
                acmeAccountView.setOrderCount(orderCount);
            } else {
                logger.warn("unexpected attribute '{}' from query", attribute);
            }
            i++;
        }

        return acmeAccountView;
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
        Root<AcmeAccount> root,
        CriteriaBuilder cb,
        CriteriaQuery<?> csrQuery,
        final String attribute,
        final String attributeSelector,
        final String attributeValue,
        List<Selection<?>> selectionList,
        List<String> acmeAccountSelectionAttributes) {

        Predicate pred = cb.conjunction();

        if ("id".equals(attribute)) {
            addNewColumn(selectionList, root.get(AcmeAccount_.id));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateLong(attributeSelector, cb, root.get(AcmeAccount_.id), attributeValue);
            }
        } else if ("accountId".equals(attribute)) {
            addNewColumn(selectionList, root.get(AcmeAccount_.accountId));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateLong(attributeSelector, cb, root.get(AcmeAccount_.accountId), attributeValue);
            }
        } else if ("status".equals(attribute)) {
            addNewColumn(selectionList, root.get(AcmeAccount_.status));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateAccountStatus(attributeSelector, cb, root.get(AcmeAccount_.status), attributeValue);
            }
        } else if ("realm".equals(attribute)) {
            addNewColumn(selectionList, root.get(AcmeAccount_.realm));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString(attributeSelector, cb, root.get(AcmeAccount_.realm), attributeValue);
            }
        } else if ("termsOfServiceAgreed".equals(attribute)) {
            addNewColumn(selectionList, root.get(AcmeAccount_.termsOfServiceAgreed));
            if (attributeValue.trim().length() > 0) {
                pred = SpecificationsHelper.buildBooleanPredicate( attributeSelector, cb, root.get(AcmeAccount_.termsOfServiceAgreed), attributeValue);
            }

        } else if ("createdOn".equals(attribute)) {
            addNewColumn(selectionList, root.get(AcmeAccount_.createdOn));
            if (attributeValue.trim().length() > 0) {
                pred = SpecificationsHelper.buildDatePredicate( attributeSelector, cb, root.get(AcmeAccount_.createdOn), attributeValue);
            }

        } else if ("publicKeyHash".equals(attribute)) {
            addNewColumn(selectionList, root.get(AcmeAccount_.publicKeyHash));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString(attributeSelector, cb, root.get(AcmeAccount_.publicKeyHash), attributeValue);
            }

        } else if ("contactUrls".equals(attribute)) {
            selectionList.add(root.get(AcmeAccount_.accountId));

            if (attributeValue.trim().length() > 0) {
                Join<AcmeAccount, AcmeContact> contactJoin = root.join(AcmeAccount_.contacts, JoinType.LEFT);
                pred = buildPredicateString( attributeSelector, cb, contactJoin.<String>get(AcmeContact_.contactUrl), attributeValue.toLowerCase());
            }
        } else if ("orderCount".equals(attribute)) {
            selectionList.add(root.get(AcmeAccount_.accountId));
        } else {
            logger.warn("fall-thru clause adding 'true' condition for {} ", attribute);
        }
        return pred;
    }

    private static Predicate buildPredicateAccountStatus(String attributeSelector, CriteriaBuilder cb, Path<AccountStatus> path, String attributeValue) {
        if (attributeSelector == null) {
            return cb.conjunction();
        }

        AccountStatus accountStatus = AccountStatus.valueOf(attributeValue);

        if (Selector.EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicateAccountStatus equal ('{}') for value '{}'", attributeSelector, accountStatus);
            return cb.equal(path, accountStatus);
        } else if (Selector.NOT_EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicateAccountStatus notEqual ('{}') for value '{}'", attributeSelector, accountStatus);
            return cb.notEqual(path, accountStatus);
        } else {
            logger.debug("buildPredicateAccountStatus defaults to equals ('{}') for value '{}'", attributeSelector, accountStatus);
            return cb.equal(path, accountStatus);
        }

    }
}
