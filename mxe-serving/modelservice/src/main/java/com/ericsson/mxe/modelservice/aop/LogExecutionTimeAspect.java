package com.ericsson.mxe.modelservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@ConditionalOnProperty(value = "model-service.logExecutionTime", havingValue = "true", matchIfMissing = false)
public class LogExecutionTimeAspect {
    public static final Logger logger = LoggerFactory.getLogger(LogExecutionTimeAspect.class);

    @Around("@annotation(com.ericsson.mxe.modelservice.aop.annotation.LogExecutionTime)")
    public Object methodTimeLogger(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();

        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        // Measure method execution time
        StopWatch stopWatch = new StopWatch(className + "->" + methodName) {
            @Override
            public String shortSummary() {
                return "Executed '" + getId() + "' in " + getTotalTimeMillis() + " ms";
            }
        };
        stopWatch.start(methodName);
        Object result = proceedingJoinPoint.proceed();
        stopWatch.stop();
        // Log method execution time
        if (logger.isInfoEnabled()) {
            // logger.info(stopWatch.prettyPrint());
            logger.info(stopWatch.shortSummary());
        }
        return result;
    }

}
