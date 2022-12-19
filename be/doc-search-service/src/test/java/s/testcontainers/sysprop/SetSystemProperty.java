package s.testcontainers.sysprop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

// https://stackoverflow.com/a/46851140
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@ExtendWith(SystemPropertyExtension.class)
public @interface SetSystemProperty {

    String key();

    String value();
}
