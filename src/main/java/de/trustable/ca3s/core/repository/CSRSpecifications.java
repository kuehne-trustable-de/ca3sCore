package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.service.dto.CSRView;
import de.trustable.ca3s.core.service.dto.Selector;
import de.trustable.ca3s.core.web.rest.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static de.trustable.ca3s.core.repository.SpecificationsHelper.*;


public final class CSRSpecifications {

    static Logger logger = LoggerFactory.getLogger(CSRSpecifications.class);

    static final String SORT = "sort";
    static final String ORDER = "order";

    private CSRSpecifications() {
    }


    public static Page<CSRView> handleQueryParamsCSRView(EntityManager entityManager,
                                                         CriteriaBuilder cb,
                                                         Map<String, String[]> parameterMap,
                                                         List<String> csrSelectionAttributes,
                                                         List<Long> pipelineIds,
                                                         User user,
                                                         String certificateStoreIsolation) {

        boolean useTenant = true;
        if( UserUtil.isAdministrativeUser(user) ||
            "none".equalsIgnoreCase(certificateStoreIsolation)){
            useTenant = false;
        }else if( user.getTenant() == null ){
            // null == default tenant
            useTenant = false;
        }

        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<CSR> root = query.from(CSR.class);

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

        HashSet<String> columnSet = new HashSet<>();
        if (parameterMap.containsKey("filter")) {
            String[] paramArr = parameterMap.get("filter");
            if (paramArr.length > 0) {
                String[] columnArr = paramArr[0].split(",");
                Collections.addAll(columnSet, columnArr);
            }
        }


        // collect all selectors in a list
        List<Predicate> predList = new ArrayList<>();

        if( selectionMap.containsKey("isAdministrable")) {

            List<String> pipelineIdList =
                pipelineIds.stream().map(Object::toString).collect(Collectors.toList());
            logger.debug("buildPredicate for 'isAdministrable' with assigned pipeline ids {}", pipelineIdList);

            predList.add(buildPredicate(root,
                cb,
                query,
                "pipelineId",
                "IN",
                String.join(", ", pipelineIdList),
                selectionList,
                csrSelectionAttributes));

            columnSet.add("pipelineId");
        }

        // walk thru all requested columns
        for (String col : columnSet) {
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
                        csrSelectionAttributes));
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
                    csrSelectionAttributes));
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

        if( useTenant ){
            logger.debug("build additional predicate tenant '{}'", user.getTenant());
            pred = addTenantClause(cb, user, root, pred);
        }else{
            logger.debug("useTenant == false");
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

        // submit the query
        List<Object[]> listResponse = typedQuery.getResultList();

        // use the result set to fill the response object
        List<CSRView> certViewList = new ArrayList<>();
        for (Object[] objArr : listResponse) {

            if (logger.isDebugEnabled() && (objArr.length != colList.size())) {
                logger.debug("objArr len {}, colList len {}", objArr.length, colList.size());
            }

            CSRView cv = buildCSRViewFromObjArr(colList, objArr);

            certViewList.add(cv);
        }

        // start again to retrieve the row count
        Pageable pageable = PageRequest.of(pageOffset / pagesize, pagesize, sortDir, sortCol);

        Long nTotalElements;

        CriteriaQuery<Long> queryCount = cb.createQuery(Long.class);
        Root<CSR> iRoot = queryCount.from(CSR.class);

        List<Predicate> predCountList = new ArrayList<>();

        ArrayList<Selection<?>> selectionListCount = new ArrayList<>();

        // walk thru all requested columns
        for (String col : columnSet) {
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
                        csrSelectionAttributes));
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
                    csrSelectionAttributes));
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

        if( useTenant ){
            logger.debug("build additional predicate tenant '{}'", user.getTenant());
            predCount = addTenantClause(cb, user, root, predCount);
        }else{
            logger.debug("useTenant == false");
        }

        queryCount.select(cb.count(iRoot));

        queryCount.where(predCount);

        nTotalElements = entityManager.createQuery(queryCount).getSingleResult();
        logger.debug("buildPredicate selects {} elements ", nTotalElements);

        return new PageImpl<>(certViewList, pageable, nTotalElements);

    }


    private static Predicate addTenantClause(CriteriaBuilder cb, User user, Root<CSR> root, Predicate predicate) {
        return cb.and(predicate, cb.equal(root.get(CSR_.tenant), user.getTenant()));
    }

    private static CSRView buildCSRViewFromObjArr(ArrayList<String> colList, Object[] objArr) {
        CSRView cv = new CSRView();
        int i = 0;

        logger.debug("objArr length: #{}, colList length #{} ", objArr.length, colList.size());

        for (String attribute : colList) {

            if (i >= objArr.length) {
                logger.debug("attribute '{}' exceeds objArr with #{} elements ", attribute, objArr.length);
                continue;
            }
            logger.debug("attribute '{}' has value '{}'", attribute, objArr[i]);

            if ("id".equalsIgnoreCase(attribute)) {
                cv.setId((Long) objArr[i]);
            } else if ("certificateId".equalsIgnoreCase(attribute)) {
                cv.setCertificateId((Long) objArr[i]);
            } else if ("status".equalsIgnoreCase(attribute)) {
                cv.setStatus((CsrStatus) objArr[i]);
            } else if ("subject".equalsIgnoreCase(attribute)) {
                cv.setSubject((String) objArr[i]);
            } else if ("sans".equalsIgnoreCase(attribute)) {
                cv.setSans((String) objArr[i]);
            } else if ("publicKeyAlgorithm".equalsIgnoreCase(attribute)) {
                cv.setPublicKeyAlgorithm((String) objArr[i]);
            } else if ("signingAlgorithm".equalsIgnoreCase(attribute)) {
                cv.setSigningAlgorithm((String) objArr[i]);
            } else if ("hashAlgorithm".equalsIgnoreCase(attribute)) {
                cv.setHashAlgorithm((String) objArr[i]);
            } else if ("keyAlgorithm".equalsIgnoreCase(attribute)) {
                cv.setKeyAlgorithm((String) objArr[i]);
            } else if ("keyLength".equalsIgnoreCase(attribute)) {
                cv.setKeyLength(objArr[i].toString());
            } else if ("x509KeySpec".equalsIgnoreCase(attribute)) {
                cv.setX509KeySpec((String) objArr[i]);
            } else if ("requestedBy".equalsIgnoreCase(attribute)) {
                cv.setRequestedBy((String) objArr[i]);
            } else if ("acceptedBy".equalsIgnoreCase(attribute)) {
                cv.setAcceptedBy((String) objArr[i]);
                logger.debug("AcceptedBy: '{}'", cv.getAcceptedBy());
            } else if ("processingCA".equalsIgnoreCase(attribute)) {
                cv.setProcessingCA((String) objArr[i]);
            } else if ("pipelineName".equalsIgnoreCase(attribute)) {
                cv.setPipelineName((String) objArr[i]);
            } else if ("pipelineId".equalsIgnoreCase(attribute)) {
                cv.setPipelineId((Long) objArr[i]);
            } else if ("pipelineType".equalsIgnoreCase(attribute)) {
                cv.setPipelineType((PipelineType) objArr[i]);
            } else if ("requestedOn".equalsIgnoreCase(attribute)) {
                cv.setRequestedOn((Instant) objArr[i]);
            } else if ("rejectedOn".equalsIgnoreCase(attribute)) {
                cv.setRejectedOn((Instant) objArr[i]);
            } else if ("rejectionReason".equalsIgnoreCase(attribute)) {
                cv.setRejectionReason((String) objArr[i]);
            } else if ("isAdministrable".equalsIgnoreCase(attribute)) {
                // not a database result column
                continue;
            } else {
                logger.warn("unexpected attribute '{}' from query", attribute);
            }
            i++;
        }
        return cv;
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
                    logger.debug("adding selector to existing list for '{}'", attribute);
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
        Root<CSR> root,
        CriteriaBuilder cb,
        CriteriaQuery<?> csrQuery,
        final String attribute,
        final String attributeSelector,
        final String attributeValue,
        List<Selection<?>> selectionList,
        List<String> csrSelectionAttributes) {

        Predicate pred = cb.conjunction();

        if ("id".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.id));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and( buildPredicateLong(attributeSelector, cb, root.get(CSR_.id), attributeValue));
            }
        } else if ("status".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.status));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and( buildPredicateCsrStatus(attributeSelector, cb, root.get(CSR_.status), attributeValue));
            }
        } else if ("certificateId".equals(attribute)) {
            Join<CSR, Certificate> certJoin = root.join(CSR_.certificate, JoinType.LEFT);
            addNewColumn(selectionList, certJoin.get(Certificate_.id));

        } else if ("subject".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.subject));

            if (attributeValue.trim().length() > 0) {
                //subquery
                Subquery<CsrAttribute> csrAttSubquery = csrQuery.subquery(CsrAttribute.class);
                Root<CsrAttribute> csrAttRoot = csrAttSubquery.from(CsrAttribute.class);
                pred = cb.and(
                    cb.exists(csrAttSubquery.select(csrAttRoot)//subquery selection
                    .where(cb.and(cb.equal(csrAttRoot.get(CsrAttribute_.CSR), root.get(CSR_.ID)),
                        cb.equal(csrAttRoot.get(CsrAttribute_.NAME), CsrAttribute.ATTRIBUTE_SUBJECT),
                        buildPredicateString(attributeSelector, cb, csrAttRoot.get(CsrAttribute_.value), attributeValue.toLowerCase()))))
                );
            }
        } else if ("sans".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.sans));

            if (attributeValue.trim().length() > 0) {
                //subquery
                Subquery<CsrAttribute> csrAttSubquery = csrQuery.subquery(CsrAttribute.class);
                Root<CsrAttribute> csrAttRoot = csrAttSubquery.from(CsrAttribute.class);
                pred = cb.and(
                    cb.exists(csrAttSubquery.select(csrAttRoot)//subquery selection
                    .where(cb.and(cb.equal(csrAttRoot.get(CsrAttribute_.CSR), root.get(CSR_.ID)),
                        cb.equal(csrAttRoot.get(CsrAttribute_.NAME), CsrAttribute.ATTRIBUTE_SAN),
                        buildPredicateString(attributeSelector, cb, csrAttRoot.get(CsrAttribute_.value), attributeValue.toLowerCase()))))
                );
            }
        } else if ("publicKeyAlgorithm".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.publicKeyAlgorithm));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(
                    buildPredicateString(attributeSelector, cb, root.get(CSR_.publicKeyAlgorithm), attributeValue));
            }

        } else if ("signingAlgorithm".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.signingAlgorithm));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(
                    buildPredicateString(attributeSelector, cb, root.get(CSR_.signingAlgorithm), attributeValue));
            }

        } else if ("x509KeySpec".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.x509KeySpec));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(
                    buildPredicateString(attributeSelector, cb, root.get(CSR_.x509KeySpec), attributeValue));
            }

        } else if ("requestedBy".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.requestedBy));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(
                    buildPredicateString(attributeSelector, cb, root.get(CSR_.requestedBy), attributeValue));
            }
        }else if( "acceptedBy".equals(attribute)){
            addNewColumn(selectionList, root.get(CSR_.acceptedBy));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(
                    buildPredicateString(attributeSelector, cb, root.get(CSR_.acceptedBy), attributeValue));
            }
/*
		}else if( "processingCA".equals(attribute)){
			Join<CSR, Pipeline> certJoin = root.join(CSR_.pipeline, JoinType.LEFT);
			addNewColumn(selectionList,certJoin.get(Pipeline_.CA_CONNECTOR));
*/
        } else if ("pipelineId".equals(attribute)) {
            Join<CSR, Pipeline> certJoin = root.join(CSR_.pipeline, JoinType.LEFT);
            addNewColumn(selectionList, certJoin.get(Pipeline_.id));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(buildPredicateLong(attributeSelector, cb, certJoin.get(Pipeline_.id), attributeValue));
            }

        } else if ("pipelineName".equals(attribute)) {
            Join<CSR, Pipeline> certJoin = root.join(CSR_.pipeline, JoinType.LEFT);
            addNewColumn(selectionList, certJoin.get(Pipeline_.name));

        } else if ("pipelineType".equals(attribute)) {
            Join<CSR, Pipeline> certJoin = root.join(CSR_.pipeline, JoinType.LEFT);
            addNewColumn(selectionList, certJoin.get(Pipeline_.type));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(buildPredicatePipelineType(attributeSelector, cb, certJoin.get(Pipeline_.type), attributeValue));
            }
        }else if( "hashAlgorithm".equals(attribute)){
            Join<CSR, CsrAttribute> attJoin = root.join(CSR_.csrAttributes, JoinType.LEFT);
            pred = cb.and( cb.equal(attJoin.get(CsrAttribute_.name), "HASH_ALGO"),
                buildPredicateString( attributeSelector, cb, attJoin.get(CsrAttribute_.value), attributeValue.toLowerCase()));

        } else if ("keyLength".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.keyLength));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(buildPredicateInteger(attributeSelector, cb, root.get(CSR_.keyLength), attributeValue));
            }
        } else if ("requestedOn".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.requestedOn));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(buildDatePredicate(attributeSelector, cb, root.get(CSR_.requestedOn), attributeValue));
            }
        } else if ("rejectedOn".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.rejectedOn));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(buildDatePredicate(attributeSelector, cb, root.get(CSR_.rejectedOn), attributeValue));
            }
        } else if ("rejectionReason".equals(attribute)) {
            addNewColumn(selectionList, root.get(CSR_.rejectionReason));
            if (attributeValue.trim().length() > 0) {
                pred = cb.and(buildPredicateString(attributeSelector, cb, root.get(CSR_.rejectionReason), attributeValue));
            }
        } else {
            if( csrSelectionAttributes.contains(attribute) ){
                // handle ARAs
                Join<CSR, CsrAttribute> attJoin = root.join(CSR_.csrAttributes, JoinType.LEFT);
                pred = cb.and( cb.equal(attJoin.get(CsrAttribute_.name), CsrAttribute.ARA_PREFIX + attribute),
                    buildPredicateString( attributeSelector, cb, attJoin.get(CsrAttribute_.value), attributeValue.toLowerCase()));

            }else {
                logger.warn("fall-thru clause adding 'true' condition for {} ", attribute);
            }

            logger.warn("fall-thru clause adding 'true' condition for {} ", attribute);
        }
        return pred;
    }

    private static Predicate buildPredicateCsrStatus(String attributeSelector, CriteriaBuilder cb, Path<CsrStatus> path, String attributeValue) {
        if (attributeSelector == null) {
            return cb.conjunction();
        }

        CsrStatus csrStatus;
        try {
            csrStatus = CsrStatus.valueOf(attributeValue);
        }catch(IllegalArgumentException iae){
            logger.debug("buildPredicateCsrStatus not an CsrStatus ", iae);
            return cb.disjunction();
        }

        if (Selector.EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicateCsrStatus equal ('{}') for value '{}'", attributeSelector, csrStatus);
            return cb.equal(path, csrStatus);
        } else if (Selector.NOT_EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicateCsrStatus notEqual ('{}') for value '{}'", attributeSelector, csrStatus);
            return cb.notEqual(path, csrStatus);
        } else {
            logger.debug("buildPredicateCsrStatus defaults to equals ('{}') for value '{}'", attributeSelector, csrStatus);
            return cb.equal(path, csrStatus);
        }

    }
    private static Predicate buildPredicatePipelineType(String attributeSelector, CriteriaBuilder cb, Path<PipelineType> path, String attributeValue) {
        if (attributeSelector == null) {
            return cb.conjunction();
        }

        PipelineType pipelineType;
        try {
            pipelineType = PipelineType.valueOf(attributeValue);
        }catch(IllegalArgumentException iae){
            logger.debug("buildPredicateCsrStatus not a PipelineType ", iae);
            return cb.disjunction();
        }

        if (Selector.EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicatePipelineType equal ('{}') for value '{}'", attributeSelector, pipelineType);
            return cb.equal(path, pipelineType);
        } else if (Selector.NOT_EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicatePipelineType notEqual ('{}') for value '{}'", attributeSelector, pipelineType);
            return cb.notEqual(path, pipelineType);
        } else {
            logger.debug("buildPredicatePipelineType defaults to equals ('{}') for value '{}'", attributeSelector, pipelineType);
            return cb.equal(path, pipelineType);
        }
    }
}
