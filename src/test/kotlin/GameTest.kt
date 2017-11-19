import org.junit.Test

import org.assertj.core.api.KotlinAssertions.assertThat
import org.junit.Assert.fail


class GameTest {

    @Test
    fun `the game begins with a score of 0`() {

        score(Game()) succeedsAndShouldReturn 0
    }

    @Test
    fun `a game with rolls of 1 and 1 has a score of 2`() {

        score(game(1, 1)) succeedsAndShouldReturn 2
    }

    @Test
    fun `a game with rolls of 9 and 3 (any two consecutive rolls in the same frame) greater than 10 is illegal`() {

        score(game(9, 3)) failsAndShouldReturn IllegalFrameScore
    }

    @Test
    fun `a game with any roll over 10 is illegal`() {

        score(game(11)) failsAndShouldReturn IllegalRollScore
    }

    @Test
    fun `a game can have a maximum of 20 non-strike rolls`() {

        val list = (1..21).fold(emptyList<Int>()) { acc, _ -> acc.plus(1) }

        score(Game(list)) failsAndShouldReturn TooManyRolls
    }

    @Test
    fun `a strike occurs when all 10 pin are knocked over in the first roll of a frame and is scored as ten plus the next two roll values`() {

        score(game(10, 1, 1)) succeedsAndShouldReturn 14
    }

    @Test
    fun `a strike and then a spare`() {

        score(game(10, 3, 7, 1)) succeedsAndShouldReturn 32
    }

    @Test
    fun `a strike has a preliminary score failure with a value when it has no rolls after it`() {

        score(game(10)) failsAndShouldReturn PreliminaryScore(10)
    }

    @Test
    fun `a strike has a preliminary score failure with a value when it has one roll after it`() {

        score(game(10, 1)) failsAndShouldReturn PreliminaryScore(12)
    }

    @Test
    fun `a strike has a preliminary score failure with a value when it has one strike after it`() {

        score(game(10, 10)) failsAndShouldReturn PreliminaryScore(30)
    }

    @Test
    fun `a strike has a score of 30 when it has two strikes following it`() {

        score(game(10, 10, 10, 1, 1)) succeedsAndShouldReturn 65
    }

    @Test
    fun `a strike then a strike then an incomplete frame`() {

        score(game(10, 10, 1)) failsAndShouldReturn PreliminaryScore(33)
    }

    @Test
    fun `it scores twos rolls in one frame totally 10 as 10 plus the value of the next roll`() {

        score(game(3, 7, 1)) succeedsAndShouldReturn 12

        score(game(3, 7, 1, 1)) succeedsAndShouldReturn 13

        // (3 + 7 + 3) + (3 + 7 + 1) + 1
        score(game(3, 7, 3, 7, 1)) succeedsAndShouldReturn 25

        // (3 + 7 + 10) + (10 + 1 + 1) + 1 + 1
        score(game(3, 7, 10, 1, 1)) succeedsAndShouldReturn 34
    }

    @Test
    fun `it returns a Failure with a preliminary score if a spare is the last frame bowled`() {

        score(game(3, 7)) failsAndShouldReturn PreliminaryScore(10)
    }

    @Test
    fun `the last frame of the game can have a third roll if the first two rolls are strikes`() {

        val list = (1..12).fold(emptyList<Int>()) { acc, _ -> acc.plus(10) }

        score(Game(list)) succeedsAndShouldReturn 300
    }
}