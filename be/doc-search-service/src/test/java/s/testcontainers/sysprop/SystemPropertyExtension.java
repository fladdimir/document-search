package s.testcontainers.sysprop;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SystemPropertyExtension
        implements
        // AfterEachCallback, BeforeEachCallback,
        BeforeAllCallback, AfterAllCallback {

    // @Override
    // public void afterEach(ExtensionContext extensionContext) throws Exception {
    // SetSystemProperty annotation =
    // extensionContext.getTestMethod().get().getAnnotation(SetSystemProperty.class);
    // System.clearProperty(annotation.key());
    // }

    // @Override
    // public void beforeEach(ExtensionContext extensionContext) throws Exception {
    // SetSystemProperty annotation =
    // extensionContext.getTestMethod().get().getAnnotation(SetSystemProperty.class);
    // System.setProperty(annotation.key(), annotation.value());
    // }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        SetSystemProperty annotation = context.getTestClass().get().getAnnotation(SetSystemProperty.class);
        System.clearProperty(annotation.key());
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        SetSystemProperty annotation = context.getTestClass().get().getAnnotation(SetSystemProperty.class);
        System.setProperty(annotation.key(), annotation.value());
    }
}