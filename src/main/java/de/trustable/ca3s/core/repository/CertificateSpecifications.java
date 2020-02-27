package de.trustable.ca3s.core.repository;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.CertificateAttribute_;
import de.trustable.ca3s.core.domain.Certificate_;
import de.trustable.ca3s.core.service.dto.CertificateView;
import de.trustable.ca3s.core.service.dto.Selector;
import de.trustable.ca3s.core.service.util.DateUtil;


public final class CertificateSpecifications {

	static Logger logger = LoggerFactory.getLogger(CertificateSpecifications.class);

	static final String SORT = "sort";
	static final String ORDER = "order";
	
    private CertificateSpecifications() {}
    
    public static Specification<Certificate> subjectOrIssuer(String searchTerm) {
        return (root, query, cb) -> {
            String containsLikePattern = getContainsLikePattern(searchTerm);
            return cb.or(
                    cb.like(cb.lower(root.<String>get(Certificate_.subject)), containsLikePattern),
                    cb.like(cb.lower(root.<String>get(Certificate_.issuer)), containsLikePattern)
            );
        };
    }
 
    private static String getContainsLikePattern(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return "%";
        }
        else {
            return searchTerm.toLowerCase() + "%";
        }
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
    
	public static Page<CertificateView> handleQueryParamsCertificateView(EntityManager entityManager, 
			CriteriaBuilder cb, 
			Map<String, String[]> parameterMap) {
		
		CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
		Root<Certificate> root = query.from(Certificate.class);

		String sortCol = getStringValue(parameterMap.get("sort"), "id").trim();
		Selection<?> orderSelection = null;
		String orderDirection = getStringValue(parameterMap.get("order"), "asc");
		
    	int pageOffset = getIntValue( parameterMap.get("offset"), 0 );
    	int pagesize = getIntValue( parameterMap.get("limit"), 20 );
				
		ArrayList<Selection<?>> selectionList = new ArrayList<Selection<?>>();
		ArrayList<String> colList = new ArrayList<String>();
		
		Map<String, SelectionData> selectionMap = getSelectionMap(parameterMap);

    	Predicate pred = null;

    	String[] columnArr = new String[0];
		if( parameterMap.containsKey("filter")){
			String[] paramArr = parameterMap.get("filter");
			if( paramArr.length > 0) {
				columnArr = paramArr[0].split(",");
			}
		}
		
		// walk thru all requested columns
		for( String col: columnArr) {
			colList.add(col);
			
	    	Predicate localPred;
			if( selectionMap.containsKey(col) ) {
				SelectionData selDate = selectionMap.get(col);
				logger.debug("buildPredicate for '{}', selector '{}', value '{}' ", col, selDate.selector, selDate.value);

				localPred = buildPredicate( root, 
						cb, 
						col, 
						selDate.selector, 
						selDate.value,
						selectionList);
			}else {
				logger.debug("buildPredicate for '{}' without selector ", col );
				localPred = buildPredicate( root, 
					cb, 
					col, 
					null, 
					"",
					selectionList);
			}

			// chain all the predicates
			if( pred == null ){
				pred = localPred;
			}else{
				pred = cb.and(pred, localPred);
			}
			
			// if this is the sorting columns, save the selection
			if( col.equals(sortCol)) {
				orderSelection = selectionList.get(selectionList.size()-1);
			}
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
    	query.distinct(true);
		
    	TypedQuery<Object[]> typedQuery = entityManager.createQuery(query);
    	typedQuery.setMaxResults(pagesize);
    	typedQuery.setFirstResult(pageOffset);
    	
    	try {
    		logger.debug("assembled query: " + typedQuery.unwrap(org.hibernate.query.Query.class).getQueryString());
    	}catch( Exception e ){
    		logger.debug("failed in retrieve sql query", e);
    	}
    	
    	// submit the query
    	List<Object[]> listResponse = typedQuery.getResultList();
    	
    	// use the result set to fill the response object
    	List<CertificateView> certViewList = new ArrayList<CertificateView>();
    	for( Object[] objArr: listResponse) {

			logger.debug("objArr len {}, colList len {}", objArr.length, colList.size());

    		CertificateView cv = new CertificateView();
    		int i = 0;
        	for( String attribute: colList) {

				logger.debug("attribute '{}' has value '{}'", attribute, objArr[i]);

            	if( "id".equalsIgnoreCase(attribute)) {
            		cv.setId((Long) objArr[i]);
            	}else if( "tbsDigest".equalsIgnoreCase(attribute)) {
                	cv.setTbsDigest((String) objArr[i]);	
                }else if( "subject".equalsIgnoreCase(attribute)) {
                	cv.setSubject((String) objArr[i]);	
                }else if( "issuer".equalsIgnoreCase(attribute)) {
                	cv.setIssuer((String) objArr[i]);	
                }else if( "type".equalsIgnoreCase(attribute)) {
                	cv.setType((String) objArr[i]);	
                }else if( "keyLength".equalsIgnoreCase(attribute)) {
                	cv.setKeyLength((String) objArr[i]);	
                }else if( "description".equalsIgnoreCase(attribute)) {
                	cv.setDescription((String) objArr[i]);	
                }else if( "serial".equalsIgnoreCase(attribute)) {
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
				}else if( "signingAlgorithm".equalsIgnoreCase(attribute)) {
                	cv.setSigningAlgorithm((String) objArr[i]);	
				}else if( "paddingAlgorithm".equalsIgnoreCase(attribute)) {
                	cv.setPaddingAlgorithm((String) objArr[i]);	
				}else if( "hashAlgorithm".equalsIgnoreCase(attribute)) {
                	cv.setHashAlgorithm((String) objArr[i]);	
				}else {
					logger.warn("unexpected attribute '{}' from query", attribute);
				}
            	i++;
        	}
        	
        	certViewList.add(cv);
    	}

    	
        Pageable pageable = PageRequest.of(pageOffset / pagesize, pagesize, sortDir, sortCol);
        
        Long nTotalElements = 1000L;
        
/*        
        CriteriaQuery<Long> queryCount = cb.createQuery(Long.class);
        Root<Certificate> iRoot = queryCount.from(Certificate.class);
        queryCount.select(cb.count(iRoot));

    	Predicate predCount = null;

		// walk thru all requested columns
		for( String col: columnArr) {
			colList.add(col);
			
	    	Predicate localPred;
			if( selectionMap.containsKey(col) ) {
				SelectionData selDate = selectionMap.get(col);
				logger.debug("buildPredicate for '{}', selector '{}', value '{}' ", col, selDate.selector, selDate.value);

				localPred = buildPredicate( root, 
						cb, 
						col, 
						selDate.selector, 
						selDate.value,
						selectionList);
			}else {
				logger.debug("buildPredicate for '{}' without selector ", col );
				localPred = buildPredicate( root, 
					cb, 
					col, 
					null, 
					"",
					selectionList);
			}

			// chain all the predicates
			if( predCount == null ){
				predCount = localPred;
			}else{
				predCount = cb.and(pred, localPred);
			}
		}

		queryCount.where(predCount);

		nTotalElements = entityManager.createQuery(queryCount).getSingleResult();
		logger.debug("buildPredicate selects {} elements ", nTotalElements);
*/        
        return new PageImpl<CertificateView>(certViewList, pageable, nTotalElements);

	}


	/**
	 * Parse the set of selection columns and put them into a map
	 * 
	 * @param parameterMap
	 * @return
	 */
	static Map<String, SelectionData> getSelectionMap(Map<String, String[]> parameterMap){
	
		Map<String, SelectionData> selectorMap = new HashMap<String, SelectionData>();
		
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
	    			logger.debug("paramNameAttributeValue {} has no value", paramNameAttributeValue);
					continue;
				}
				
				logger.debug("Attribute {} selecting by {} for value {}", attribute, attributeSelector, attributeValue);
	
				selectorMap.put(attribute,new SelectionData(attributeSelector, attributeValue));
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
	 * @return
	 */
	private static Predicate buildPredicate(
			Root<Certificate> root, 
			CriteriaBuilder cb, 
			final String attribute, 
			final String attributeSelector, 
			final String attributeValue,
			List<Selection<?>> selectionList) {
	
		Predicate pred = cb.conjunction();
	
		if( "id".equals(attribute)){
			selectionList.add(root.get(Certificate_.id));
			pred = buildPredicateLong( attributeSelector, cb, root.<Long>get(Certificate_.id), attributeValue);
		
		}else if( "subject".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			selectionList.add(root.get(Certificate_.subject));
			/*
			Subquery<CertificateAttribute> attributesSubquery = query.subquery(CertificateAttribute.class);
			Root<CertificateAttribute> certAttributes = attributesSubquery.from(CertificateAttribute.class);
			attributesSubquery.select(certAttributes)//subquery selection
			                  .where(cb.equal(jobInfo.get(JobInfo_.jobName),
			                          employee.get(Employee_.job)));//subquery restriction
			   //main query selection
			   employeeQuery.select(employee)
			                .where(criteriaBuilder.exists(jobInfoSubquery));
*/
		
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SUBJECT),
			buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue.toLowerCase()));
		
		}else if( "san".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
			selectionList.add(attJoin.get(CertificateAttribute_.value));
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SAN),
			buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue.toLowerCase()));
			
		}else if( "issuer".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			selectionList.add(root.get(Certificate_.issuer));
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_ISSUER),
			buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue.toLowerCase()));
			
		}else if( "ski".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
			selectionList.add(attJoin.get(CertificateAttribute_.value));
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SKI),
			buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
			
		}else if( "fingerprint".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			selectionList.add(attJoin.get(CertificateAttribute_.value));
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_FINGERPRINT),
			buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
				
		}else if( "hashAlgorithm".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
			selectionList.add(attJoin.get(CertificateAttribute_.value));
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_HASH_ALGO),
			buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
		
		}else if( "type".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			selectionList.add(attJoin.get(CertificateAttribute_.value));
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_TYPE),
			buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
		
		}else if( "signingAlgorithm".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
			selectionList.add(attJoin.get(CertificateAttribute_.value));
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SIGNATURE_ALGO),
			buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
		
		}else if( "paddingAlgorithm".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
			selectionList.add(attJoin.get(CertificateAttribute_.value));
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_PADDING_ALGO),
			buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
		
		}else if( "keyLength".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
			selectionList.add(attJoin.get(CertificateAttribute_.value));
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_KEY_LENGTH),
			buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
		
		}else if( "serial".equals(attribute)){
	
			selectionList.add(root.get(Certificate_.serial));
	
			String decSerial = attributeValue;
			if( attributeValue.startsWith("#")){
				decSerial = attributeValue.substring(1);
			} else if( attributeValue.startsWith("$")){
				BigInteger serialBI = new BigInteger( attributeValue.substring(1).replaceAll(" ", ""), 16);
				decSerial = serialBI.toString();
			}
			
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes, JoinType.LEFT);
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SERIAL_PADDED),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), de.trustable.util.CryptoUtil.getPaddedSerial(decSerial)));
			
		}else if( "validFrom".equals(attribute)){
			selectionList.add(root.get(Certificate_.validFrom));
			pred = buildDatePredicate( attributeSelector, cb, root.<Instant>get(Certificate_.validFrom), attributeValue);
		}else if( "validTo".equals(attribute)){
			selectionList.add(root.get(Certificate_.validTo));
			pred = buildDatePredicate( attributeSelector, cb, root.<Instant>get(Certificate_.validTo), attributeValue);
		}else if( "revoked".equals(attribute)){
			selectionList.add(root.get(Certificate_.revoked));
			pred = buildBooleanPredicate( attributeSelector, cb, root.<Boolean>get(Certificate_.revoked), attributeValue);
			
		}else if( "revokedSince".equals(attribute)){
			selectionList.add(root.get(Certificate_.revokedSince));
			pred = buildDatePredicate( attributeSelector, cb, root.<Instant>get(Certificate_.revokedSince), attributeValue);
	
		}else{
			logger.warn("fall-thru clause adding 'true' condition for {} ", attribute);
		}
		return pred;
	}

	
	private static Predicate buildPredicate(String attributeSelector, CriteriaBuilder cb, Expression<String> expression, String value) {
		
		if( attributeSelector == null) {
			return cb.conjunction();
		}
		
		if( Selector.EQUALS.toString().equals(attributeSelector)){
			logger.debug("buildPredicate equal ('{}') for value '{}'", attributeSelector, value);
			return cb.equal(expression, value);
		}else if( Selector.ON.toString().equals(attributeSelector)){
			logger.debug("buildPredicate on ('{}') for value '{}'", attributeSelector, value);
			return cb.equal(expression, value);
		}else if( Selector.LIKE.toString().equals(attributeSelector)){
			logger.debug("buildPredicate like ('{}') for value '{}'", attributeSelector, getContainsLikePattern(value));
			return cb.like(expression, getContainsLikePattern(value));
		}else if( Selector.NOTLIKE.toString().equals(attributeSelector)){
			logger.debug("buildPredicate not like ('{}') for value '{}'", attributeSelector, getContainsLikePattern(value));
			return cb.like(expression, getContainsLikePattern(value)).not();
		}else if( Selector.LESSTHAN.toString().equals(attributeSelector)){
			logger.debug("buildPredicate lessThan ('{}') for value '{}'", attributeSelector, value);
				return cb.lessThan(expression, value);
		}else if( Selector.BEFORE.toString().equals(attributeSelector)){
			logger.debug("buildPredicate before ('{}') for value '{}'", attributeSelector, value);
				return cb.lessThan(expression, value);
		}else if( Selector.GREATERTHAN.toString().equals(attributeSelector)){
			logger.debug("buildPredicate greaterThan ('{}') for value '{}'", attributeSelector, value);
				return cb.greaterThan(expression, value);
		}else if( Selector.AFTER.toString().equals(attributeSelector)){
			logger.debug("buildPredicate after ('{}') for value '{}'", attributeSelector, value);
				return cb.greaterThan(expression, value);
		}else{
			logger.debug("buildPredicate defaults to equals ('{}') for value '{}'", attributeSelector, value);
			return cb.equal(expression, value);
		}
	}

	private static Predicate buildPredicateLong(String attributeSelector, CriteriaBuilder cb, Expression<Long> expression, String value) {
		
		if( attributeSelector == null) {
			return cb.conjunction();
		}
		
		long lValue = Long.parseLong(value);
		
		if( Selector.EQUALS.toString().equals(attributeSelector)){
			logger.debug("buildPredicate equal ('{}') for value '{}'", attributeSelector, lValue);
			return cb.equal(expression, lValue);
		}else if( Selector.LESSTHAN.toString().equals(attributeSelector)){
			logger.debug("buildPredicate lessThan ('{}') for value '{}'", attributeSelector, lValue);
				return cb.lessThan(expression, lValue);
		}else if( Selector.GREATERTHAN.toString().equals(attributeSelector)){
			logger.debug("buildPredicate greaterThan ('{}') for value '{}'", attributeSelector, lValue);
				return cb.greaterThan(expression, lValue);
		}else{
			logger.debug("buildPredicate defaults to equals ('{}') for value '{}'", attributeSelector, lValue);
			return cb.equal(expression, lValue);
		}
	}

	private static Predicate buildBooleanPredicate(String attributeSelector, CriteriaBuilder cb, Expression<Boolean> expression, String value) {

		if( attributeSelector == null) {
			return cb.conjunction();
		}

		logger.debug("buildBooleanPredicatedefaults to equals ('{}') ", attributeSelector);
		
		if( Selector.ISTRUE.toString().equals(attributeSelector) ) {
			return cb.equal(expression, Boolean.TRUE);
		} else {
			return cb.equal(expression, Boolean.FALSE);
		}
	}

	
	private static Predicate buildDatePredicate(String attributeSelector, CriteriaBuilder cb, Expression<Instant> expression, String value) {
		
		if( attributeSelector == null) {
			return cb.conjunction();
		}

		Instant dateTime;
		try{

			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				dateTime = DateUtil.asInstant(dateFormat.parse(value));
			} catch(Exception ex ){
				dateTime = Instant.ofEpochMilli(Long.parseLong(value));
			}
			
			if( Selector.ON.toString().equals(attributeSelector)){
				logger.debug("buildDatePredicate on ('{}') for value {}", attributeSelector, dateTime);
				return cb.equal(expression, dateTime);
			}else if( Selector.BEFORE.toString().equals(attributeSelector)){
				logger.debug("buildDatePredicate before ('{}') for value {}", attributeSelector, dateTime);
				return cb.lessThanOrEqualTo(expression, dateTime);
			}else if( Selector.AFTER.toString().equals(attributeSelector)){
				logger.debug("buildDatePredicate after ('{}') for value {}", attributeSelector, dateTime);
				return cb.greaterThanOrEqualTo(expression, dateTime);
			}else{
				logger.debug("buildDatePredicate defaults to equals ('{}') for value {}", attributeSelector, dateTime);
				return cb.equal(expression, dateTime);
			}
		} catch(Exception ex ){
			logger.debug("parsing date ... ", ex);
			throw ex;
		}
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