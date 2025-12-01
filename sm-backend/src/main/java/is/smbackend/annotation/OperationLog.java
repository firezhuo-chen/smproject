package is.smbackend.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 操作类型
     */
    String operation();
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 操作对象类型（如：award, punishment, appeal等）
     */
    String targetType() default "";
    
    /**
     * 是否记录变更前后的值
     */
    boolean recordChange() default false;
}
