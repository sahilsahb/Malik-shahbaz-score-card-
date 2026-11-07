
package com.malikshahbaz.scorecardv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF071B36)
private val Blue = Color(0xFF174477)
private val Gold = Color(0xFFFFC857)

data class BallState(
    val runs: Int,
    val wickets: Int,
    val balls: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ScoreCardApp()
            }
        }
    }
}

@Composable
fun ScoreCardApp() {
    var page by remember { mutableStateOf("home") }
    var teamA by remember { mutableStateOf("Team A") }
    var teamB by remember { mutableStateOf("Team B") }
    var format by remember { mutableStateOf("T20") }
    var oversLimit by remember { mutableIntStateOf(20) }
    var battingFirst by remember { mutableStateOf(true) }

    var innings by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var wickets by remember { mutableIntStateOf(0) }
    var balls by remember { mutableIntStateOf(0) }
    var firstScore by remember { mutableIntStateOf(0) }

    val undoList = remember { mutableStateListOf<BallState>() }

    val battingTeam = if (innings == 1) {
        if (battingFirst) teamA else teamB
    } else {
        if (battingFirst) teamB else teamA
    }

    fun addBall(runs: Int = 0, wicket: Boolean = false, legal: Boolean = true) {
        if (balls >= oversLimit * 6 || wickets >= 10) return

        undoList.add(BallState(score, wickets, balls))
        score += runs
        if (wicket) wickets++
        if (legal) balls++
    }

    fun undoBall() {
        if (undoList.isNotEmpty()) {
            val old = undoList.removeAt(undoList.lastIndex)
            score = old.runs
            wickets = old.wickets
            balls = old.balls
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Navy
    ) {
        when (page) {
            "home" -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🏏", fontSize = 65.sp)
                Text(
                    "MALIK SHAHBAZ",
                    color = Gold,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("SCORE CARD V2", color = Color.White, fontSize = 21.sp)
                Spacer(Modifier.height(30.dp))
                MainButton("NEW MATCH") { page = "setup" }
                MainButton("ABOUT APP") { page = "about" }
            }

            "setup" -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Heading("NEW MATCH")

                OutlinedTextField(
                    value = teamA,
                    onValueChange = { teamA = it },
                    label = { Text("Team A") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = teamB,
                    onValueChange = { teamB = it },
                    label = { Text("Team B") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Match Format", color = Color.White)

                Row {
                    listOf("T20", "ODI", "Test").forEach { item ->
                        FilterChip(
                            selected = format == item,
                            onClick = {
                                format = item
                                oversLimit = when (item) {
                                    "T20" -> 20
                                    "ODI" -> 50
                                    else -> 90
                                }
                            },
                            label = { Text(item) }
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                }

                OutlinedTextField(
                    value = oversLimit.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { number ->
                            if (number in 1..500) oversLimit = number
                        }
                    },
                    label = { Text("Overs") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Batting First", color = Gold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = battingFirst,
                        onClick = { battingFirst = true }
                    )
                    Text(teamA, color = Color.White)
                    RadioButton(
                        selected = !battingFirst,
                        onClick = { battingFirst = false }
                    )
                    Text(teamB, color = Color.White)
                }

                MainButton("START MATCH") {
                    score = 0
                    wickets = 0
                    balls = 0
                    firstScore = 0
                    innings = 1
                    undoList.clear()
                    page = "score"
                }

                BackButton { page = "home" }
            }

            "score" -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Heading("LIVE SCORE")

                Text(battingTeam, color = Color.White, fontSize = 22.sp)

                Text(
                    "$score / $wickets",
                    color = Gold,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Overs: ${balls / 6}.${balls % 6} / $oversLimit",
                    color = Color.White,
                    fontSize = 20.sp
                )

                Text("Innings $innings", color = Gold)

                Spacer(Modifier.height(18.dp))
                Heading("ADD RUNS")

                Row(Modifier.fillMaxWidth()) {
                    (0..3).forEach { run ->
                        ScoreButton(run.toString(), Modifier.weight(1f)) {
                            addBall(runs = run)
                        }
                    }
                }

                Row(Modifier.fillMaxWidth()) {
                    (4..6).forEach { run ->
                        ScoreButton(run.toString(), Modifier.weight(1f)) {
                            addBall(runs = run)
                        }
                    }
                    ScoreButton("W", Modifier.weight(1f)) {
                        addBall(wicket = true)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Heading("EXTRAS")

                Row(Modifier.fillMaxWidth()) {
                    ScoreButton("WD", Modifier.weight(1f)) {
                        addBall(runs = 1, legal = false)
                    }
                    ScoreButton("NB", Modifier.weight(1f)) {
                        addBall(runs = 1, legal = false)
                    }
                    ScoreButton("B", Modifier.weight(1f)) {
                        addBall(runs = 1)
                    }
                    ScoreButton("LB", Modifier.weight(1f)) {
                        addBall(runs = 1)
                    }
                }

                Spacer(Modifier.height(18.dp))

                MainButton("UNDO LAST BALL") {
                    undoBall()
                }

                MainButton(
                    if (innings == 1) "NEXT INNINGS" else "FINISH MATCH"
                ) {
                    if (innings == 1) {
                        firstScore = score
                        innings = 2
                        score = 0
                        wickets = 0
                        balls = 0
                        undoList.clear()
                    } else {
                        page = "result"
                    }
                }

                BackButton { page = "home" }
            }

            "result" -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Heading("MATCH RESULT")

                Text(
                    "$teamA / $firstScore",
                    color = Color.White,
                    fontSize = 20.sp
                )

                Text(
                    "$teamB / $score",
                    color = Color.White,
                    fontSize = 20.sp
                )

                Spacer(Modifier.height(15.dp))

                Text(
                    when {
                        score > firstScore ->
                            "$battingTeam won!"
                        score == firstScore ->
                            "Match Tied"
                        else ->
                            "First innings team won!"
                    },
                    color = Gold,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                MainButton("HOME") { page = "home" }
            }

            "about" -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Heading("ABOUT APP")
                Text(
                    "Malik Shahbaz Score Card V2",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    "Cricket scoring application",
                    color = Gold
                )
                BackButton { page = "home" }
            }
        }
    }
}

@Composable
fun Heading(text: String) {
    Text(
        text,
        color = Gold,
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
fun MainButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue
        )
    ) {
        Text(text, color = Gold, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BackButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text("BACK", color = Gold)
    }
}

@Composable
fun ScoreButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(3.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue
        )
    ) {
        Text(text, color = Gold, fontWeight = FontWeight.Bold)
    }
}
