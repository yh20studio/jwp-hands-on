package transaction.stage2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * 트랜잭션 전파(Transaction Propagation)란?
 * 트랜잭션의 경계에서 이미 진행 중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식을 말한다.
 *
 * FirstUserService 클래스의 메서드를 실행할 때 첫 번째 트랜잭션이 생성된다.
 * SecondUserService 클래스의 메서드를 실행할 때 두 번째 트랜잭션이 어떻게 되는지 관찰해보자.
 *
 * https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#tx-propagation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage2Test {

    private static final Logger log = LoggerFactory.getLogger(Stage2Test.class);

    @Autowired
    private FirstUserService firstUserService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    /**
     * 생성된 트랜잭션이 몇 개인가? 왜 그런 결과가 나왔을까? 2번째 트랜잭션의 전파수준이 Required 이다. 그렇기 때문에 부모인 firstUserService 메서드에서 생성된 트랜잭션이 그대로
     * 전파되어서 2번째 메서드에서도 그대로 사용되기 때문에 1개가 나온다.
     */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequired");
    }

    /**
     * 만약에 내부의 논리적인 트랜잭션이 예외를 던지고 해당 예외를 try catch 문으로 바깥 메서드에서 잡는다고 해도 rollback-only가 마킹되어 있기 때문에 최종 물리적인 트랜잭션은 커밋될 수
     * 없다.
     */
    @Test
    void testRequiredThrow() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithRequiredThrow())
                .isInstanceOf(UnexpectedRollbackException.class);
    }

    /**
     * 생성된 트랜잭션이 몇 개인가? 왜 그런 결과가 나왔을까? 2번째 트랜잭션의 전파 속성이 Required_New 이다. 따라서 부모의 트랜잭션과는 상관없이 해당 메서드 내부에서 새로운 트랜잭션을 생성하기
     * 때문이다.
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다. REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는
     * 지 확인해보자. REQUIRES_NEW로 2번째 save하는 메서드에서 새로운 트랜잭션이 생성되므로 부모 트랜잭션이 실패하여 롤백이 되더라도 save하는 트랜잭션은 커밋된다.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * Supports 전파 속성을 가지고 있기 때문에 부모가 트랜잭션을 가지고 있어야만 종속된다.
     * 만약 부모가 트랜잭션이 없다면 트랜잭션이 active되지 않고 진행된다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * Mandatory 속성은 Supports와 부모가 트랜잭션이 있는 경우에는 종속된다는 점이 같지만,
     * Mandatory는 부모가 트랜잭션이 없는 경우에는 예외가 발생한다.(org.springframework.transaction.IllegalTransactionStateException)
     */
    @Test
    void testMandatory() {
        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * 1개가 작동한다. Not_Supported 이기 때문에 부모가 트랜잭션을 가지고 있다고 하더라도 부모의 트랜잭션을 대기시키고 트랜잭션 없이 해당 로직을
     * 처리하기 때문이다.
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     * 주석 처리하고 나면 0개가 작동한다.
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * https://docs.spring.io/spring-framework/docs/5.1.4.RELEASE/spring-framework-reference/data-access.html#tx-propagation
     * 각 메서드 호출마다 트랜잭션이 전파됨에 있어서 사실은 물리적은 트랜잭션은 1개이다.
     * 그리고 각 메서드마다 논리적인 트랜잭션을 따로 가지고 있는 것이다.
     * 그렇기 때문에 각각이 롤백 상태를 독립적으로 결정할 수 있다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * JPA를 사용하는 경우, 변경감지를 통해서 업데이트문을 최대한 지연해서 발행하는 방식을 사용하기 때문에 중첩된 트랜잭션 경계를 설정할 수 없어 지원하지 않습니다.
     * Nested 속성을 사용하기 위해선 JPA가 아닌 DataSourceTransactionManager를 직접적으로 JDBC에서 사용해야한다.
     * Nested 속성은 참고로 하나의 물리적 트랜잭션을 여러개의 save point를 이용해서 사용하게 된다.
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * 부모의 트랜잭션이 없다면 Required 와 마찬가지로 자식내부에서 트랜잭션을 사용하게 된다.
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     * Never 속성은 부모의 트랜잭션이 있다면 예외를 발생시킨다.
     * org.springframework.transaction.IllegalTransactionStateException
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
