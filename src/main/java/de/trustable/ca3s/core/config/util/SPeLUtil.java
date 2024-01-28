package de.trustable.ca3s.core.config.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class SPeLUtil {

    private final Logger LOG = LoggerFactory.getLogger(SPeLUtil.class);

    public String evaluateExpression(HashMap<String, List<String>> attributeMap, String expressionString) {
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
