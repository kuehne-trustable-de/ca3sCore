package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.*;

import de.trustable.ca3s.core.service.dto.NamedValue;
import de.trustable.ca3s.core.service.dto.Selector;
import de.trustable.ca3s.core.service.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.util.*;

import static de.trustable.ca3s.core.repository.SpecificationsHelper.*;


public final class UserSpecifications {

    static Logger logger = LoggerFactory.getLogger(UserSpecifications.class);

    static final String SORT = "sort";
    static final String ORDER = "order";

    static final String DEFAULT_FILTER = "id,login,firstName,lastName,email,phone,activated,secondFactorRequired,langKey,blockedUntilDate,credentialsValidToDate,managedExternally,authorities";

    private UserSpecifications() {
    }


    public static String getStringValue(final String[] inArr) {
        return getStringValue(inArr, "");
    }

    public static String getStringValue(final String[] inArr, String defaultValue) {
        if (inArr == null || inArr.length == 0) {
            return defaultValue;
        } else {
            return inArr[0];
        }
    }

    public static int getIntValue(final String[] inArr, int defaultValue) {
        if (inArr == null || inArr.length == 0) {
            return defaultValue;
        } else {
            return Integer.parseInt(inArr[0]);
        }
    }


    /**
     * @param entityManager EntityManager
     * @param cb            CriteriaBuilder
     * @param parameterMap  map of parameters
     * @return page
     */
    public static Page<UserDTO> handleQueryParamsUser(EntityManager entityManager,
                                                      CriteriaBuilder cb,
                                                      Map<String, String[]> parameterMap) {
        long startTime = System.currentTimeMillis();

        List<String> userSelectionAttributes = new ArrayList<>();

        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<User> root = query.from(User.class);

        String sortCol = getStringValue(parameterMap.get("sort"), "id").trim();
        Selection<?> orderSelection = null;
        String orderDirection = getStringValue(parameterMap.get("order"), "asc");

        int pageOffset = getIntValue(parameterMap.get("offset"), 0);
        int pagesize = getIntValue(parameterMap.get("limit"), 20);

        logger.debug("buildPredicate: offset '{}', limit '{}' ", pageOffset, pagesize);

        ArrayList<Selection<?>> selectionList = new ArrayList<>();
        ArrayList<String> colList = new ArrayList<>();

        Map<String, List<SelectionData>> selectionMap = getSelectionMap(parameterMap);


        // retrieve all the required columns
        // 'filter' is a bit misleading, here...
        String[] columnArr = DEFAULT_FILTER.split(",");

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

            logger.debug("handleQueryParamsCertificateView handles column '{}' ", col);

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
                        userSelectionAttributes));
                }
            } else {
                logger.debug("buildPredicate for '{}' without selector ", col);
                predList.add(buildPredicate(root,
                    cb,
                    query,
                    col,
                    null,
                    "",
                    selectionList, userSelectionAttributes));
            }


            // if this is the sorting columns, save the selection
            if (col.equals(sortCol)) {
                orderSelection = selectionList.get(selectionList.size() - 1);
            }
        }

        ArrayList<Selection<?>> selectionListDummy = new ArrayList<>();
        for (String selection : selectionMap.keySet()) {
            boolean handled = false;
            for (String col : columnArr) {
                if (selection.equals(col)) {
                    handled = true;
                }
            }
            if (!handled) {
                List<SelectionData> selDataList = selectionMap.get(selection);
                for (SelectionData selDataItem : selDataList) {

                    predList.add(buildPredicate(root,
                        cb,
                        query,
                        selection,
                        selDataItem.selector,
                        selDataItem.value,
                        selectionListDummy, userSelectionAttributes));
                }
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
//    	query.distinct(true);

        TypedQuery<Object[]> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(pagesize);
        typedQuery.setFirstResult(pageOffset);

        try {
            logger.debug("assembled query: " + typedQuery.unwrap(org.hibernate.query.Query.class).getQueryString());
        } catch (Exception e) {
            logger.debug("failed in retrieve sql query", e);
        }

        long queryStartTime = System.currentTimeMillis();

        // submit the query
        List<Object[]> listResponse = typedQuery.getResultList();

        logger.debug("typedQuery.getResultList() took {} msecs", System.currentTimeMillis() - queryStartTime);

        // use the result set to fill the response object
        List<UserDTO> userList = new ArrayList<>();
        for (Object[] objArr : listResponse) {

            if (logger.isDebugEnabled() && (objArr.length != colList.size())) {
                logger.debug("objArr len {}, colList len {}", objArr.length, colList.size());
            }

            userList.add(buildUserFromObjArr(colList, objArr));
        }

        // start again to retrieve the row count
        Pageable pageable = PageRequest.of(pageOffset / pagesize, pagesize, sortDir, sortCol);

        Long nTotalElements = 1000L;

        CriteriaQuery<Long> queryCount = cb.createQuery(Long.class);
        Root<User> iRoot = queryCount.from(User.class);

        // collect all selectors in a list
        List<Predicate> predCountList = new ArrayList<>();

        Predicate predCount = null;
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
                        selectionListCount, userSelectionAttributes));
                }
            } else {
                logger.debug("buildPredicate for '{}' without selector ", col);
                predCountList.add(buildPredicate(iRoot,
                    cb,
                    queryCount,
                    col,
                    null,
                    "",
                    selectionListCount, userSelectionAttributes));
            }

        }

        selectionListDummy.clear();
        for (String selection : selectionMap.keySet()) {
            boolean handled = false;
            for (String col : columnArr) {
                if (selection.equals(col)) {
                    handled = true;
                }
            }
            if (!handled) {
                List<SelectionData> selDataList = selectionMap.get(selection);
                for (SelectionData selDataItem : selDataList) {

                    predCountList.add(buildPredicate(iRoot,
                        cb,
                        queryCount,
                        selection,
                        selDataItem.selector,
                        selDataItem.value,
                        selectionListDummy, userSelectionAttributes));
                }
            }
        }

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


        try {
            TypedQuery<Long> typedCountQuery = entityManager.createQuery(queryCount);
            logger.debug("assembled count query: " + typedCountQuery.unwrap(org.hibernate.query.Query.class).getQueryString());
        } catch (Exception e) {
            logger.debug("failed in retrieve sql query", e);
        }

        long countStartTime = System.currentTimeMillis();

        nTotalElements = entityManager.createQuery(queryCount).getSingleResult();

        logger.debug("count getSingleResult() took {} msecs", System.currentTimeMillis() - countStartTime);

        logger.debug("buildPredicate selects {} elements in {} msecs", nTotalElements, System.currentTimeMillis() - startTime);

        return new PageImpl<>(userList, pageable, nTotalElements);

    }


    private static UserDTO buildUserFromObjArr(ArrayList<String> colList,
                                               Object[] objArr) {
        UserDTO user = new UserDTO();
        List<NamedValue> namedValueList = new ArrayList<>();
        int i = 0;
        for (String attribute : colList) {

//			logger.debug("attribute '{}' has value '{}'", attribute, objArr[i]);

            if ("id".equalsIgnoreCase(attribute)) {
                user.setId((Long) objArr[i]);
            } else if ("login".equalsIgnoreCase(attribute)) {
                user.setLogin(objArr[i] == null ? "" : objArr[i].toString());
            } else if ("firstName".equalsIgnoreCase(attribute)) {
                user.setFirstName(objArr[i] == null ? "" : objArr[i].toString());
            } else if ("lastName".equalsIgnoreCase(attribute)) {
                user.setLastName(objArr[i] == null ? "" : objArr[i].toString());
            } else if ("email".equalsIgnoreCase(attribute)) {
                user.setEmail(objArr[i] == null ? "" : objArr[i].toString());
            } else if ("phone".equalsIgnoreCase(attribute)) {
                user.setPhone(objArr[i] == null ? "" : objArr[i].toString());
            } else if ("activated".equalsIgnoreCase(attribute)) {
                user.setActivated((Boolean) objArr[i]);
            } else if ("secondFactorRequired".equalsIgnoreCase(attribute)) {
                user.setSecondFactorRequired((Boolean) objArr[i]);
            } else if ("langKey".equalsIgnoreCase(attribute)) {
                user.setLangKey(objArr[i] == null ? "" : objArr[i].toString());
            } else if ("createdDate".equalsIgnoreCase(attribute)) {
                user.setCreatedDate((Instant) objArr[i]);
            } else if ("blockedUntilDate".equalsIgnoreCase(attribute)) {
                user.setBlockedUntilDate((Instant) objArr[i]);
            } else if ("lastModifiedBy".equalsIgnoreCase(attribute)) {
                user.setLastModifiedBy(objArr[i] == null ? "" : objArr[i].toString());
            } else if ("credentialsValidToDate".equalsIgnoreCase(attribute)) {
                user.setCredentialsValidToDate((Instant) objArr[i]);
            } else if ("isManagedExternally".equalsIgnoreCase(attribute)) {
                user.setManagedExternally((Boolean) objArr[i]);
            } else if ("isBlocked".equalsIgnoreCase(attribute)) {
//                user.setBlocked((Boolean) objArr[i]);
            } else if ("failedLogins".equalsIgnoreCase(attribute)) {
                user.setFailedLogins((Long) objArr[i]);
            } else if ("tenant".equalsIgnoreCase(attribute)) {
/*
                if (objArr[i] != null && objArr[i] instanceof Tenant) {
                    Tenant tenant = (Tenant) objArr[i];
                    user.setTenantId(tenant.getId());
                    user.setTenantName(tenant.getLongname());
                }
*/
            } else if ("authorities".equalsIgnoreCase(attribute)) {
                logger.info("attribute '{}' from query returns: {}", attribute, objArr[i]);
            } else {

                logger.info("attribute '{}' from query added as additionalRestriction", attribute);

                NamedValue namedValue = new NamedValue();
                namedValue.setName(attribute);
//                namedValue.setValue(objArr[i].toString());
                namedValueList.add(namedValue);
                continue;
            }
            i++;
        }

        return user;
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
     * @param certificateSelectionAttributes
     * @return
     */
    private static Predicate buildPredicate(
        Root<User> root,
        CriteriaBuilder cb,
        CriteriaQuery<?> userQuery,
        final String attribute,
        final String attributeSelector,
        final String attributeValue,
        List<Selection<?>> selectionList,
        List<String> certificateSelectionAttributes) {

        Predicate pred = cb.conjunction();

        if ("id".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.id));
            pred = SpecificationsHelper.buildPredicateLong(attributeSelector, cb, root.<Long>get(User_.id), attributeValue);

        } else if ("login".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.login));
            pred = SpecificationsHelper.buildPredicateString(attributeSelector, cb, root.get(User_.login), attributeValue);

        } else if ("firstName".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.firstName));
            pred = SpecificationsHelper.buildPredicateString(attributeSelector, cb, root.get(User_.firstName), attributeValue);

        } else if ("lastName".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.lastName));
            pred = SpecificationsHelper.buildPredicateString(attributeSelector, cb, root.get(User_.lastName), attributeValue);

        } else if ("email".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.email));
            pred = SpecificationsHelper.buildPredicateString(attributeSelector, cb, root.get(User_.email), attributeValue);

        } else if ("phone".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.phone));
            pred = SpecificationsHelper.buildPredicateString(attributeSelector, cb, root.get(User_.phone), attributeValue);

        } else if ("langKey".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.langKey));
            pred = SpecificationsHelper.buildPredicateString(attributeSelector, cb, root.get(User_.langKey), attributeValue);

        } else if ("activated".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.activated));
            pred = SpecificationsHelper.buildBooleanPredicate(attributeSelector, cb, root.get(User_.activated), attributeValue);

        } else if ("secondFactorRequired".equalsIgnoreCase(attribute)) {
            addNewColumn(selectionList, root.get(User_.secondFactorRequired));
            pred = SpecificationsHelper.buildBooleanPredicate(attributeSelector, cb, root.get(User_.secondFactorRequired), attributeValue);
        } else if ("failedLogins".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.failedLogins));
            pred = SpecificationsHelper.buildPredicateLong(attributeSelector, cb, root.get(User_.failedLogins), attributeValue);

        } else if ("isManagedExternally".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.managedExternally));
            pred = SpecificationsHelper.buildBooleanPredicate(attributeSelector, cb, root.get(User_.managedExternally), attributeValue);

        } else if ("createdDate".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.createdDate));
            pred = SpecificationsHelper.buildDatePredicate(attributeSelector, cb, root.get(User_.createdDate), attributeValue);

        } else if ("lastModifiedBy".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.lastModifiedBy));
            pred = SpecificationsHelper.buildPredicateString(attributeSelector, cb, root.get(User_.lastModifiedBy), attributeValue);

        } else if ("blockedUntilDate".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.blockedUntilDate));
            pred = SpecificationsHelper.buildDatePredicate(attributeSelector, cb, root.get(User_.blockedUntilDate), attributeValue);

        } else if ("credentialsValidToDate".equals(attribute)) {
            addNewColumn(selectionList, root.get(User_.credentialsValidToDate));
            pred = SpecificationsHelper.buildDatePredicate(attributeSelector, cb, root.get(User_.credentialsValidToDate), attributeValue);
        } else if ("tenant".equals(attribute)) {
            selectionList.add(root.get(User_.id));

            if(!attributeValue.isEmpty()) {
                Join<User, Tenant> attJoin = root.join(User_.tenant, JoinType.LEFT);
                pred = buildPredicateString(attributeSelector, cb, attJoin.get(Tenant_.longname), attributeValue);
            }

        } else if ("authorities".equals(attribute)) {
            selectionList.add(root.get(User_.id));

            if( attributeValue.trim().length() > 0 ) {
                //subquery
                Subquery<Authority> authoritySubquery = userQuery.subquery(Authority.class);
                Root<Authority> authorityRoot = authoritySubquery.from(Authority.class);
                pred = cb.exists(authoritySubquery.select(authorityRoot)//subquery selection
                    .where(buildPredicateString( attributeSelector, cb, authorityRoot.get(Authority_.name), attributeValue)));
            }

/*
            Join<User, Authority> attJoin = root.join(User_.authorities, JoinType.LEFT);
            addNewColumn(selectionList,attJoin.get(Authority_.name));
*/
/*
            if(!attributeValue.isEmpty()) {
                Join<User, Tenant> attJoin = root.join(User_.tenant, JoinType.LEFT);
                pred = buildPredicateString(attributeSelector, cb, attJoin.get(Tenant_.longname), attributeValue);
            }
*/

        } else if ("isBlocked".equals(attribute)) {
        }
        return pred;

    }
}
