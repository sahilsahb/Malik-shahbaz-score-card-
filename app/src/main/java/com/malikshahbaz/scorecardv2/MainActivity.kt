package com.malikshahbaz.scorecardv2

import androidx.compose.foundation.verticalScroll
import android.os.Bundle
package com.malikshahbaz.scorecardv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
private val Background = Color(0xFFF1F4F8)

data class ScoreSnapshot(
    val runs: Int,
    val wickets: Int,
    val balls: Int,
    val extras: Int
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
    var battingFirst by remember { mutableStateOf("Team A") }

    var inningsNumber by remember { mutableIntStateOf(1) }
    var battingTeam by remember { mutableStateOf("Team A") }
    var bowlingTeam by remember { mutableStateOf("Team B") }

    var runs by remember { mutableIntStateOf(0) }
    var wickets by remember { mutableIntStateOf(0) }
    var balls by remember { mutableIntStateOf(0) }
    var extras by remember { mutableIntStateOf(0) }

    var firstInningsRuns by remember { mutableIntStateOf(0) }
    var secondInningsRuns by remember { mutableIntStateOf(0) }
    var matchResult by remember { mutableStateOf("") }

    val undoList = remember { mutableStateListOf<ScoreSnapshot>() }
    val history = remember { mutableStateListOf<String>() }

    fun startMatch() {
        inningsNumber = 1
        runs = 0
        wickets = 0
        balls = 0
        extras = 0
        firstInningsRuns = 0
        secondInningsRuns = 0
        undoList.clear()

        battingTeam = battingFirst
        bowlingTeam = if (battingFirst == teamA) teamB else teamA
        page = "score"
    }

    fun addDelivery(
        totalRuns: Int = 0,
        extraRuns: Int = 0,
        wicket: Boolean = false,
        legal: Boolean = true
    ) {
        if (wickets >= 10 || balls >= oversLimit * 6) return

        undoList.add(
            ScoreSnapshot(runs, wickets, balls, extras)
        )

        runs += totalRuns
        extras += extraRuns

        if (wicket) wickets++
        if (legal) balls++

        if (inningsNumber == 2 && runs > firstInningsRuns) {
            secondInningsRuns = runs
            matchResult = "$battingTeam won by ${10 - wickets} wickets"
            history.add(0, matchResult)
            page = "result"
        } else if (wickets >= 10 || balls >= oversLimit * 6) {
            if (inningsNumber == 1) {
                firstInningsRuns = runs
                inningsNumber = 2

                val oldBatting = battingTeam
                battingTeam = bowlingTeam
                bowlingTeam = oldBatting

                runs = 0
                wickets = 0
                balls = 0
                extras = 0
                undoList.clear()
            } else {
                secondInningsRuns = runs
                matchResult = when {
                    secondInningsRuns > firstInningsRuns ->
                        "$battingTeam won by ${10 - wickets} wickets"
                    secondInningsRuns < firstInningsRuns ->
                        "$bowlingTeam won by ${firstInningsRuns - secondInningsRuns} runs"
                    else -> "Match Tied"
                }
                history.add(0, matchResult)
                page = "result"
            }
        }
    }

    fun undoDelivery() {
        if (undoList.isNotEmpty()) {
            val old = undoList.removeAt(undoList.lastIndex)
            runs = old.runs
            wickets = old.wickets
            balls = old.balls
            extras = old.extras
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        when (page) {
            "home" -> HomeScreen(
                onNewMatch = { page = "setup" },
                onHistory = { page = "history" },
                onAbout = { page = "about" }
            )

            "setup" -> SetupScreen(
                teamA = teamA,
                teamB = teamB,
                format = format,
                overs = oversLimit,
                battingFirst = battingFirst,
                onTeamA = { teamA = it },
                onTeamB = { teamB = it },
                onFormat = {
                    format = it
                    oversLimit = when (it) {
                        "T20" -> 20
                        "ODI" -> 50
                        else -> 90
                    }
                },
                onOvers = { oversLimit = it },
                onBattingFirst = { battingFirst = it },
                onStart = { startMatch() },
                onBack = { page = "home" }
            )

            "score" -> ScoreScreen(
                battingTeam = battingTeam,
                bowlingTeam = bowlingTeam,
                inningsNumber = inningsNumber,
                runs = runs,
                wickets = wickets,
                balls = balls,
                extras = extras,
                oversLimit = oversLimit,
                target = if (inningsNumber == 2) firstInningsRuns + 1 else null,
                onRun = { addDelivery(totalRuns = it) },
                onWide = { addDelivery(1, 1, legal = false) },
                onNoBall = { addDelivery(1, 1, legal = false) },
                onBye = { addDelivery(1, 1) },
                onLegBye = { addDelivery(1, 1) },
                onWicket = { addDelivery(wicket = true) },
                onUndo = { undoDelivery() },
                onEndInnings = {
                    if (inningsNumber == 1) {
                        firstInningsRuns = runs
                        inningsNumber = 2
                        val oldBatting = battingTeam
                        battingTeam = bowlingTeam
                        bowlingTeam = oldBatting
                        runs = 0
                        wickets = 0
                        balls = 0
                        extras = 0
                        undoList.clear()
                    } else {
                        secondInningsRuns = runs
                        matchResult = when {
                            runs > firstInningsRuns ->
                                "$battingTeam won by ${10 - wickets} wickets"
                            runs < firstInningsRuns ->
                                "$bowlingTeam won by ${firstInningsRuns - runs} runs"
                            else -> "Match Tied"
                        }
                        history.add(0, matchResult)
                        page = "result"
                    }
                }
            )

            "result" -> ResultScreen(
                teamA = teamA,
                teamB = teamB,
                firstScore = firstInningsRuns,
                secondScore = secondInningsRuns,
                result = matchResult,
                onHome = { page = "home" },
                onHistory = { page = "history" }
            )

            "history" -> HistoryScreen(
                history = history.toList(),
                onBack = { page = "home" }
            )

            "about" -> AboutScreen(
                onBack = { page = "home" }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onNewMatch: () -> Unit,
    onHistory: () -> Unit,
    onAbout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏏", fontSize = 72.sp)
        Text(
            "MALIK SHAHBAZ",
            color = Gold,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            "SCORE CARD V2",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(40.dp))

        MenuButton("NEW MATCH", onNewMatch)
        MenuButton("MATCH HISTORY", onHistory)
        MenuButton("ABOUT APP", onAbout)

        Spacer(Modifier.height(25.dp))
        Text(
            "OFFLINE CRICKET SCORING",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun MenuButton(title: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
            .height(55.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = Navy
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SetupScreen(
    teamA: String,
    teamB: String,
    format: String,
    overs: Int,
    battingFirst: String,
    onTeamA: (String) -> Unit,
    onTeamB: (String) -> Unit,
    onFormat: (String) -> Unit,
    onOvers: (Int) -> Unit,
    onBattingFirst: (String) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
            .verticalScroll(
                androidx.compose.foundation.rememberScrollState()
            )
    ) {
        Text(
            "NEW MATCH",
            color = Navy,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = teamA,
            onValueChange = onTeamA,
            label = { Text("Team A name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = teamB,
            onValueChange = onTeamB,
            label = { Text("Team B name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "MATCH FORMAT",
            color = Navy,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("T20", "ODI", "Test").forEach { option ->
                FilterChip(
                    selected = format == option,
                    onClick = { onFormat(option) },
                    label = { Text(option) }
                )
            }
        }

        Spacer(Modifier.height(15.dp))

        Text(
            "OVERS: $overs",
            color = Navy,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5, 10, 20, 50, 90).forEach { value ->
                FilterChip(
                    selected = overs == value,
                    onClick = { onOvers(value) },
                    label = { Text("$value") }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "WHO WILL BAT FIRST?",
            color = Navy,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = battingFirst == teamA,
                onClick = { onBattingFirst(teamA) },
                label = { Text(teamA) }
            )

            FilterChip(
                selected = battingFirst == teamB,
                onClick = { onBattingFirst(teamB) },
                label = { Text(teamB) }
            )
        }

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = onStart,
            enabled = teamA.isNotBlank() &&
                teamB.isNotBlank() &&
                teamA != teamB,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            )
        ) {
            Text(
                "START MATCH",
                fontWeight = FontWeight.Bold
            )
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("BACK")
        }
    }
}

@Composable
fun ScoreScreen(
    battingTeam: String,
    bowlingTeam: String,
    inningsNumber: Int,
    runs: Int,
    wickets: Int,
    balls: Int,
    extras: Int,
    oversLimit: Int,
    target: Int?,
    onRun: (Int) -> Unit,
    onWide: () -> Unit,
    onNoBall: () -> Unit,
    onBye: () -> Unit,
    onLegBye: () -> Unit,
    onWicket: () -> Unit,
    onUndo: () -> Unit,
    onEndInnings: () -> Unit
) {
    val overText = "${balls / 6}.${balls % 6}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
    ) {
        Text(
            "LIVE MATCH • INNINGS $inningsNumber",
            color = Blue,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Navy
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    battingTeam,
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "$runs/$wickets",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    "Overs: $overText / $oversLimit",
                    color = Color.White,
                    fontSize = 17.sp
                )

                Text(
                    "Extras: $extras",
                    color = Gold
                )

                if (target != null) {
                    Text(
                        "Target: $target",
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Need ${maxOf(0, target - runs)} runs",
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "RUNS",
            color = Navy,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            (0..6).forEach { value ->
                Button(
                    onClick = { onRun(value) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue
                    )
                ) {
                    Text("$value")
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "EXTRAS",
            color = Navy,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onWide,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Navy
                )
            ) { Text("WD") }

            Button(
                onClick = onNoBall,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Navy
                )
            ) { Text("NB") }

            Button(
                onClick = onBye,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Navy
                )
            ) { Text("B") }

            Button(
                onClick = onLegBye,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Navy
                )
            ) { Text("LB") }
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onWicket,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB3261E)
            )
        ) {
            Text("WICKET")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onUndo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("UNDO LAST BALL")
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onEndInnings,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Navy
            )
        ) {
            Text(
                if (inningsNumber == 1) "END 1ST INNINGS"
                else "END MATCH"
            )
        }

        Text(
            "Bowling: $bowlingTeam",
            color = Blue,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun ResultScreen(
    teamA: String,
    teamB: String,
    firstScore: Int,
    secondScore: Int,
    result: String,
    onHome: () -> Unit,
    onHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏆", fontSize = 65.sp)

        Text(
            "MATCH RESULT",
            color = Gold,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(25.dp))

        Text(
            "$firstScore  -  $secondScore",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        Text(
            result,
            color = Gold,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(35.dp))

        MenuButton("MATCH HISTORY", onHistory)
        MenuButton("HOME", onHome)
    }
}

@Composable
fun HistoryScreen(
    history: List<String>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
    ) {
        Text(
            "MATCH HISTORY",
            color = Navy,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(15.dp))

        if (history.isEmpty()) {
            Text("No matches completed yet.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(history) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Text(
                            item,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("BACK HOME")
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏏", fontSize = 65.sp)

        Text(
            "MALIK SHAHBAZ",
            color = Gold,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "SCORE CARD V2",
            color = Color.White,
            fontSize = 20.sp
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "An offline cricket scoring application.",
            color = Color.White
        )

        Text(
            "Version 2.0",
            color = Gold
        )

        Spacer(Modifier.height(30.dp))

        MenuButton("BACK", onBack)
    }
}
