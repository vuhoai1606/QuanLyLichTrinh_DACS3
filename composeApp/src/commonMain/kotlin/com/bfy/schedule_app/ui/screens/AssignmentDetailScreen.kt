package com.bfy.schedule_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bfy.schedule_app.ui.components.BfyButton
import com.bfy.schedule_app.ui.components.BfyButtonStyle
import com.bfy.schedule_app.ui.components.BfyTopBar
import com.bfy.schedule_app.ui.data.FakeBfyData
import com.bfy.schedule_app.ui.theme.BfyTheme
import org.jetbrains.compose.resources.stringResource
import schedule_app.composeapp.generated.resources.Res
import schedule_app.composeapp.generated.resources.accept
import schedule_app.composeapp.generated.resources.assigned_by
import schedule_app.composeapp.generated.resources.assignment_detail
import schedule_app.composeapp.generated.resources.decline

data class AssignmentDetailScreen(
    private val assignmentId: String
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val assignment = FakeBfyData.assignments.firstOrNull { it.id == assignmentId } ?: FakeBfyData.assignments.first()

        Column(modifier = Modifier.fillMaxSize()) {
            BfyTopBar(
                title = stringResource(Res.string.assignment_detail),
                onBack = { navigator.pop() }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BfyTheme.dimens.spacing16),
                verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(BfyTheme.dimens.spacing16)) {
                        Text(assignment.title, style = MaterialTheme.typography.headlineMedium)
                        Text(assignment.description, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${stringResource(Res.string.assigned_by)}: ${assignment.assignedBy}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(assignment.dueText, style = MaterialTheme.typography.labelLarge)
                    }
                }

                BfyButton(
                    text = stringResource(Res.string.accept),
                    onClick = { navigator.pop() }
                )
                BfyButton(
                    text = stringResource(Res.string.decline),
                    onClick = { navigator.pop() },
                    style = BfyButtonStyle.SECONDARY
                )
            }
        }
    }
}
