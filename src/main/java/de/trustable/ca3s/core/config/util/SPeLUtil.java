package de.trustable.ca3s.core.config.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.*;

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
            LOG.info("SPeL evaluation of [{}] failed: {}", expressionString, exception.getMessage());
            return "";
        }
    }
    public Collection<String> evaluateListExpression(HashMap<String, List<String>> attributeMap, String expressionString) {
        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression(expressionString);

        EvaluationContext context = new StandardEvaluationContext(attributeMap);
        try {
            Object result = expression.getValue(context);
            if( result == null){
                return Collections.emptyList();
            }else if( result instanceof String){
                return Collections.singletonList((String)result);
            }else if( result instanceof String[]){
                return Arrays.asList( (String[])result);
            }else if( result instanceof Collection){
                return (Collection<String>)result;
            }
            return Collections.emptyList();
        }catch(Exception exception){
            LOG.info("SPeL evaluation of [{}] failed: {}", expressionString, exception.getMessage());
            return Collections.emptyList();
        }
    }


}
