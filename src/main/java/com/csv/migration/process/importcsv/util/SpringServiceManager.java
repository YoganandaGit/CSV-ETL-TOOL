package com.csv.migration.process.importcsv.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * The Class SpringServiceManager.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class SpringServiceManager implements ApplicationContextAware, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(SpringServiceManager.class);

    private static Environment environment;
    /**
     * The context.
     */
    private static ApplicationContext context = null;

    private static CopyOnWriteArrayList<ApplicationContext> otherContextList = new CopyOnWriteArrayList<>();
    private static ConcurrentHashMap<String, Optional<Object>> beanLookupCache = new ConcurrentHashMap<>();
    private static Function<String, Optional<Object>> findBeanInOtherContext = (key) -> {
        for (ApplicationContext otherContext : otherContextList) {
            if (otherContext.containsBean(key)) {
                return Optional.ofNullable(otherContext.getBean(key));
            }
        }
        return Optional.empty();
    };

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        setContext(applicationContext);
        logger.info("In setApplicationContext: " + Objects.hashCode(applicationContext));
    }

    @Override
    public void setEnvironment(Environment environment) {
        SpringServiceManager.environment = environment;
        logger.info("In setEnvironment");
    }

    /**
     * Sets the context.
     *
     * @param context the new context
     */
    private static void setContext(ApplicationContext context) {
        SpringServiceManager.context = context;
        logger.info("In SpringServiceManager: ApplicationContext initialized, configuredBeans: " + StringUtils.join(context.getBeanDefinitionNames(), ","));
    }

    /**
     * Gets the context.
     *
     * @return the context
     */
    public static final ApplicationContext getContext() {
        if (context == null) {
            throw new ApplicationContextException("The context is null, Spring not initialized, please make sure call this function after the initialization of spring framework");
        }
        return context;
    }

    public static void registerOtherContext(ApplicationContext otherContext) {
        if (otherContext == null) {
            return;
        }
        if (otherContextList.contains(otherContext)) {
            return;
        }
        otherContextList.add(otherContext);
        beanLookupCache.clear();
    }

    /**
     * Gets the bean.
     *
     * @param <T>  the generic type
     * @param name the name
     * @return the bean
     */
    @SuppressWarnings("unchecked")
    public static final <T> T getBean(String name) {

        try {
            Optional<Object> optionalBean = beanLookupCache.get(name);
            if (optionalBean == null || !optionalBean.isPresent()) {
                return (T) getContext().getBean(name);
            } else {
                logger.info("In getBean: getting bean from other context beanLookupCache for bean : " + name + ", isPresent: " + optionalBean.isPresent());
                return (T) optionalBean.get();
            }
        } catch (NoSuchBeanDefinitionException ex) {
            Optional<Object> optionalBean = findBeanInOtherContext.apply(name);
            if (optionalBean.isPresent()) {
                beanLookupCache.put(name, optionalBean);
                logger.info("In getBean: After caching the bean in beanLookupCache for bean : " + name + ", isPresent: " + optionalBean.isPresent());
                return (T) optionalBean.get();
            }
            throw ex;
        }
    }

    /**
     * Gets the bean.
     *
     * @param <T>          the generic type
     * @param name         the name of the bean to retrieve
     * @param requiredType type the bean must match. Can be an interface or superclass of the actual class, or {@code null} for any match. For example, if the value is {@code Object.class}, this method will succeed whatever the class of the returned instance.
     * @return an instance of the bean
     */
    public static final <T> T getBean(String name, Class<T> requiredType) {
        return getContext().getBean(name, requiredType);
    }

    public static final <T> T getBean(Class<T> requiredType) {
        return (T) getContext().getBean(requiredType);
    }

    /**
     * Contains bean.
     *
     * @param name the name
     * @return true, if successful
     */
    public static final boolean containsBean(String name) {
        return getContext().containsBean(name);
    }

    public static final String getEnvironmentProperty(String key) {
        try {
            return environment == null ? null : environment.getProperty(key);
        } catch (Throwable th) {
            logger.error("Error in getEnvironmentProperty: key: " + key + " : " + th.getMessage(), th);
        }
        return null;
    }

}
