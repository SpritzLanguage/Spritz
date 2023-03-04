package spritz.api.annotations

/**
 * @author surge
 * @since 04/03/2023
 */

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Identifier(val identifier: String)
