package de.trustable.ca3s.core.web.rest;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.CertificateAttribute_;
import de.trustable.ca3s.core.domain.Certificate_;
import de.trustable.ca3s.core.web.rest.data.Selector;


public final class CertificateSpecifications {

	static Logger logger = LoggerFactory.getLogger(CertificateSpecifications.class);

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
    	if( inArr == null || inArr.length == 0){
    		return "";
    	}else{
    		return inArr[0];
    	}
    }
    
	public static Specification<Certificate> handleQueryParams(Map<String, String[]> parameterMap) {
		
        return (root, query, cb) -> {
        	
        	Predicate pred = null;
        	
        	boolean bFirst = true;
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

        			if( bFirst ){
        				pred = buildPredicate(root, cb, attribute, attributeSelector, attributeValue);
        				bFirst = false;
        			}else{
        				pred = cb.and(pred, buildPredicate(root, cb, attribute, attributeSelector, attributeValue));
        			}
        		}else{
        			break;
        		}
        	}

        	List<Order> ordering = new ArrayList<Order>();
        	for( int n = 1; n < 20; n++){
        		String paramOrderAttribute = "orderBy_" + n;
    			logger.debug("paramOrderAttribute {} ", paramOrderAttribute);
    			
        		if( parameterMap.containsKey(paramOrderAttribute)){
        			String attribute = getStringValue(parameterMap.get(paramOrderAttribute));
        			if( attribute.length() == 0){
            			logger.debug("paramOrderAttribute {} has no value", paramOrderAttribute);
        				continue;
        			}

        			boolean bUp = attribute.startsWith("U");
        			String attributeName = attribute.substring(1);

        			logger.debug("Ordering by attribute {} up {}", attribute, bUp);
/*
        			if( bFirst ){
        				pred = buildPredicate(root, cb, attribute, attributeSelector, attributeValue);
        				bFirst = false;
        			}else{
        				pred = cb.and(pred, buildPredicate(root, cb, attribute, attributeSelector, attributeValue));
        			}
*/        			
        		}else{
        			break;
        		}
        	}

			query.distinct(true);
			
            return pred;
        };
	}

	private static Predicate buildPredicate(Root<Certificate> root, CriteriaBuilder cb, final String attribute, final String attributeSelector, final String attributeValue) {
		Predicate pred;
/*		
		{ name: 'san', label: 'SAN', type: 'text'},
		{ name: 'issuer', label: 'Issuer', type: 'text'},
		{ name: 'ski', label: 'Ski', type: 'text'},
		{ name: 'validFrom', label: 'From', type: 'date'},
		{ name: 'validTo', label: 'To', type: 'date'},
		{ name: 'fingerprint', label: 'Fingerprint', type: 'text'},
		{ name: 'type', label: 'Type', type: 'text'},
		{ name: 'usage', label: 'Usage', type: 'listUsage'},
		{ name: 'sigatureAlgorithm', label: 'Signature algorithm', type: 'text'},
		{ name: 'signingAlgorithm', label: 'Signing algorithm', type: 'text'},
		{ name: 'hashAlgorithm', label: 'Hash algorithm', type: 'text'},
		{ name: 'padingAlgorithm', label: 'Padding algorithm', type: 'text'} ];
*/
		if( "subject".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SUBJECT),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue.toLowerCase()));

			
		}else if( "san".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SAN),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue.toLowerCase()));
			
		}else if( "issuer".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_ISSUER),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue.toLowerCase()));
			
		}else if( "ski".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SKI),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
			
		}else if( "fingerprint".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_FINGERPRINT),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
				
		}else if( "hashAlgorithm".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_HASH_ALGO),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
		
		}else if( "type".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_KEY_ALGO),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
		
		}else if( "signingAlgorithm".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SIGNATURE_ALGO),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
		
		}else if( "padingAlgorithm".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_PADDING_ALGO),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
		
		}else if( "keyLength".equals(attribute)){
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_KEY_LENGTH),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), attributeValue));
		
		}else if( "serial".equals(attribute)){

			String decSerial = attributeValue;
			if( attributeValue.startsWith("#")){
				decSerial = attributeValue.substring(1);
			} else if( attributeValue.startsWith("$")){
				BigInteger serialBI = new BigInteger( attributeValue.substring(1).replaceAll(" ", ""), 16);
				decSerial = serialBI.toString();
			}
			
			Join<Certificate, CertificateAttribute> attJoin = root.join(Certificate_.certificateAttributes);
			pred = cb.and( cb.equal(attJoin.<String>get(CertificateAttribute_.name), CertificateAttribute.ATTRIBUTE_SERIAL_PADDED),
					buildPredicate( attributeSelector, cb, attJoin.<String>get(CertificateAttribute_.value), de.trustable.util.CryptoUtil.getPaddedSerial(decSerial)));
			
		}else if( "validFrom".equals(attribute)){
			pred = buildDatePredicate( attributeSelector, cb, root.<Instant>get(Certificate_.validFrom), attributeValue);
		}else if( "validTo".equals(attribute)){
			pred = buildDatePredicate( attributeSelector, cb, root.<Instant>get(Certificate_.validTo), attributeValue);
		}else if( "revoked".equals(attribute)){
			pred = buildBooleanPredicate( attributeSelector, cb, root.<Boolean>get(Certificate_.revoked), attributeValue);
		}else{
			
			logger.warn("fall-thru clause adding 'true' condition for {} ", attribute);
			pred = cb.isTrue(null);
		}
		return pred;
	}

	
	private static Predicate buildPredicate(String attributeSelector, CriteriaBuilder cb, Expression<String> expression, String value) {
		
		
		if( Selector.EQUALS.toString().equals(attributeSelector)){
			logger.debug("buildPredicate equal ('{}') for value '{}'", attributeSelector, value);
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
		}else if( Selector.GREATERTHAN.toString().equals(attributeSelector)){
			logger.debug("buildPredicate greaterThan ('{}') for value '{}'", attributeSelector, value);
				return cb.greaterThan(expression, value);
		}else{
			logger.debug("buildPredicate defaults to equals ('{}') for value '{}'", attributeSelector, value);
			return cb.equal(expression, value);
		}
	}

	private static Predicate buildBooleanPredicate(String attributeSelector, CriteriaBuilder cb, Expression<Boolean> expression, String value) {
		
		logger.debug("buildBooleanPredicatedefaults to equals ('{}') ", attributeSelector);
		
		if( Selector.ISTRUE.toString().equals(attributeSelector) ) {
			return cb.equal(expression, Boolean.TRUE);
		} else {
			return cb.equal(expression, Boolean.FALSE);
		}
	}

	
	private static Predicate buildDatePredicate(String attributeSelector, CriteriaBuilder cb, Expression<Instant> expression, String value) {
		try{
			Instant dateTime = Instant.ofEpochMilli(Long.parseLong(value));
			if( "on".equals(attributeSelector)){
				logger.debug("buildPredicate on ('{}') for value {}", attributeSelector, dateTime);
				return cb.equal(expression, dateTime);
			}else if( "before".equals(attributeSelector)){
				logger.debug("buildPredicate before ('{}') for value {}", attributeSelector, dateTime);
				return cb.lessThanOrEqualTo(expression, dateTime);
			}else if( "after".equals(attributeSelector)){
				logger.debug("buildPredicate after ('{}') for value {}", attributeSelector, dateTime);
				return cb.greaterThanOrEqualTo(expression, dateTime);
			}else{
				logger.debug("buildPredicate defaults to equals ('{}') for value {}", attributeSelector, value);
				return cb.equal(expression, value);
			}
		} catch(Exception ex ){
			logger.debug("parsing date ... ", ex);
			throw ex;
		}
	}


}
