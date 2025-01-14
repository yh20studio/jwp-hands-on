package aop.stage1;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * 어드바이저(advisor). 포인트컷과 어드바이스를 하나씩 갖고 있는 객체. AOP의 애스팩트(aspect)에 해당되는 클래스다.
 */
public class TransactionAdvisor implements PointcutAdvisor {

    private final TransactionAdvice transactionAdvice;
    private final TransactionPointcut transactionPointcut;

    public TransactionAdvisor(final TransactionAdvice transactionAdvice,
                              final TransactionPointcut transactionPointcut) {
        this.transactionAdvice = transactionAdvice;
        this.transactionPointcut = transactionPointcut;
    }

    @Override
    public Pointcut getPointcut() {
        return transactionPointcut;
    }

    @Override
    public Advice getAdvice() {
        return transactionAdvice;
    }

    @Override
    public boolean isPerInstance() {
        return false;
    }
}
