package spritz.interpreter

import spritz.value.Value
import spritz.error.Error

/**
 * @author surge
 * @since 01/03/2023
 */
class RuntimeResult {
    
    var value: Value? = null
    var error: Error? = null
    var returnValue: Value? = null
    var shouldContinue = false
    var shouldBreak = false

    fun reset() {
        this.value = null
        this.error = null
        this.returnValue = null
        this.shouldContinue = false
        this.shouldBreak = false
    }

    fun register(result: RuntimeResult): Value? {
        this.error = result.error
        this.returnValue = result.returnValue
        this.shouldContinue = result.shouldContinue
        this.shouldBreak = result.shouldBreak

        return result.value
    }

    fun success(value: Value?): RuntimeResult {
        this.reset()
        this.value = value
        return this
    }

    fun successReturn(value: Value?): RuntimeResult {
        this.reset()
        this.returnValue = value
        return this
    }

    fun successContinue(): RuntimeResult {
        this.reset()
        this.shouldContinue = true
        return this
    }

    fun successBreak(): RuntimeResult {
        this.reset()
        this.shouldBreak = true
        return this
    }

    fun failure(error: Error): RuntimeResult {
        this.reset()
        this.error = error
        return this
    }

    fun shouldReturn(): Boolean = this.error != null || this.returnValue != null || this.shouldContinue || this.shouldBreak
    
}