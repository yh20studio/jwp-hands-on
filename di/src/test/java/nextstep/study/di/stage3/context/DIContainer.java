package nextstep.study.di.stage3.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContainer {

    private final Set<Object> beans;

    public DIContainer(final Set<Class<?>> classes) {
        this.beans = createBeansInstance(classes);
        dependencyInject();
    }

    private Set<Object> createBeansInstance(final Set<Class<?>> classes) {
        return classes.stream()
                .map(this::createInstance)
                .collect(Collectors.toSet());
    }

    private Object createInstance(final Class<?> clazz) {
        try {
            final Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void dependencyInject() {
        for (Object bean : beans) {
            final Class<?> aClass = bean.getClass();
            final Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                injectWithField(bean, field);
            }
        }
    }

    private void injectWithField(final Object o, final Field field) {
        try {
            field.setAccessible(true);
            final Class<?> fieldType = field.getType();
            if (existBean(fieldType)) {
                final Object bean = getBean(fieldType);
                field.set(o, bean);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> boolean existBean(final Class<T> aClass) {
        return this.beans.stream()
                .anyMatch(bean -> aClass.isAssignableFrom(bean.getClass()));
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        return (T) this.beans.stream()
                .filter(bean -> aClass.isAssignableFrom(bean.getClass()))
                .findFirst()
                .orElseThrow();
    }
}
