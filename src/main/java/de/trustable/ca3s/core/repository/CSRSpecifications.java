package de.trustable.ca3s.core.repository;

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
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.CSR_;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.Certificate_;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.CsrAttribute_;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.Pipeline_;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.service.dto.CSRView;
import de.trustable.ca3s.core.service.dto.Selector;
import de.trustable.ca3s.core.service.util.DateUtil;


public final class CSRSpecifications {

	static Logger logger = LoggerFactory.getLogger(CSRSpecifications.class);

	static final String SORT = "sort";
	static final String ORDER = "order";
	
    private CSRSpecifications() {}
    
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
    
	public static Page<CSRView> handleQueryParamsCertificateView(EntityManager entityManager, 
			CriteriaBuilder cb, 
			Map<String, String[]> parameterMap) {
		
		CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
		Root<CSR> root = query.from(CSR.class);

		String sortCol = getStringValue(parameterMap.get("sort"), "id").trim();
		Selection<?> orderSelection = null;
		String orderDirection = getStringValue(parameterMap.get("order"), "asc");
		
    	int pageOffset = getIntValue( parameterMap.get("offset"), 0 );
    	int pagesize = getIntValue( parameterMap.get("limit"), 20 );
				
		ArrayList<Selection<?>> selectionList = new ArrayList<Selection<?>>();
		ArrayList<String> colList = new ArrayList<String>();
		
		Map<String, List<SelectionData>> selectionMap = getSelectionMap(parameterMap);

		// retrieve all the required columns
		// 'filter' is a bit misleading, here...
    	String[] columnArr = new String[0];
		if( parameterMap.containsKey("filter")){
			String[] paramArr = parameterMap.get("filter");
			if( paramArr.length > 0) {
				columnArr = paramArr[0].split(",");
			}
		}
		
		
		// collect all selectors in a list
		List<Predicate> predList = new ArrayList<Predicate>();
		
		// walk thru all requested columns
		for( String col: columnArr) {
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
							selectionList));
				}
			}else {
				logger.debug("buildPredicate for '{}' without selector ", col );
				predList.add( buildPredicate( root, 
					cb, 
					query,
					col, 
					null, 
					"",
					selectionList));
			}

			
			// if this is the sorting columns, save the selection
			if( col.equals(sortCol)) {
				orderSelection = selectionList.get(selectionList.size()-1);
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
    	
    	// submit the query
    	List<Object[]> listResponse = typedQuery.getResultList();
    	
    	// use the result set to fill the response object
    	List<CSRView> certViewList = new ArrayList<CSRView>();
    	for( Object[] objArr: listResponse) {

    		if( logger.isDebugEnabled() && (objArr.length != colList.size())) {
    			logger.debug("objArr len {}, colList len {}", objArr.length, colList.size());
    		}
    		
    		CSRView cv = buildCSRViewFromObjArr(colList, objArr);
        	
        	certViewList.add(cv);
    	}

    	// start again to retrieve the row count
        Pageable pageable = PageRequest.of(pageOffset / pagesize, pagesize, sortDir, sortCol);
        
        Long nTotalElements = 1000L;
                
        CriteriaQuery<Long> queryCount = cb.createQuery(Long.class);
        Root<CSR> iRoot = queryCount.from(CSR.class);

		List<Predicate> predCountList = new ArrayList<Predicate>();

		ArrayList<Selection<?>> selectionListCount = new ArrayList<Selection<?>>();

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
							selectionListCount));
				}
			}else {
				logger.debug("buildPredicate for '{}' without selector ", col );
				predCountList.add( buildPredicate( iRoot, 
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
    	for( Predicate predPart: predCountList) {
			// chain all the predicates
			if( predCount == null ){
				predCount = predPart;
			}else{
				predCount = cb.and(predCount, predPart);
			}
    	}
		
        queryCount.select(cb.count(iRoot));
      
		queryCount.where(predCount);

		nTotalElements = entityManager.createQuery(queryCount).getSingleResult();
		logger.debug("buildPredicate selects {} elements ", nTotalElements);
        
        return new PageImpl<CSRView>(certViewList, pageable, nTotalElements);

	}

	private static CSRView buildCSRViewFromObjArr(ArrayList<String> colList, Object[] objArr) {
		CSRView cv = new CSRView();
		int i = 0;

		
		for( String attribute: colList) {

			if( i >= objArr.length) {
				logger.debug("attribute '{}' exceeds objArr with #{} elements ", attribute, objArr.length);
				continue;
			}
			logger.debug("attribute '{}' has value '{}'", attribute, objArr[i]);

			if( "id".equalsIgnoreCase(attribute)) {
				cv.setId((Long) objArr[i]);
			}else if( "certificateId".equalsIgnoreCase(attribute)) {
		    	cv.setCertificateId((Long) objArr[i]);	
		    }else if( "status".equalsIgnoreCase(attribute)) {
		    	cv.setStatus((CsrStatus) objArr[i]); 
		    }else if( "subject".equalsIgnoreCase(attribute)) {
		    	cv.setSubject((String) objArr[i]);	
		    }else if( "sans".equalsIgnoreCase(attribute)) {
		    	cv.setSans((String) objArr[i]);	
		    }else if( "publicKeyAlgorithm".equalsIgnoreCase(attribute)) {
		    	cv.setPublicKeyAlgorithm((String) objArr[i]);	
		    }else if( "signingAlgorithm".equalsIgnoreCase(attribute)) {
		    	cv.setSigningAlgorithm((String) objArr[i]);	
		    }else if( "keyLength".equalsIgnoreCase(attribute)) {
		    	cv.setKeyLength(objArr[i].toString());	
		    }else if( "x509KeySpec".equalsIgnoreCase(attribute)) {
		    	cv.setX509KeySpec((String) objArr[i]);	
		    }else if( "requestedBy".equalsIgnoreCase(attribute)) {
		    	cv.setRequestedBy((String) objArr[i]);	
		    }else if( "processingCA".equalsIgnoreCase(attribute)) {
		    	cv.setProcessingCA((String) objArr[i]);	
		    }else if( "pipelineName".equalsIgnoreCase(attribute)) {
		    	cv.setPipelineName((String) objArr[i]);	
		    }else if( "pipelineType".equalsIgnoreCase(attribute)) {
		    	cv.setPipelineType((PipelineType) objArr[i]);	
		    }else if( "requestedOn".equalsIgnoreCase(attribute)) {
		    	cv.setRequestedOn((Instant) objArr[i]);			    	
		    }else if( "rejectedOn".equalsIgnoreCase(attribute)) {
		    	cv.setRejectedOn((Instant) objArr[i]);			    	
		    }else if( "rejectionReason".equalsIgnoreCase(attribute)) {
		    	cv.setRejectionReason((String) objArr[i]);	
			}else {
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
	static Map<String, List<SelectionData>> getSelectionMap(Map<String, String[]> parameterMap){
	
		Map<String, List<SelectionData>> selectorMap = new HashMap<String, List<SelectionData>>();
		
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
					List<SelectionData> selectorList = new ArrayList<SelectionData>();
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
	 * @return
	 */
	private static Predicate buildPredicate(
			Root<CSR> root, 
			CriteriaBuilder cb, 
			CriteriaQuery<?> csrQuery,
			final String attribute, 
			final String attributeSelector, 
			final String attributeValue,
			List<Selection<?>> selectionList) {
	
		Predicate pred = cb.conjunction();

		if( "id".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.id));
			pred = buildPredicateLong( attributeSelector, cb, root.<Long>get(CSR_.id), attributeValue);
			
		}else if( "status".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.status));
			if( attributeValue.trim().length() > 0 ) {
				pred = buildPredicateCsrStatus( attributeSelector, cb, root.<CsrStatus>get(CSR_.status), attributeValue);
			}
		}else if( "certificateId".equals(attribute)){
			Join<CSR, Certificate> certJoin = root.join(CSR_.certificate, JoinType.LEFT);
			addNewColumn(selectionList,certJoin.get(Certificate_.id));
			
		}else if( "subject".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.subject));
		
			if( attributeValue.trim().length() > 0 ) {
				//subquery
			    Subquery<CsrAttribute> csrAttSubquery = csrQuery.subquery(CsrAttribute.class);
			    Root<CsrAttribute> csrAttRoot = csrAttSubquery.from(CsrAttribute.class);
			    pred = cb.exists(csrAttSubquery.select(csrAttRoot)//subquery selection
	                     .where(cb.and( cb.equal(csrAttRoot.get(CsrAttribute_.CSR), root.get(CSR_.ID)),
	                    		 cb.equal(csrAttRoot.get(CsrAttribute_.NAME), CsrAttribute.ATTRIBUTE_SUBJECT),
	                    		 buildPredicate( attributeSelector, cb, csrAttRoot.<String>get(CsrAttribute_.value), attributeValue.toLowerCase()) )));
			}
		}else if( "sans".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.sans));
		
			if( attributeValue.trim().length() > 0 ) {
				//subquery
			    Subquery<CsrAttribute> csrAttSubquery = csrQuery.subquery(CsrAttribute.class);
			    Root<CsrAttribute> csrAttRoot = csrAttSubquery.from(CsrAttribute.class);
			    pred = cb.exists(csrAttSubquery.select(csrAttRoot)//subquery selection
	                     .where(cb.and( cb.equal(csrAttRoot.get(CsrAttribute_.CSR), root.get(CSR_.ID)),
	                    		 cb.equal(csrAttRoot.get(CsrAttribute_.NAME), CsrAttribute.ATTRIBUTE_SAN),
	                    		 buildPredicate( attributeSelector, cb, csrAttRoot.<String>get(CsrAttribute_.value), attributeValue.toLowerCase()) )));
			}
		}else if( "publicKeyAlgorithm".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.publicKeyAlgorithm));
			if( attributeValue.trim().length() > 0 ) {
				pred = buildPredicate( attributeSelector, cb, root.<String>get(CSR_.publicKeyAlgorithm), attributeValue);
			}
			
		}else if( "signingAlgorithm".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.signingAlgorithm));
			if( attributeValue.trim().length() > 0 ) {
				pred = buildPredicate( attributeSelector, cb, root.<String>get(CSR_.signingAlgorithm), attributeValue);
			}
			
		}else if( "x509KeySpec".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.x509KeySpec));
			if( attributeValue.trim().length() > 0 ) {
				pred = buildPredicate( attributeSelector, cb, root.<String>get(CSR_.x509KeySpec), attributeValue);
			}
			
		}else if( "requestedBy".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.requestedBy));
			if( attributeValue.trim().length() > 0 ) {
				pred = buildPredicate( attributeSelector, cb, root.<String>get(CSR_.requestedBy), attributeValue);
			}

/*			
		}else if( "processingCA".equals(attribute)){
			Join<CSR, Pipeline> certJoin = root.join(CSR_.pipeline, JoinType.LEFT);
			addNewColumn(selectionList,certJoin.get(Pipeline_.CA_CONNECTOR));
*/			
		}else if( "pipelineName".equals(attribute)){
			Join<CSR, Pipeline> certJoin = root.join(CSR_.pipeline, JoinType.LEFT);
			addNewColumn(selectionList,certJoin.get(Pipeline_.name));
			
		}else if( "keyLength".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.keyLength));
			if( attributeValue.trim().length() > 0 ) {
				pred = buildPredicateInteger( attributeSelector, cb, root.<Integer>get(CSR_.keyLength), attributeValue);
			}
		}else if( "requestedOn".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.requestedOn));
			if( attributeValue.trim().length() > 0 ) {
				pred = buildDatePredicate( attributeSelector, cb, root.<Instant>get(CSR_.requestedOn), attributeValue);
			}
		}else if( "rejectedOn".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.rejectedOn));
			if( attributeValue.trim().length() > 0 ) {
				pred = buildDatePredicate( attributeSelector, cb, root.<Instant>get(CSR_.rejectedOn), attributeValue);
			}
		}else if( "rejectionReason".equals(attribute)){
			addNewColumn(selectionList,root.get(CSR_.rejectionReason));
			if( attributeValue.trim().length() > 0 ) {
				pred = buildPredicate( attributeSelector, cb, root.<String>get(CSR_.rejectionReason), attributeValue);
			}
		}else{
			logger.warn("fall-thru clause adding 'true' condition for {} ", attribute);
		}
		return pred;
	}

	private static Predicate buildPredicateCsrStatus(String attributeSelector, CriteriaBuilder cb, Path<CsrStatus> path,	String attributeValue) {
		if( attributeSelector == null) {
			return cb.conjunction();
		}
		
		CsrStatus csrStatus = CsrStatus.valueOf(attributeValue);
		
		if( Selector.EQUAL.toString().equals(attributeSelector)){
			logger.debug("buildPredicateCsrStatus equal ('{}') for value '{}'", attributeSelector, csrStatus);
			return cb.equal(path, csrStatus);
		}else if( Selector.NOT_EQUAL.toString().equals(attributeSelector)){
			logger.debug("buildPredicateCsrStatus notEqual ('{}') for value '{}'", attributeSelector, csrStatus);
			return cb.notEqual(path, csrStatus);
		}else{
			logger.debug("buildPredicateCsrStatus defaults to equals ('{}') for value '{}'", attributeSelector, csrStatus);
			return cb.equal(path, csrStatus);
		}
		
	}

	private static void addNewColumn(List<Selection<?>> selectionList, Selection<?> sel) {
		if( !selectionList.contains(sel)) {
			selectionList.add(sel);
		}
	}

	private static Predicate buildPredicate(String attributeSelector, CriteriaBuilder cb, Expression<String> expression, String value) {
		
		if( attributeSelector == null) {
			return cb.conjunction();
		}
		
		if( Selector.EQUAL.toString().equals(attributeSelector)){
			logger.debug("buildPredicate equal ('{}') for value '{}'", attributeSelector, value);
			return cb.equal(expression, value);
		}else if( Selector.NOT_EQUAL.toString().equals(attributeSelector)){
			logger.debug("buildPredicate not equal ('{}') for value '{}'", attributeSelector, value);
			return cb.notEqual(expression, value);
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
		
		long lValue = Long.parseLong(value.trim());
		
		if( Selector.EQUAL.toString().equals(attributeSelector)){
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

	private static Predicate buildPredicateInteger(String attributeSelector, CriteriaBuilder cb, Expression<Integer> expression, String value) {
		
		if( attributeSelector == null) {
			return cb.conjunction();
		}
		
		int lValue = Integer.parseInt(value.trim());
		
		if( Selector.EQUAL.toString().equals(attributeSelector)){
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
//			throw ex;
		}
		
		return cb.conjunction();

	}

}
