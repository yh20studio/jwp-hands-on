package nextstep.study.di.stage4.annotations;

import java.util.HashSet;
import java.util.Set;
import org.reflections.Reflections;

public class ClassPathScanner {

    public static Set<Class<?>> getAllClassesInPackage(final String packageName) {
        final Reflections reflections = new Reflections(packageName);
        final Set<Class<?>> allClassesInPackage = new HashSet<>();
        final Set<Class<?>> serviceClass = reflections.getTypesAnnotatedWith(Service.class);
        final Set<Class<?>> repositoryClass = reflections.getTypesAnnotatedWith(Repository.class);
        allClassesInPackage.addAll(serviceClass);
        allClassesInPackage.addAll(repositoryClass);
        return allClassesInPackage;
    }
}
