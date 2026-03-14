package com.example.taskblocks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taskblocks.R
import com.example.taskblocks.data.BlockStyle
import com.example.taskblocks.ui.theme.WarmBackground

@Composable
fun BlockStyleSelectionScreen(
    currentStyle: BlockStyle,
    onStyleSelected: (BlockStyle) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(WarmBackground)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.IconButton(onClick = onBack) {
                Text("←", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = stringResource(R.string.block_style),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BlockStyle.entries.forEach { style ->
                val isSelected = style == currentStyle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStyleSelected(style) }
                        .then(
                            if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            else Modifier
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp, 48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    when (style) {
                                        BlockStyle.DEFAULT -> Modifier
                                            .background(Color(0xFF4CAF50))
                                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                        BlockStyle.CUBE_3D -> Modifier
                                            .shadow(4.dp, RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFF66BB6A),
                                                        Color(0xFF2E7D32)
                                                    )
                                                )
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        BlockStyle.METAL -> Modifier
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFFB0BEC5),
                                                        Color(0xFF546E7A),
                                                        Color(0xFF37474F)
                                                    )
                                                )
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        BlockStyle.WOOD -> Modifier
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFF8D6E63),
                                                        Color(0xFF5D4037),
                                                        Color(0xFF3E2723)
                                                    )
                                                )
                                            )
                                            .border(1.dp, Color(0xFFA1887F).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(style.labelResId),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
