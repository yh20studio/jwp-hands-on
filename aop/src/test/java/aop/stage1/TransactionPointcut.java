package aop.stage1;

import aop.Transactional;
import java.lang.reflect.Method;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

/**
 * 포인트컷(pointcut). 어드바이스를 적용할 조인 포인트를 선별하는 클래스.
 * TransactionPointcut 클래스는 메서드를 대상으로 조인 포인트를 찾는다.
 *
 * 조인 포인트(join point). 어드바이스가 적용될 위치
 */
public class TransactionPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(final Method method, final Class<?> targetClass) {
        try {
            final boolean annotationPresent = targetClass.getMethod(method.getName(), method.getParameterTypes())
                    .isAnnotationPresent(Transactional.class);
            return annotationPresent;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
