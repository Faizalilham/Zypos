package dev.faizal.features.menu.components.category


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.faizal.features.menu.CategoryState

@Composable
fun AddCategoryDialog(
    categoryState: CategoryState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
    onDisplayOrderChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onCreate: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Category",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Category Name
                OutlinedTextField(
                    value = categoryState.name,
                    onValueChange = onNameChange,
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g., Coffee, Tea, Snack") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = categoryState.nameError != null,
                    supportingText = {
                        if (categoryState.nameError != null) {
                            Text(
                                text = categoryState.nameError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Emoji
                OutlinedTextField(
                    value = categoryState.emoji,
                    onValueChange = onEmojiChange,
                    label = { Text("Emoji") },
                    placeholder = { Text("☕ 🍵 🍪") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = categoryState.emojiError != null,
                    supportingText = {
                        if (categoryState.emojiError != null) {
                            Text(
                                text = categoryState.emojiError,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Tap to paste emoji or type manually",
                                fontSize = 12.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display Order
                OutlinedTextField(
                    value = categoryState.displayOrder,
                    onValueChange = onDisplayOrderChange,
                    label = { Text("Display Order") },
                    placeholder = { Text("1") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = categoryState.displayOrderError != null,
                    supportingText = {
                        if (categoryState.displayOrderError != null) {
                            Text(
                                text = categoryState.displayOrderError,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Lower numbers appear first",
                                fontSize = 12.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Active Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "Make this category visible",
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                    Switch(
                        checked = categoryState.isActive,
                        onCheckedChange = onIsActiveChange
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onCreate,
                        modifier = Modifier.weight(1f),
                        enabled = !categoryState.isLoading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (categoryState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditCategoryDialog(
    categoryState: CategoryState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
    onDisplayOrderChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onUpdate: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Category",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Category Name
                OutlinedTextField(
                    value = categoryState.name,
                    onValueChange = onNameChange,
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = categoryState.nameError != null,
                    supportingText = {
                        if (categoryState.nameError != null) {
                            Text(
                                text = categoryState.nameError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Emoji
                OutlinedTextField(
                    value = categoryState.emoji,
                    onValueChange = onEmojiChange,
                    label = { Text("Emoji") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = categoryState.emojiError != null,
                    supportingText = {
                        if (categoryState.emojiError != null) {
                            Text(
                                text = categoryState.emojiError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display Order
                OutlinedTextField(
                    value = categoryState.displayOrder,
                    onValueChange = onDisplayOrderChange,
                    label = { Text("Display Order") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = categoryState.displayOrderError != null,
                    supportingText = {
                        if (categoryState.displayOrderError != null) {
                            Text(
                                text = categoryState.displayOrderError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Active Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "Make this category visible",
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                    Switch(
                        checked = categoryState.isActive,
                        onCheckedChange = onIsActiveChange
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onUpdate,
                        modifier = Modifier.weight(1f),
                        enabled = !categoryState.isLoading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (categoryState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteCategoryDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFF44336).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "⚠️",
                        fontSize = 28.sp
                    )
                }
            }
        },
        title = {
            Text(
                text = "Delete Category?",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete",
                    color = Color(0xFF666666)
                )
                Text(
                    text = "\"$categoryName\"?",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This action cannot be undone.",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}