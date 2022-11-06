package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    private final PlatformTransactionManager platformTransactionManager;
    private final Object target;

    public TransactionHandler(final PlatformTransactionManager platformTransactionManager,
                              final Object target) {
        this.platformTransactionManager = platformTransactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (!isAnnotationPresent(method)) {
            return method.invoke(target, args);
        }

        final TransactionStatus transaction = platformTransactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            final Object result = method.invoke(target, args);
            platformTransactionManager.commit(transaction);
            return result;
        } catch (InvocationTargetException e) {
            platformTransactionManager.rollback(transaction);
            throw new DataAccessException(e);
        }
    }

    private boolean isAnnotationPresent(final Method method) throws NoSuchMethodException {
        return target.getClass()
                .getMethod(method.getName(), method.getParameterTypes())
                .isAnnotationPresent(Transactional.class);
    }
}
