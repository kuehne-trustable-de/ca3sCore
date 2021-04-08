package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.service.dto.Selector;
import de.trustable.ca3s.core.service.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class SpecificationsHelper {

    static Logger logger = LoggerFactory.getLogger(SpecificationsHelper.class);

    static final String SORT = "sort";
    static final String ORDER = "order";

    private SpecificationsHelper() {
    }

    private static String getContainsLikePattern(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return "%";
        } else {
            return searchTerm.toLowerCase() + "%";
        }
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
     * Parse the set of selection columns and put them into a map
     *
     * @param parameterMap
     * @return
     */
    static Map<String, List<SelectionData>> getSelectionMap(Map<String, String[]> parameterMap) {

        Map<String, List<SelectionData>> selectorMap = new HashMap<String, List<SelectionData>>();

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
                    List<SelectionData> selectorList = new ArrayList<SelectionData>();
                    selectorList.add(selData);
                    selectorMap.put(attribute, selectorList);
                }
            } else {
                logger.debug("paramNameAttribute '{}' not contained in parameterMap ", paramNameAttribute);
                break;
            }
        }
        return selectorMap;
    }


    static void addNewColumn(List<Selection<?>> selectionList, Selection<?> sel) {
        if (!selectionList.contains(sel)) {
            selectionList.add(sel);
        }
    }

    static Predicate buildPredicateString(String attributeSelector, CriteriaBuilder cb, Expression<String> expression, String value) {

        if (attributeSelector == null) {
            return cb.conjunction();
        }

        if (Selector.EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate equal ('{}') for value '{}'", attributeSelector, value);
            return cb.equal(expression, value);
        } else if (Selector.NOT_EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate not equal ('{}') for value '{}'", attributeSelector, value);
            return cb.notEqual(expression, value);
        } else if (Selector.ON.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate on ('{}') for value '{}'", attributeSelector, value);
            return cb.equal(expression, value);
        } else if (Selector.LIKE.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate like ('{}') for value '{}'", attributeSelector, getContainsLikePattern(value));
            return cb.like(expression, getContainsLikePattern(value));
        } else if (Selector.NOTLIKE.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate not like ('{}') for value '{}'", attributeSelector, getContainsLikePattern(value));
            return cb.like(expression, getContainsLikePattern(value)).not();
        } else if (Selector.LESSTHAN.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate lessThan ('{}') for value '{}'", attributeSelector, value);
            return cb.lessThan(expression, value);
        } else if (Selector.BEFORE.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate before ('{}') for value '{}'", attributeSelector, value);
            return cb.lessThan(expression, value);
        } else if (Selector.GREATERTHAN.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate greaterThan ('{}') for value '{}'", attributeSelector, value);
            return cb.greaterThan(expression, value);
        } else if (Selector.AFTER.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate after ('{}') for value '{}'", attributeSelector, value);
            return cb.greaterThan(expression, value);
        } else {
            logger.debug("buildPredicate defaults to equals ('{}') for value '{}'", attributeSelector, value);
            return cb.equal(expression, value);
        }
    }

    static Predicate buildPredicateLong(String attributeSelector, CriteriaBuilder cb, Expression<Long> expression, String value) {

        if (attributeSelector == null) {
            return cb.conjunction();
        }

        long lValue = Long.parseLong(value.trim());

        if (Selector.EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate equal ('{}') for value '{}'", attributeSelector, lValue);
            return cb.equal(expression, lValue);
        } else if (Selector.LESSTHAN.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate lessThan ('{}') for value '{}'", attributeSelector, lValue);
            return cb.lessThan(expression, lValue);
        } else if (Selector.GREATERTHAN.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate greaterThan ('{}') for value '{}'", attributeSelector, lValue);
            return cb.greaterThan(expression, lValue);
        } else {
            logger.debug("buildPredicate defaults to equals ('{}') for value '{}'", attributeSelector, lValue);
            return cb.equal(expression, lValue);
        }
    }

    static Predicate buildPredicateInteger(String attributeSelector, CriteriaBuilder cb, Expression<Integer> expression, String value) {

        if (attributeSelector == null) {
            return cb.conjunction();
        }

        int lValue = Integer.parseInt(value.trim());

        if (Selector.EQUAL.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate equal ('{}') for value '{}'", attributeSelector, lValue);
            return cb.equal(expression, lValue);
        } else if (Selector.LESSTHAN.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate lessThan ('{}') for value '{}'", attributeSelector, lValue);
            return cb.lessThan(expression, lValue);
        } else if (Selector.GREATERTHAN.toString().equals(attributeSelector)) {
            logger.debug("buildPredicate greaterThan ('{}') for value '{}'", attributeSelector, lValue);
            return cb.greaterThan(expression, lValue);
        } else {
            logger.debug("buildPredicate defaults to equals ('{}') for value '{}'", attributeSelector, lValue);
            return cb.equal(expression, lValue);
        }
    }

    static Predicate buildBooleanPredicate(String attributeSelector, CriteriaBuilder cb, Expression<Boolean> expression, String value) {

        if (attributeSelector == null) {
            return cb.conjunction();
        }

        logger.debug("buildBooleanPredicatedefaults to equals ('{}') ", attributeSelector);

        if (Selector.ISTRUE.toString().equals(attributeSelector)) {
            return cb.equal(expression, Boolean.TRUE);
        } else {
            return cb.equal(expression, Boolean.FALSE);
        }
    }


    private static Predicate buildDatePredicate(String attributeSelector, CriteriaBuilder cb, Expression<Instant> expression, String value) {

        if (attributeSelector == null) {
            return cb.conjunction();
        }

        Instant dateTime;
        try {

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateTime = DateUtil.asInstant(dateFormat.parse(value));
            } catch (Exception ex) {
                dateTime = Instant.ofEpochMilli(Long.parseLong(value));
            }

            if (Selector.ON.toString().equals(attributeSelector)) {

                // truncate isn't idempotent, so ensure the date isn't already truncated by adding an hour
                Instant dateTimeStart = dateTime.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.DAYS);
                //add exactly one day
                Instant dateTimeEnd = dateTimeStart.plus(1, ChronoUnit.DAYS);

                logger.debug("buildDatePredicate on ('{}') for value > {} and < {}", attributeSelector, dateTimeStart, dateTimeEnd);

                // find all elements within the given day
                return cb.and(cb.lessThanOrEqualTo(expression, dateTimeEnd), cb.greaterThanOrEqualTo(expression, dateTimeStart));

            } else if (Selector.BEFORE.toString().equals(attributeSelector)) {
                logger.debug("buildDatePredicate before ('{}') for value {}", attributeSelector, dateTime);
                return cb.lessThanOrEqualTo(expression, dateTime);
            } else if (Selector.AFTER.toString().equals(attributeSelector)) {
                logger.debug("buildDatePredicate after ('{}') for value {}", attributeSelector, dateTime);
                return cb.greaterThanOrEqualTo(expression, dateTime);
            } else {
                logger.debug("buildDatePredicate defaults to equals ('{}') for value {}", attributeSelector, dateTime);
                return cb.equal(expression, dateTime);
            }
        } catch (Exception ex) {
            logger.debug("parsing date ... ", ex);
//			throw ex;
        }

        return cb.conjunction();

    }
}
