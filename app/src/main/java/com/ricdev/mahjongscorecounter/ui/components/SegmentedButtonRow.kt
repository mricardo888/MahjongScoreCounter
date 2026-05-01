package com.ricdev.mahjongscorecounter.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SegmentedButtonRow(
    options: List<T>,
    selected: T?,
    onSelectedChange: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    semanticLabel: (@Composable (T) -> String)? = null,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, option ->
            val description = semanticLabel?.invoke(option)
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelectedChange(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                modifier = if (description != null) {
                    Modifier.semantics { contentDescription = description }
                } else {
                    Modifier
                },
            ) {
                Text(label(option))
            }
        }
    }
}
