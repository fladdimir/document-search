package s.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

import com.google.common.base.Stopwatch;

import s.util.TimeLoggingInterceptor.LogExecutionTime;

@LogExecutionTime
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class TimeLoggingInterceptor {

    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.TYPE })
    public static @interface LogExecutionTime {
        // see for additional parameters:
        // https://github.com/quarkusio/quarkus/issues/5373
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public static @interface LogExecutionTimeParameter {
        String name();
    }

    @Inject
    Logger logger;

    @AroundInvoke
    Object intercept(InvocationContext context) throws Exception {

        var timer = Stopwatch.createStarted();
        Object ret = context.proceed();
        timer.stop();

        String parameterString = getParameterString(context);
        logger.info("" + context.getMethod() + (parameterString.length() > 0 ? "\nparameters: " + parameterString : "")
                + "\nexecution time: " + timer);

        return ret;
    }

    private String getParameterString(InvocationContext context) {
        return IntStream.range(0, context.getMethod().getParameterCount()).mapToObj(i -> getParamLog(i, context))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.joining(", "));
    }

    private Optional<String> getParamLog(int i, InvocationContext context) {
        return getLoggedParameterName(context.getMethod().getParameterAnnotations()[i])
                .map(v -> v + ": " + context.getParameters()[i]);
    }

    private Optional<String> getLoggedParameterName(Annotation[] annotations) {
        // tbd: empty default -> parameter name (register for reflection ?)
        return Arrays.stream(annotations).filter(a -> a.annotationType().equals(LogExecutionTimeParameter.class))
                .map(a -> (LogExecutionTimeParameter) a).map(LogExecutionTimeParameter::name).findAny();
    }

}
