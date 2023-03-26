package spritz.api.annotations

/**
 * Marks a function or field to not be processed when the class is coerced.
 *
 * @author surge
 * @since 04/03/2023
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Excluded
