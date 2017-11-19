data class Game(val rolls: List<Int> = emptyList())

sealed class Frame

data class IncompleteFrame(val roll: Int) : Frame()

data class Spare(val rollOne: Int) : Frame() {
    val rollTwo: Int = 10 - rollOne
}

object Strike : Frame()

data class CompleteFrame(val rollOne: Int, val rollTwo: Int) : Frame()

data class LastFrame(val rollOne: Int, val rollTwo: Int? = null, val rollThree: Int? = null)

fun game(vararg rolls: Int) = Game(rolls.toList())

sealed class ScoreFailure

object IllegalFrameScore : ScoreFailure()

object IllegalRollScore : ScoreFailure()

object TooManyRolls : ScoreFailure()

data class PreliminaryScore(val value: Int) : ScoreFailure()

fun score(game: Game): Result<ScoreFailure, Int> {

    val initialResult: Result<ScoreFailure, List<Frame>> = Success<ScoreFailure, List<Frame>>(emptyList())

    return game.rolls.fold(initialResult) { frameListResult, roll ->

        frameListResult.flatMap { frameList ->
            if (roll > 10) {
                Failure<ScoreFailure, List<Frame>>(IllegalRollScore)
            } else {
                frameList.lastOrNull().let { lastFrame ->
                    when (lastFrame) {
                        null, is CompleteFrame, Strike, is Spare ->
                            if (roll == 10) Strike else {
                                IncompleteFrame(roll = roll)
                            }
                                    .let { frameList.plus(it) }
                                    .let(::Success)
                        is IncompleteFrame -> {
                            (lastFrame.roll + roll).let {
                                when {
                                    it > 10 -> Failure<ScoreFailure, List<Frame>>(IllegalFrameScore)
                                    it == 10 -> Success<ScoreFailure, List<Frame>>(frameList.dropLast(1).plus(Spare(rollOne = lastFrame.roll)))
                                    it < 0 -> Failure<ScoreFailure, List<Frame>>(IllegalFrameScore)
                                    else -> Success<ScoreFailure, List<Frame>>(frameList.dropLast(1).plus(CompleteFrame(rollOne = lastFrame.roll, rollTwo = roll)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }.let {
        it.flatMap { frames ->
            if (frames.count() > 10) {
                Failure(TooManyRolls as ScoreFailure)
            } else {
                frames.foldIndexed(Success<ScoreFailure, Int>(0) as Result<ScoreFailure, Int>) { index, accResult, frame ->
                    accResult.flatMap { acc ->
                        when (frame) {
                            is CompleteFrame -> Success<ScoreFailure, Int>(acc + frame.rollOne + frame.rollTwo)
                            is IncompleteFrame -> Success<ScoreFailure, Int>(acc + frame.roll)
                            is Spare -> {
                                if (frames.count() > index + 1) {
                                    frames[index + 1].let { nextFrame ->
                                        when (nextFrame) {
                                            is CompleteFrame -> Success<ScoreFailure, Int>(acc + 10 + nextFrame.rollOne)
                                            is IncompleteFrame -> Success<ScoreFailure, Int>(acc + 10 + nextFrame.roll)
                                            is Strike -> Success<ScoreFailure, Int>(acc + 10 + 10)
                                            is Spare ->Success<ScoreFailure, Int>(acc + 10 + nextFrame.rollOne)
                                        }
                                    }
                                } else {
                                    Failure<ScoreFailure, Int>(PreliminaryScore(value = acc + 10))
                                }
                            }
                            Strike -> {
                                if (frames.count() > index + 1) {
                                    frames[index + 1].let { nextFrame ->
                                        when (nextFrame) {
                                            is CompleteFrame -> Success<ScoreFailure, Int>(acc + 10 + nextFrame.rollOne + nextFrame.rollTwo)
                                            is IncompleteFrame -> Failure<ScoreFailure, Int>(PreliminaryScore(value = acc + 10 + nextFrame.roll * 2))
                                            is Spare -> Success<ScoreFailure, Int>(acc + 10 + 10)
                                            is Strike -> {
                                                if (frames.count() > index + 2) {
                                                    frames[index + 2].let { anotherFrame ->
                                                        when (anotherFrame) {
                                                            is CompleteFrame -> Success<ScoreFailure, Int>(acc + 10 + 10 + anotherFrame.rollOne)
                                                            is IncompleteFrame -> Success<ScoreFailure, Int>(acc + 10 + 10 + anotherFrame.roll)
                                                            is Spare -> TODO()
                                                            is Strike -> Success<ScoreFailure, Int>(acc + 10 + 10 + 10)
                                                        }
                                                    }
                                                } else {
                                                    Failure<ScoreFailure, Int>(PreliminaryScore(acc + 10 + 10 + 10))
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Failure<ScoreFailure, Int>(PreliminaryScore(value = acc + 10))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

//fun scoreSpare()

sealed class Result<fail, success>

data class Success<fail, success>(val value: success) : Result<fail, success>()

data class Failure<fail, success>(val value: fail) : Result<fail, success>()

fun <fail, success, newSuccess> Result<fail, success>.flatMap(
        transform: (success) -> Result<fail, newSuccess>
): Result<fail, newSuccess> {

    return when (this) {
        is Success -> transform(this.value)
        is Failure -> Failure(this.value)
    }
}