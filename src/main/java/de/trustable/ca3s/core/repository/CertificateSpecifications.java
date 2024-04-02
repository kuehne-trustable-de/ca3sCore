package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.service.dto.CertificateView;
import de.trustable.ca3s.core.service.dto.NamedValue;
import de.trustable.ca3s.core.service.dto.Selector;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.web.rest.util.CurrentUserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import javax.naming.ldap.Rdn;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

import static de.trustable.ca3s.core.repository.SpecificationsHelper.*;


public final class CertificateSpecifications {

	static Logger logger = LoggerFactory.getLogger(CertificateSpecifications.class);

	static final String SORT = "sort";
	static final String ORDER = "order";

    static final String DEFAULT_FILTER ="id,issuer,subject";

    private CertificateSpecifications() {}

    public static Specification<Certificate> subjectOrIssuer(String searchTerm) {
        return (root, query, cb) -> {
            String containsLikePattern = getContainsLikePattern(searchTerm);
            return cb.or(
                    cb.like(cb.lower(root.get(Certificate_.subject)), containsLikePattern),
                    cb.like(cb.lower(root.get(Certificate_.issuer)), containsLikePattern)
            );
        };
    }

    public static String getStringValue(final String[] inArr){
    	return getStringValue(inArr, "");
    }

    public static String getStringValue(final String[] inArr, String defaultValue){
    	if( inArr == null || inArr.length == 0){
    		return defaultValue;
    	}else{
    		return inArr[0];
    	}
    }

    public static int getIntValue(final String[] inArr, int defaultValue){
    	if( inArr == null || inArr.length == 0){
    		return defaultValue;
    	}else{
    		return Integer.parseInt(inArr[0]);
    	}
    }

    /**
     *
     * @param entityManager EntityManager
     * @param cb CriteriaBuilder
     * @param rdnList list of RDNs
     * @return list of certificates
     */
	public static List<Certificate> findCertificatesBySubject(EntityManager entityManager,
			CriteriaBuilder cb,
			List<Rdn> rdnList) {

		CriteriaQuery<Certificate> query = cb.createQuery(Certificate.class);
		Root<Certificate> root = query.from(Certificate.class);


		Predicate pred = cb.conjunction();
    	for( Rdn rdn: rdnList) {

    		String rdnExpression = rdn.getType() + "=" + rdn.getValue();
			logger.debug("single rdn representation '{}' ", rdnExpression);

		    Subquery<CertificateAttribute> certAttSubquery = query.subquery(CertificateAttribute.class);
		    Root<CertificateAttribute> certAttRoot = certAttSubquery.from(CertificateAttribute.class);
		    Predicate predPart = cb.exists(certAttSubquery.select(certAttRoot)//subquery selection
                     .where(cb.and( cb.equal(certAttRoot.get(CertificateAttribute_.CERTIFICATE), root.get(Certificate_.ID)),
                    		 cb.equal(certAttRoot.get(CertificateAttribute_.NAME), CertificateAttribute.ATTRIBUTE_SUBJECT),
                         buildPredicateString( Selector.EQUAL.toString(), cb, certAttRoot.get(CertificateAttribute_.value), rdnExpression))));

			pred = cb.and(pred, predPart);

    	}

		query.where(pred);

//		query.multiselect(selectionList);
//    	query.distinct(true);

    	TypedQuery<Certificate> typedQuery = entityManager.createQuery(query);

    	try {
    		logger.debug("assembled query: " + typedQuery.unwrap(org.hibernate.query.Query.class).getQueryString());
    	}catch( Exception e ){
    		logger.debug("failed in retrieve sql query", e);
    	}

		long queryStartTime = System.currentTimeMillis();

    	// submit the query
    	List<Certificate> listResponse = typedQuery.getResultList();

		logger.debug("typedQuery.getResultList() return {} items took {} msecs", listResponse.size(), System.currentTimeMillis() - queryStartTime);

		return listResponse;
	}

    /**
     * @param entityManager                  EntityManager
     * @param cb                             CriteriaBuilder
     * @param parameterMap                   map of parameters
     * @param certificateSelectionAttributes
     * @param user
     * @return page
     */
	public static Page<CertificateView> handleQueryParamsCertificateView(EntityManager entityManager,
                                                                         CriteriaBuilder cb,
                                                                         Map<String, String[]> parameterMap,
                                                                         List<String> certificateSelectionAttributes,
                                                                         CertificateRepository certificateRepository,
                                                                         User user,
                                                                         String certificateStoreIsolation) {
		long startTime = System.currentTimeMillis();

        boolean useTenant = true;
        if( CurrentUserUtil.isAdministrativeUser(user) ||
            "none".equalsIgnoreCase(certificateStoreIsolation)){
            useTenant = false;
        }else if( user.getTenant() == null ){
            // null == default tenant
            useTenant = false;
        }

        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
		Root<Certificate> root = query.from(Certificate.class);

		String sortCol = getStringValue(parameterMap.get("sort"), "id").trim();
		Selection<?> orderSelection = null;
		String orderDirection = getStringValue(parameterMap.get("order"), "asc");

    	int pageOffset = getIntValue( parameterMap.get("offset"), 0 );
    	int pagesize = getIntValue( parameterMap.get("limit"), 20 );

        logger.debug("buildPredicate: offset '{}', limit '{}' ", pageOffset, pagesize);

        ArrayList<Selection<?>> selectionList = new ArrayList<>();
		ArrayList<String> colList = new ArrayList<>();

		Map<String, List<SelectionData>> selectionMap = getSelectionMap(parameterMap);


		// retrieve all the required columns
		// 'filter' is a bit misleading, here...
    	String[] columnArr = DEFAULT_FILTER.split(",");
        boolean idOnly = true;

		if( parameterMap.containsKey("filter")){
			String[] paramArr = parameterMap.get("filter");
			if( paramArr.length > 0) {
                idOnly = false;
				columnArr = paramArr[0].split(",");
			}
		}

		// collect all selectors in a list
		List<Predicate> predList = new ArrayList<>();

		// walk thru all requested columns
		for( String col: columnArr) {

            logger.debug("handleQueryParamsCertificateView handles column '{}' ", col);

            colList.add(col);

			if( selectionMap.containsKey(col) ) {
				List<SelectionData> selDataList = selectionMap.get(col);
				for(SelectionData selDataItem: selDataList ) {
					logger.debug("buildPredicate for '{}', selector '{}', value '{}' ", col, selDataItem.selector, selDataItem.value);

					predList.add( buildPredicate( root,
							cb,
							query,
							col,
							selDataItem.selector,
							selDataItem.value,
							selectionList,
                        certificateSelectionAttributes));
				}
			}else {
				logger.debug("buildPredicate for '{}' without selector ", col );
				predList.add( buildPredicate( root,
					cb,
					query,
					col,
					null,
					"",
					selectionList, certificateSelectionAttributes));
			}


			// if this is the sorting columns, save the selection
			if( col.equals(sortCol)) {
				orderSelection = selectionList.get(selectionList.size()-1);
			}
		}

		ArrayList<Selection<?>> selectionListDummy = new ArrayList<>();
		for( String selection: selectionMap.keySet()) {
			boolean handled = false;
			for( String col: columnArr) {
				if( selection.equals(col)) {
					handled=true;
				}
			}
			if( !handled) {
				List<SelectionData> selDataList = selectionMap.get(selection);
				for(SelectionData selDataItem: selDataList ) {

					predList.add( buildPredicate( root,
							cb,
							query,
							selection,
							selDataItem.selector,
							selDataItem.value,
							selectionListDummy, certificateSelectionAttributes));
				}
			}
		}

		// chain all the conditions together
    	Predicate pred = null;
    	for( Predicate predPart: predList) {
			// chain all the predicates
			if( pred == null ){
				pred = predPart;
			}else{
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
		if( "asc".equalsIgnoreCase(orderDirection)) {
			query.orderBy(cb.asc((Expression<?>) orderSelection));
		}else {
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
    	}catch( Exception e ){
    		logger.debug("failed in retrieve sql query", e);
    	}

		long queryStartTime = System.currentTimeMillis();

    	// submit the query
    	List<Object[]> listResponse = typedQuery.getResultList();

		logger.debug("typedQuery.getResultList() took {} msecs", System.currentTimeMillis() - queryStartTime);

    	// use the result set to fill the response object
    	List<CertificateView> certViewList = new ArrayList<>();
        for (Object[] objArr : listResponse) {

            if (logger.isDebugEnabled() && (objArr.length != colList.size())) {
                logger.debug("objArr len {}, colList len {}", objArr.length, colList.size());
            }

            CertificateView cv = buildCertificateViewFromObjArr(colList, certificateRepository, objArr);
            certViewList.add(cv);
        }

    	// start again to retrieve the row count
        Pageable pageable = PageRequest.of(pageOffset / pagesize, pagesize, sortDir, sortCol);

        Long nTotalElements = 1000L;

        CriteriaQuery<Long> queryCount = cb.createQuery(Long.class);
        Root<Certificate> iRoot = queryCount.from(Certificate.class);

		// collect all selectors in a list
		List<Predicate> predCountList = new ArrayList<>();

    	Predicate predCount = null;
		ArrayList<Selection<?>> selectionListCount = new ArrayList<>();

		// walk thru all requested columns
		for( String col: columnArr) {
			colList.add(col);

			if( selectionMap.containsKey(col) ) {
				List<SelectionData> selDataList = selectionMap.get(col);
				for(SelectionData selDataItem: selDataList ) {
					logger.debug("buildPredicate for '{}', selector '{}', value '{}' ", col, selDataItem.selector, selDataItem.value);

					predCountList.add( buildPredicate( iRoot,
							cb,
							queryCount,
							col,
							selDataItem.selector,
							selDataItem.value,
							selectionListCount, certificateSelectionAttributes));
				}
			}else {
				logger.debug("buildPredicate for '{}' without selector ", col );
				predCountList.add( buildPredicate( iRoot,
					cb,
					queryCount,
					col,
					null,
					"",
					selectionListCount, certificateSelectionAttributes));
			}

		}

		selectionListDummy.clear();
		for( String selection: selectionMap.keySet()) {
			boolean handled = false;
			for( String col: columnArr) {
				if( selection.equals(col)) {
					handled=true;
				}
			}
			if( !handled) {
				List<SelectionData> selDataList = selectionMap.get(selection);
				for(SelectionData selDataItem: selDataList ) {

					predCountList.add( buildPredicate( iRoot,
							cb,
							queryCount,
							selection,
							selDataItem.selector,
							selDataItem.value,
							selectionListDummy, certificateSelectionAttributes));
				}
			}
		}

		// chain all the conditions together
    	for( Predicate predPart: predCountList) {
			// chain all the predicates
			if( predCount == null ){
				predCount = predPart;
			}else{
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


    	try {
        	TypedQuery<Long> typedCountQuery = entityManager.createQuery(queryCount);
    		logger.debug("assembled count query: " + typedCountQuery.unwrap(org.hibernate.query.Query.class).getQueryString());
    	}catch( Exception e ){
    		logger.debug("failed in retrieve sql query", e);
    	}

		long countStartTime = System.currentTimeMillis();

		nTotalElements = entityManager.createQuery(queryCount).getSingleResult();

		logger.debug("count getSingleResult() took {} msecs", System.currentTimeMillis() - countStartTime);

		logger.debug("buildPredicate selects {} elements in {} msecs", nTotalElements, System.currentTimeMillis() - startTime);

        return new PageImpl<>(certViewList, pageable, nTotalElements);

	}

    private static Predicate addTenantClause(CriteriaBuilder cb, User user, Root<Certificate> root, Predicate predicate) {
        predicate = cb.and(predicate, cb.or(
            cb.equal(root.get(Certificate_.tenant), user.getTenant()),
            cb.isFalse(root.get(Certificate_.endEntity)) )
        );
        return predicate;
    }

    private static CertificateView buildCertificateViewFromObjArr(ArrayList<String> colList,
                                                                  CertificateRepository certificateRepository,
                                                                  Object[] objArr) {
		CertificateView cv = new CertificateView();
        List<NamedValue> namedValueList = new ArrayList<>();
		int i = 0;
		for( String attribute: colList) {

//			logger.debug("attribute '{}' has value '{}'", attribute, objArr[i]);

			if( "id".equalsIgnoreCase(attribute)) {
				cv.setId((Long) objArr[i]);
			}else if( "tbsDigest".equalsIgnoreCase(attribute)) {
		    	cv.setTbsDigest((String) objArr[i]);
		    }else if( "subject".equalsIgnoreCase(attribute)) {
		    	cv.setSubject((String) objArr[i]);
		    }else if( "sans".equalsIgnoreCase(attribute)) {
		    	cv.setSans((String) objArr[i]);
		    }else if( "issuer".equalsIgnoreCase(attribute)) {
		    	cv.setIssuer((String) objArr[i]);
		    }else if( "type".equalsIgnoreCase(attribute)) {
		    	cv.setType((String) objArr[i]);
		    }else if( "keyLength".equalsIgnoreCase(attribute)) {
		    	cv.setKeyLength(objArr[i].toString());
		    }else if( "description".equalsIgnoreCase(attribute)) {
		    	cv.setDescription((String) objArr[i]);
            }else if( "serial".equalsIgnoreCase(attribute)) {
                cv.setSerial((String) objArr[i]);
            }else if( "serialHex".equalsIgnoreCase(attribute)) {
                cv.setSerial((String) objArr[i]);
		    }else if( "validFrom".equalsIgnoreCase(attribute)) {
		    	cv.setValidFrom((Instant) objArr[i]);
		    }else if( "validTo".equalsIgnoreCase(attribute)) {
		    	cv.setValidTo((Instant) objArr[i]);
		    }else if( "contentAddedAt".equalsIgnoreCase(attribute)) {
		    	cv.setContentAddedAt((Instant) objArr[i]);
		    }else if( "revokedSince".equalsIgnoreCase(attribute)) {
		    	cv.setRevokedSince((Instant) objArr[i]);
			}else if( "revocationReason".equalsIgnoreCase(attribute)) {
		    	cv.setRevocationReason((String) objArr[i]);
            }else if( "revoked".equalsIgnoreCase(attribute)) {
                cv.setRevoked((Boolean) objArr[i]);
            }else if( "revokedBy".equalsIgnoreCase(attribute)) {
                cv.setRevokedBy((String) objArr[i]);
            }else if( "root".equalsIgnoreCase(attribute)) {
                cv.setRoot((String) objArr[i]);
            }else if( "keyAlgorithm".equalsIgnoreCase(attribute)) {
                cv.setKeyAlgorithm((String) objArr[i]);
			}else if( "signingAlgorithm".equalsIgnoreCase(attribute)) {
		    	cv.setSigningAlgorithm((String) objArr[i]);
			}else if( "paddingAlgorithm".equalsIgnoreCase(attribute)) {
		    	cv.setPaddingAlgorithm((String) objArr[i]);
			}else if( "hashAlgorithm".equalsIgnoreCase(attribute)) {
		    	cv.setHashAlgorithm((String) objArr[i]);
            }else if( "comment".equalsIgnoreCase(attribute)) {
                cv.setComment((String) objArr[i]);
            }else if( "csrComment".equalsIgnoreCase(attribute)) {
                cv.setCsrComment((String) objArr[i]);
            }else if( "ca".equalsIgnoreCase(attribute)) {
                cv.setCa( Boolean.parseBoolean((String)objArr[i]));
            }else if( "selfSigned".equalsIgnoreCase(attribute)) {
                cv.setSelfsigned(Boolean.parseBoolean((String)objArr[i]));
            }else if( "trusted".equalsIgnoreCase(attribute)) {
                cv.setTrusted(Boolean.parseBoolean((String)objArr[i]));
            }else if( "endEntity".equalsIgnoreCase(attribute)) {
                cv.setEndEntity( Boolean.parseBoolean((String)objArr[i]));
            }else if( "chainlength".equalsIgnoreCase(attribute)) {
                cv.setChainLength( Long.parseLong ((String)objArr[i]));
            }else if( "crlurl".equalsIgnoreCase(attribute)) {
                cv.setCrlUrl((String) objArr[i]);
            }else if( "requestedBy".equalsIgnoreCase(attribute)) {
                cv.setRequestedBy( (String) objArr[i]) ;
            }else {

                logger.info("attribute '{}' from query added as additionalRestriction", attribute);

                NamedValue namedValue = new NamedValue();
                namedValue.setName(attribute);
//                namedValue.setValue(objArr[i].toString());
                namedValueList.add(namedValue);
                continue;
			}
			i++;
		}

        if(!namedValueList.isEmpty()){
            cv.setArArr(namedValueList.toArray(new NamedValue[0]));
        }else{
            Optional<Certificate> optionalCertificate = certificateRepository.findById(cv.getId());
            if( optionalCertificate.isPresent()){
                Set<CertificateAttribute> certificateAttributeList = optionalCertificate.get().getCertificateAttributes();
                for( CertificateAttribute attribute: certificateAttributeList){
                    for( NamedValue namedValue: namedValueList){
                        if( namedValue.getName().equals(attribute.getName())){
                            namedValue.setValue(attribute.getValue());
                            logger.info("retrieved attribute '{}' required by column list", attribute.getName());
                            break;
                        }
                    }
                }
            }
        }
		return cv;
	}


	/**
	 * Parse the set of selection columns and put them into a map
	 *
	 * @param parameterMap
	 * @return
	 */
	static Map<String, List<SelectionData>> getSelectionMap(Map<String, String[]> parameterMap){

		Map<String, List<SelectionData>> selectorMap = new HashMap<>();

		for( int n = 1; n < 20; n++){
			String paramNameAttribute = "attributeName_" + n;
			logger.debug("paramNameAttribute {} ", paramNameAttribute);

			if( parameterMap.containsKey(paramNameAttribute)){
				String attribute = getStringValue(parameterMap.get(paramNameAttribute));
				if( attribute.length() == 0){
	    			logger.debug("paramNameAttribute {} has no value", paramNameAttribute);
					continue;
				}
	    		String paramNameAttributeSelector = "attributeSelector_" + n;
				String attributeSelector = getStringValue(parameterMap.get(paramNameAttributeSelector));
				if( attributeSelector.length() == 0){
	    			logger.debug("paramNameAttributeSelector {} has no value", paramNameAttributeSelector);
					continue;
				}

	    		String paramNameAttributeValue = "attributeValue_" + n;
				String attributeValue = getStringValue(parameterMap.get(paramNameAttributeValue));
				if( attributeValue.length() == 0){
					if( Selector.requiresValue(attributeSelector)) {
						logger.debug("paramNameAttributeValue {} has no value", paramNameAttributeValue);
						continue;
					}
				}

				logger.debug("Attribute {} selecting by {} for value {}", attribute, attributeSelector, attributeValue);

				SelectionData selData = new SelectionData(attributeSelector, attributeValue);
				if( selectorMap.containsKey(attribute)) {
					logger.debug("adding selector to exiting list for '{}'", attribute);
					selectorMap.get(attribute).add(selData);
				}else {
					logger.debug("creating new selector list for '{}'", attribute);
					List<SelectionData> selectorList = new ArrayList<>();
					selectorList.add(selData);
					selectorMap.put(attribute,selectorList);
				}
			}else{
				break;
			}
		}
		return selectorMap;
	}

	/**
	 *
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
        Root<Certificate> root,
        CriteriaBuilder cb,
        CriteriaQuery<?> certQuery,
        final String attribute,
        final String attributeSelector,
        final String attributeValue,
        List<Selection<?>> selectionList, List<String> certificateSelectionAttributes) {

		Predicate pred = cb.conjunction();

		if( "id".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.id));
			pred = SpecificationsHelper.buildPredicateLong( attributeSelector, cb, root.<Long>get(Certificate_.id), attributeValue);

        }else if( "csrComment".equals(attribute)){
        }else if( "extUsage".equals(attribute)){
        }else if( "processingCa".equals(attribute)){
        }else if( "uploadedBy".equals(attribute)){
        }else if( "selfSigned".equals(attribute)){
            Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
            addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

            pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SELFSIGNED),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));

        }else if( "intermediate".equals(attribute)){
            Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
            addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

            pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_CA3S_INTERMEDIATE),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));

        }else if( "endEntity".equals(attribute)){
            Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
            addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

            pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_END_ENTITY),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));

        }else if( "crlurl".equals(attribute)){
            Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
            addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

            pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_CRL_URL),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));

        }else if( "chainlength".equals(attribute)){
            Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
            addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

            pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_CHAIN_LENGTH),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));

        }else if( "ca".equals(attribute)){
            Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
            addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

            pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_CA),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));

        }else if( "subject".equals(attribute)){
            addNewColumn(selectionList,root.get(Certificate_.subject));

            if( attributeValue.trim().length() > 0 ) {
                //subquery
                Subquery<CertificateAttribute> certAttSubquery = certQuery.subquery(CertificateAttribute.class);
                Root<CertificateAttribute> certAttRoot = certAttSubquery.from(CertificateAttribute.class);
                pred = cb.exists(certAttSubquery.select(certAttRoot)//subquery selection
                    .where(cb.and( cb.equal(certAttRoot.get(CertificateAttribute_.CERTIFICATE), root.get(Certificate_.ID)),
                        cb.equal(certAttRoot.get(CertificateAttribute_.NAME), CertificateAttribute.ATTRIBUTE_SUBJECT),
                        buildPredicateString( attributeSelector, cb, certAttRoot.get(CertificateAttribute_.value), attributeValue.toLowerCase()) )));
            }
        }else if( "cn".equals(attribute)){
            Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
            addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

            pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_RDN_CN),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));
		}else if( "sans".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.sans));

			if( attributeValue.trim().length() > 0 ) {
				//subquery
			    Subquery<CertificateAttribute> certAttSubquery = certQuery.subquery(CertificateAttribute.class);
			    Root<CertificateAttribute> certAttRoot = certAttSubquery.from(CertificateAttribute.class);
			    pred = cb.exists(certAttSubquery.select(certAttRoot)//subquery selection
	                     .where(cb.and( cb.equal(certAttRoot.get(CertificateAttribute_.CERTIFICATE), root.get(Certificate_.ID)),
	                    		 cb.equal(certAttRoot.get(CertificateAttribute_.NAME), CertificateAttribute.ATTRIBUTE_SAN),
                             buildPredicateString( attributeSelector, cb, certAttRoot.get(CertificateAttribute_.value), attributeValue.toLowerCase()) )));
			}
		}else if( "issuer".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.issuer));

			if( attributeValue.trim().length() > 0 ) {
				//subquery
			    Subquery<CertificateAttribute> certAttSubquery = certQuery.subquery(CertificateAttribute.class);
			    Root<CertificateAttribute> certAttRoot = certAttSubquery.from(CertificateAttribute.class);
			    pred = cb.exists(certAttSubquery.select(certAttRoot)//subquery selection
	                     .where(cb.and( cb.equal(certAttRoot.get(CertificateAttribute_.CERTIFICATE), root.get(Certificate_.ID)),
	                    		 cb.equal(certAttRoot.get(CertificateAttribute_.NAME), CertificateAttribute.ATTRIBUTE_ISSUER),
                             buildPredicateString( attributeSelector, cb, certAttRoot.get(CertificateAttribute_.value), attributeValue.toLowerCase()) )));
			}
		}else if( "root".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.root));

			if( attributeValue.trim().length() > 0 ) {
				//subquery
			    Subquery<CertificateAttribute> certAttSubquery = certQuery.subquery(CertificateAttribute.class);
			    Root<CertificateAttribute> certAttRoot = certAttSubquery.from(CertificateAttribute.class);
			    pred = cb.exists(certAttSubquery.select(certAttRoot)//subquery selection
	                     .where(cb.and( cb.equal(certAttRoot.get(CertificateAttribute_.CERTIFICATE), root.get(Certificate_.ID)),
	                    		 cb.equal(certAttRoot.get(CertificateAttribute_.NAME), CertificateAttribute.ATTRIBUTE_ROOT),
                             buildPredicateString( attributeSelector, cb, certAttRoot.get(CertificateAttribute_.value), attributeValue.toLowerCase()) )));
			}
		}else if( "san".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
			addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

			pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SAN),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));
		}else if( "usage".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
			addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

			pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_USAGE),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));
		}else if( "ski".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
			addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

			pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SKI),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue));

        }else if( "fingerprint".equals(attribute)){
            Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
            addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));

            pred = cb.and( cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_FINGERPRINT_SHA1),
                buildPredicateString( attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));

        }else if( "pkiLevel".equals(attribute)){
            addNewColumn(selectionList,root.get(Certificate_.root));

            String attrName = CertificateAttribute.ATTRIBUTE_END_ENTITY;
            if( "root".equalsIgnoreCase(attributeValue)){
                attrName = CertificateAttribute.ATTRIBUTE_SELFSIGNED;
            }else if( "intermediate".equalsIgnoreCase(attributeValue)) {
                attrName = CertificateAttribute.ATTRIBUTE_CA;
            }

            //subquery
            Subquery<CertificateAttribute> certAttSubquery = certQuery.subquery(CertificateAttribute.class);
            Root<CertificateAttribute> certAttRoot = certAttSubquery.from(CertificateAttribute.class);
            Predicate predExists =
            cb.exists(certAttSubquery.select(certAttRoot)//subquery selection
                .where(cb.and( cb.equal(certAttRoot.get(CertificateAttribute_.CERTIFICATE), root.get(Certificate_.ID)),
                    cb.equal(certAttRoot.get(CertificateAttribute_.NAME), attrName),
                    buildPredicateString( Selector.EQUAL.toString(), cb, certAttRoot.get(CertificateAttribute_.value), "true") )));

            pred = predExists;
            if(attributeSelector.equals(Selector.NOT_EQUAL.toString())){
                pred = cb.not(predExists);
            }
        }else if( "hashAlgorithm".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.hashingAlgorithm));
			pred = buildPredicateString( attributeSelector, cb, root.get(Certificate_.hashingAlgorithm), attributeValue);

		}else if( "type".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.type));
			pred = buildPredicateString( attributeSelector, cb, root.get(Certificate_.type), attributeValue);

		}else if( "signingAlgorithm".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.signingAlgorithm));
			pred = buildPredicateString( attributeSelector, cb, root.get(Certificate_.signingAlgorithm), attributeValue);

		}else if( "paddingAlgorithm".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.paddingAlgorithm));
			pred = buildPredicateString( attributeSelector, cb, root.get(Certificate_.paddingAlgorithm), attributeValue);

		}else if( "keyAlgorithm".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.keyAlgorithm));
			pred = buildPredicateString( attributeSelector, cb, root.get(Certificate_.keyAlgorithm), attributeValue);

		}else if( "keyLength".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.keyLength));
			pred = SpecificationsHelper.buildPredicateInteger( attributeSelector, cb, root.get(Certificate_.keyLength), attributeValue);

		}else if( "serial".equals(attribute) || "serialHex".equals(attribute) ){

			addNewColumn(selectionList,root.get(Certificate_.serial));

            if (attributeSelector != null) {

                String decSerial = attributeValue;

                if (Selector.HEX.toString().equalsIgnoreCase(attributeSelector)) {
                    try {
                        BigInteger serialBI = new BigInteger(attributeValue.replaceAll(" ", ""), 16);
                        decSerial = serialBI.toString();
                    } catch (NumberFormatException nfe) {
                        // not a hex number, ignore the problem
                    }
                }

                String paddedSerial = CertificateUtil.getPaddedSerial(decSerial);
                logger.debug("serial used for search {} ", paddedSerial);

                Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
                pred = cb.and(cb.equal(attJoin.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SERIAL_PADDED),
                    cb.equal(attJoin.get(CertificateAttribute_.value), paddedSerial));
            }
		}else if( "validFrom".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.validFrom));
			pred = SpecificationsHelper.buildDatePredicate( attributeSelector, cb, root.get(Certificate_.validFrom), attributeValue);
		}else if( "validTo".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.validTo));
			pred = SpecificationsHelper.buildDatePredicate( attributeSelector, cb, root.get(Certificate_.validTo), attributeValue);
		}else if( "active".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.active));
			pred = SpecificationsHelper.buildBooleanPredicate( attributeSelector, cb, root.get(Certificate_.active), attributeValue);
        }else if( "revoked".equals(attribute)){
            addNewColumn(selectionList,root.get(Certificate_.revoked));
            pred = SpecificationsHelper.buildBooleanPredicate( attributeSelector, cb, root.get(Certificate_.revoked), attributeValue);

        }else if( "trusted".equals(attribute)){
            addNewColumn(selectionList,root.get(Certificate_.trusted));
            pred = SpecificationsHelper.buildBooleanPredicate( attributeSelector, cb, root.get(Certificate_.trusted), attributeValue);

        }else if( "revokedSince".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.revokedSince));
			pred = SpecificationsHelper.buildDatePredicate( attributeSelector, cb, root.get(Certificate_.revokedSince), attributeValue);

		}else if( "revocationReason".equals(attribute)){
			addNewColumn(selectionList,root.get(Certificate_.revocationReason));
			pred = buildPredicateString( attributeSelector, cb, root.get(Certificate_.revocationReason), attributeValue);

        }else if( "revokedBy".equals(attribute)){
            Join<Certificate, CertificateAttribute> attJoinRequestor = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
            addNewColumn(selectionList,attJoinRequestor.get(CertificateAttribute_.value));

            pred = cb.and( cb.equal(attJoinRequestor.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_REVOKED_BY),
                buildPredicateString( attributeSelector, cb, attJoinRequestor.get(CertificateAttribute_.value), attributeValue));

        }else if( "requestedBy".equals(attribute)){
            Join<Certificate, CertificateAttribute> attJoinRequestor = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
            addNewColumn(selectionList,attJoinRequestor.get(CertificateAttribute_.value));

            pred = cb.and( cb.equal(attJoinRequestor.get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_REQUESTED_BY),
                buildPredicateString( attributeSelector, cb, attJoinRequestor.get(CertificateAttribute_.value), attributeValue));
        }else if( "comment".equals(attribute)){
            Join<Certificate, CertificateComment> attJoin = root.join(Certificate_.comment, JoinType.LEFT);
            addNewColumn(selectionList,attJoin.get(CertificateComment_.comment));

            pred = buildPredicateString( attributeSelector, cb, attJoin.get(CertificateComment_.comment), attributeValue.toLowerCase());
        }else{

            if( certificateSelectionAttributes.contains(attribute) ){
                // handle ARAs
//                addNewColumn(selectionList,attJoin.get(CertificateAttribute_.value));
                if (attributeSelector != null) {
                    Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
                    pred = cb.and(cb.equal(attJoin.get(CertificateAttribute_.name), CsrAttribute.ARA_PREFIX + attribute),
                        buildPredicateString(attributeSelector, cb, attJoin.get(CertificateAttribute_.value), attributeValue.toLowerCase()));
                }
            }else {
                logger.warn("fall-thru clause adding 'true' condition for {} ", attribute);
            }
		}
		return pred;
	}

}


class SelectionData{

	String selector;
	String value;

	public SelectionData( String selector, String value) {
		this.selector = selector;
		this.value = value;
	}
}
