package com.github.csande.persistentnotificationsapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.csande.persistentnotificationsapp.R
import com.github.csande.persistentnotificationsapp.ui.theme.QuickNoteTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    isEditing: Boolean,
    field: TextFieldValue,
    canSave: Boolean,
    onFieldChange: (TextFieldValue) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isEditing) R.string.title_edit_note else R.string.title_new_note
                        )
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = field,
                onValueChange = onFieldChange,
                label = { Text(stringResource(R.string.note_label)) },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )

            // Save on the left, Cancel on the right. Notes are deleted from their notification.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onSave, enabled = canSave) {
                    Text(stringResource(R.string.action_save))
                }
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditorScreenNewPreview() {
    QuickNoteTheme {
        EditorScreen(
            isEditing = false,
            field = TextFieldValue("Buy milk"),
            canSave = true,
            onFieldChange = {},
            onSave = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditorScreenEditPreview() {
    QuickNoteTheme {
        EditorScreen(
            isEditing = true,
            field = TextFieldValue("Buy milk"),
            canSave = false,
            onFieldChange = {},
            onSave = {},
            onCancel = {},
        )
    }
}
