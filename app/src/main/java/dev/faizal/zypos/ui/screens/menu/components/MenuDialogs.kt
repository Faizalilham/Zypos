package dev.faizal.zypos.ui.screens.menu.components


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import dev.faizal.zypos.domain.model.menu.Category
import dev.faizal.zypos.ui.screens.menu.MenuState

// ui/screens/menu/components/MenuDialogs.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuDialog(
    menuState: MenuState,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (Int) -> Unit,
    onPriceChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onCreate: () -> Unit
) {
    var expandedCategory by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f) // ✅ Max 90% tinggi layar
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ✅ HEADER - Fixed (tidak ikut scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Menu",
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

                // ✅ CONTENT - Scrollable
                Column(
                    modifier = Modifier
                        .weight(1f) // Ambil sisa space
                        .verticalScroll(rememberScrollState()) // ✅ Scrollable
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Image Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (menuState.selectedImageUri != null) {
                            AsyncImage(
                                model = menuState.selectedImageUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "📷", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to select image",
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Menu Name
                    OutlinedTextField(
                        value = menuState.name,
                        onValueChange = onNameChange,
                        label = { Text("Menu Name") },
                        placeholder = { Text("e.g., Americano, Cappuccino") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = menuState.nameError != null,
                        supportingText = {
                            if (menuState.nameError != null) {
                                Text(
                                    text = menuState.nameError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory }
                    ) {
                        OutlinedTextField(
                            value = categories.find { it.id == menuState.selectedCategoryId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                Icon(Icons.Default.KeyboardArrowDown, "Dropdown")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            isError = menuState.categoryError != null,
                            supportingText = {
                                if (menuState.categoryError != null) {
                                    Text(
                                        text = menuState.categoryError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            placeholder = { Text("Select category") }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(category.emoji, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(category.name)
                                        }
                                    },
                                    onClick = {
                                        onCategoryChange(category.id)
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price
                    OutlinedTextField(
                        value = menuState.price,
                        onValueChange = onPriceChange,
                        label = { Text("Price (Rp)") },
                        placeholder = { Text("e.g., 25000") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = menuState.priceError != null,
                        supportingText = {
                            if (menuState.priceError != null) {
                                Text(
                                    text = menuState.priceError,
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
                                text = "Make this menu available",
                                fontSize = 12.sp,
                                color = Color(0xFF999999)
                            )
                        }
                        Switch(
                            checked = menuState.isActive,
                            onCheckedChange = onIsActiveChange
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ✅ BUTTONS - Fixed di bawah (tidak ikut scroll)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
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
                            enabled = !menuState.isLoading,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (menuState.isLoading) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMenuDialog(
    menuState: MenuState,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (Int) -> Unit,
    onPriceChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onUpdate: () -> Unit
) {
    var expandedCategory by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f) // ✅ Max 90% tinggi layar
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ✅ HEADER - Fixed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Menu",
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

                // ✅ CONTENT - Scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()) // ✅ Scrollable
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Image Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (menuState.selectedImageUri != null) {
                            AsyncImage(
                                model = menuState.selectedImageUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "📷", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to change image",
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Menu Name
                    OutlinedTextField(
                        value = menuState.name,
                        onValueChange = onNameChange,
                        label = { Text("Menu Name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = menuState.nameError != null,
                        supportingText = {
                            if (menuState.nameError != null) {
                                Text(
                                    text = menuState.nameError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory }
                    ) {
                        OutlinedTextField(
                            value = categories.find { it.id == menuState.selectedCategoryId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                Icon(Icons.Default.KeyboardArrowDown, "Dropdown")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            isError = menuState.categoryError != null,
                            supportingText = {
                                if (menuState.categoryError != null) {
                                    Text(
                                        text = menuState.categoryError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(category.emoji, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(category.name)
                                        }
                                    },
                                    onClick = {
                                        onCategoryChange(category.id)
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price
                    OutlinedTextField(
                        value = menuState.price,
                        onValueChange = onPriceChange,
                        label = { Text("Price (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = menuState.priceError != null,
                        supportingText = {
                            if (menuState.priceError != null) {
                                Text(
                                    text = menuState.priceError,
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
                                text = "Make this menu available",
                                fontSize = 12.sp,
                                color = Color(0xFF999999)
                            )
                        }
                        Switch(
                            checked = menuState.isActive,
                            onCheckedChange = onIsActiveChange
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ✅ BUTTONS - Fixed di bawah
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
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
                            enabled = !menuState.isLoading,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (menuState.isLoading) {
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
}
@Composable
fun DeleteMenuDialog(
    menuName: String,
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
                text = "Delete Menu?",
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
                    text = "\"$menuName\"?",
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