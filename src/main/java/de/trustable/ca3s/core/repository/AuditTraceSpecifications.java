package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.service.dto.AuditTraceView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.trustable.ca3s.core.repository.SpecificationsHelper.*;


public final class AuditTraceSpecifications {

    static Logger logger = LoggerFactory.getLogger(AuditTraceSpecifications.class);

    private AuditTraceSpecifications() {
    }


    public static Page<AuditTraceView> handleQueryParamsCertificateView(EntityManager entityManager,
                                                                        CriteriaBuilder cb,
                                                                        Map<String, String[]> parameterMap) {

        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<AuditTrace> root = query.from(AuditTrace.class);

        String sortCol = getStringValue(parameterMap.get("sort"), "id").trim();
        Selection<?> orderSelection = null;
        String orderDirection = getStringValue(parameterMap.get("order"), "asc");

        int pageOffset = getIntValue(parameterMap.get("offset"), 0);
        int pagesize = getIntValue(parameterMap.get("limit"), 20);

        ArrayList<Selection<?>> selectionList = new ArrayList<Selection<?>>();
        ArrayList<String> colList = new ArrayList<String>();

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
        List<Predicate> predList = new ArrayList<Predicate>();

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
                        selectionList));
                }
            } else {
                logger.debug("buildPredicate for '{}' without selector ", col);
                predList.add(buildPredicate(root,
                    cb,
                    query,
                    col,
                    null,
                    "",
                    selectionList));
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
        List<AuditTraceView> certViewList = new ArrayList<AuditTraceView>();
        for (Object[] objArr : listResponse) {

            if (logger.isDebugEnabled() && (objArr.length != colList.size())) {
                logger.debug("objArr len {}, colList len {}", objArr.length, colList.size());
            }

            AuditTraceView cv = buildAuditTraceViewFromObjArr(colList, objArr);

            certViewList.add(cv);
        }

        // start again to retrieve the row count
        Pageable pageable = PageRequest.of(pageOffset / pagesize, pagesize, sortDir, sortCol);

        Long nTotalElements = 1000L;

        CriteriaQuery<Long> queryCount = cb.createQuery(Long.class);
        Root<AuditTrace> iRoot = queryCount.from(AuditTrace.class);

        List<Predicate> predCountList = new ArrayList<Predicate>();

        ArrayList<Selection<?>> selectionListCount = new ArrayList<Selection<?>>();

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
                        selectionListCount));
                }
            } else {
                logger.debug("buildPredicate for '{}' without selector ", col);
                predCountList.add(buildPredicate(iRoot,
                    cb,
                    queryCount,
                    col,
                    null,
                    "",
                    selectionListCount));
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

        return new PageImpl<AuditTraceView>(certViewList, pageable, nTotalElements);

    }

    private static AuditTraceView buildAuditTraceViewFromObjArr(ArrayList<String> colList, Object[] objArr) {
        AuditTraceView atv = new AuditTraceView();
        int i = 0;

        for (String attribute : colList) {

            if (i >= objArr.length) {
                logger.debug("attribute '{}' exceeds objArr with #{} elements ", attribute, objArr.length);
                continue;
            }
            logger.debug("attribute '{}' has value '{}'", attribute, objArr[i]);

            if ("id".equalsIgnoreCase(attribute)) {
                atv.setId((Long) objArr[i]);
            } else if ("actorName".equalsIgnoreCase(attribute)) {
                atv.setActorName((String) objArr[i]);
            } else if ("actorRole".equalsIgnoreCase(attribute)) {
                atv.setActorRole((String) objArr[i]);
            } else if ("contentTemplate".equalsIgnoreCase(attribute)) {
                atv.setContentTemplate((String) objArr[i]);
            } else if ("plainContent".equalsIgnoreCase(attribute)) {
                atv.setPlainContent((String) objArr[i]);
            } else if ("createdOn".equalsIgnoreCase(attribute)) {
                atv.setCreatedOn((Instant) objArr[i]);
            } else if ("certificateId".equalsIgnoreCase(attribute)) {
                atv.setCertificateId((Long) objArr[i]);
            } else if ("csrId".equalsIgnoreCase(attribute)) {
                atv.setCsrId((Long) objArr[i]);
            } else if ("caConnectorIdId".equalsIgnoreCase(attribute)) {
                atv.setCaConnectorId((Long) objArr[i]);
            } else if ("pipelineId".equalsIgnoreCase(attribute)) {
                atv.setPipelineId((Long) objArr[i]);
            } else if ("processInfoId".equalsIgnoreCase(attribute)) {
                atv.setProcessInfoId((Long) objArr[i]);
            } else {
                logger.warn("unexpected attribute '{}' from query", attribute);
            }
            i++;
        }

        return atv;
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
        Root<AuditTrace> root,
        CriteriaBuilder cb,
        CriteriaQuery<?> csrQuery,
        final String attribute,
        final String attributeSelector,
        final String attributeValue,
        List<Selection<?>> selectionList) {

        Predicate pred = cb.conjunction();

        if ("id".equals(attribute)) {
            addNewColumn(selectionList, root.get(AuditTrace_.id));
            pred = buildPredicateLong(attributeSelector, cb, root.<Long>get(AuditTrace_.id), attributeValue);

        } else if ("actorName".equals(attribute)) {
            addNewColumn(selectionList, root.get(AuditTrace_.actorName));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString(attributeSelector, cb, root.get(AuditTrace_.actorName), attributeValue);
            }
        } else if ("actorRole".equals(attribute)) {
            addNewColumn(selectionList, root.get(AuditTrace_.actorRole));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString(attributeSelector, cb, root.get(AuditTrace_.actorRole), attributeValue);
            }
        } else if ("createdOn".equals(attribute)) {
            addNewColumn(selectionList, root.get(AuditTrace_.createdOn));

        } else if ("links".equals(attribute)) {
            addNewColumn(selectionList, root.get(AuditTrace_.certificate));

        } else if ("certificateId".equals(attribute)) {
            Join<AuditTrace, Certificate> certJoin = root.join(AuditTrace_.certificate, JoinType.LEFT);
            addNewColumn(selectionList, certJoin.get(Certificate_.id));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateLong(attributeSelector, cb, certJoin.<Long>get(Certificate_.id), attributeValue);
            }
        } else if ("csrId".equals(attribute)) {
            Join<AuditTrace, CSR> csrJoin = root.join(AuditTrace_.csr, JoinType.LEFT);
            addNewColumn(selectionList, csrJoin.get(CSR_.id));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateLong(attributeSelector, cb, csrJoin.<Long>get(CSR_.id), attributeValue);
            }
/*
        } else if ("subject".equals(attribute)) {
            addNewColumn(selectionList, root.get(AuditTrace_.subject));

            if (attributeValue.trim().length() > 0) {
                //subquery
                Subquery<CsrAttribute> csrAttSubquery = csrQuery.subquery(CsrAttribute.class);
                Root<CsrAttribute> csrAttRoot = csrAttSubquery.from(CsrAttribute.class);
                pred = cb.exists(csrAttSubquery.select(csrAttRoot)//subquery selection
                    .where(cb.and(cb.equal(csrAttRoot.get(CsrAttribute_.CSR), root.get(AuditTrace_.ID)),
                        cb.equal(csrAttRoot.get(CsrAttribute_.NAME), CsrAttribute.ATTRIBUTE_SUBJECT),
                        buildPredicate(attributeSelector, cb, csrAttRoot.<String>get(CsrAttribute_.value), attributeValue.toLowerCase()))));
            }

 */
        } else if ("contentTemplate".equals(attribute)) {
            addNewColumn(selectionList, root.get(AuditTrace_.contentTemplate));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString(attributeSelector, cb, root.get(AuditTrace_.contentTemplate), attributeValue);
            }

        } else if ("plainContent".equals(attribute)) {
            addNewColumn(selectionList, root.get(AuditTrace_.plainContent));
            if (attributeValue.trim().length() > 0) {
                pred = buildPredicateString(attributeSelector, cb, root.get(AuditTrace_.plainContent), attributeValue);
            }

        } else {
            logger.warn("fall-thru clause adding 'true' condition for {} ", attribute);
        }
        return pred;
    }

}
