package de.trustable.ca3s.core.security;

import de.trustable.ca3s.core.security.saml.CustomSAMLAuthenticationProvider;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SPELEvalTest {

    private final Logger LOG = LoggerFactory.getLogger(SPELEvalTest.class);

    @Test
    void testGetCurrentUserLogin() {

        HashMap<String, List<String>> attributeMap = new HashMap();

        attributeMap.put("id", Collections.singletonList("id_1"));
        attributeMap.put("user", Collections.singletonList("xy123456"));
        attributeMap.put("a", Collections.singletonList("a"));
        attributeMap.put("firstname", Collections.singletonList("Joe"));
        attributeMap.put("lastname", Collections.singletonList("Smith"));
        attributeMap.put("email", Collections.singletonList("joe@smith.com"));

        String result = evaluateExpression(attributeMap, "'constant value'");
        Assert.assertEquals("constant value ", "constant value", result);

        result = evaluateExpression(attributeMap, "get('id').get(0)");
        Assert.assertEquals("value of key 'id'", "id_1", result);

        result = evaluateExpression(attributeMap, "get('user').get(0).substring(0,2).toUpperCase()");
        Assert.assertEquals("first two chars, uppercase, of user value ", "XY", result);

        result = evaluateExpression(attributeMap, "get('foo').get(0).substring(0,2).toUpperCase()");
        Assert.assertEquals("empty value for unknown key 'foo' ", "", result);

        result = evaluateExpression(attributeMap, "get('a').get(0).substring(0,10)");
        Assert.assertEquals("empty value for string of too short value for substring ", "", result);

        result = evaluateExpression(attributeMap, "T(java.lang.System).out.println('aaa')");

    }


    private String evaluateExpression(HashMap<String, List<String>> attributeMap, String expressionString) {
        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression(expressionString);

        EvaluationContext context = new StandardEvaluationContext(attributeMap);
        try {
            return (String) expression.getValue(context);
        }catch(Exception exception){
            LOG.info("SPeL evaluation of [{}]failed: {}", expressionString, exception.getMessage());
            return "";
        }
    }

}
