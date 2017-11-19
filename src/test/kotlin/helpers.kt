import org.assertj.core.api.KotlinAssertions
import org.junit.Assert

infix fun <fail, success> Result<fail, success>.succeedsAnd(fn: (success) -> Unit) {
    when (this) {
        is Success -> fn(value)
        is Failure -> Assert.fail("Should have succeeded")
    }
}

infix fun <fail, success> Result<fail, success>.failsAnd(fn: (fail) -> Unit) {
    when (this) {
        is Success -> Assert.fail("Should have failed")
        is Failure -> fn(value)
    }
}

infix fun <fail, success> Result<fail, success>.succeedsAndShouldReturn(expected: success) {
    succeedsAnd { KotlinAssertions.assertThat(it).isEqualTo(expected) }
}

infix fun <fail, success> Result<fail, success>.failsAndShouldReturn(expected: fail) {
    failsAnd { KotlinAssertions.assertThat(it).isEqualTo(expected) }
}

fun <fail, success> Result<fail, success>.succeeded() {
    succeedsAnd { Unit }
}