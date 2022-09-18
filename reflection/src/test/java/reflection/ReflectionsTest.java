package reflection;

import annotation.Controller;
import annotation.Repository;
import annotation.Service;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReflectionsTest {

    private static final Logger log = LoggerFactory.getLogger(ReflectionsTest.class);

    @Test
    void showAnnotationClass() throws Exception {
        Reflections reflections = new Reflections("examples");
        final Set<Class<?>> controllerAnnotatedWith = reflections.getTypesAnnotatedWith(Controller.class);
        final Set<Class<?>> serviceAnnotatedWith = reflections.getTypesAnnotatedWith(Service.class);
        final Set<Class<?>> repositoryAnnotatedWith = reflections.getTypesAnnotatedWith(Repository.class);
        for (Class<?> clazz : controllerAnnotatedWith) {
            log.info("Controller {}", clazz.getName());
        }
        for (Class<?> clazz : serviceAnnotatedWith) {
            log.info("Service {}", clazz.getName());
        }
        for (Class<?> clazz : repositoryAnnotatedWith) {
            log.info("Repository {}", clazz.getName());
        }
        // TODO 클래스 레벨에 @Controller, @Service, @Repository 애노테이션이 설정되어 모든 클래스 찾아 로그로 출력한다.
    }
}
