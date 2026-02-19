package dev.faizal.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.faizal.core.designsystem.R
import dev.faizal.ui.navigation.MainRoute
@Composable
fun Sidebar(
    isCompact: Boolean = false,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    currentRoute: MainRoute = MainRoute.Overview,
    onNavigate: (MainRoute) -> Unit = {}
) {
    var isTransactionExpanded by remember { mutableStateOf(false) }

    val sidebarWidth by animateDpAsState(
        targetValue = if (isCompact) 72.dp else 240.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sidebar_width"
    )

    Column(
        modifier = Modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(if (isCompact) 16.dp else 16.dp),
        horizontalAlignment = if (isCompact) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Logo
        if (isCompact) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.dashboard_filled),
                    contentDescription = "Logo",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.dashboard_filled),
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zypos",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dashboard
        SidebarMenuItem(
            title = "Dashboard",
            icon = R.drawable.dashboard_outlined,
            isSelected = currentRoute is MainRoute.Overview,
            isCompact = isCompact,
            onClick = { onNavigate(MainRoute.Overview) }
        )

        // Order List
        SidebarMenuItem(
            title = "Order List",
            icon = R.drawable.order_list_outlined,
            isSelected = currentRoute is MainRoute.Order,
            isCompact = isCompact,
            onClick = { onNavigate(MainRoute.Order) }
        )

        // Transaction dengan Submenu
        if (isCompact) {
            // Compact mode: langsung ke transaction tanpa submenu
            SidebarMenuItem(
                title = "Transaction",
                icon = R.drawable.ic_transaction,
                isSelected = currentRoute is MainRoute.TransactionAll ||
                        currentRoute is MainRoute.TransactionSales,
                isCompact = isCompact,
                onClick = { onNavigate(MainRoute.TransactionAll) }
            )
        } else {
            // Normal mode: dengan expandable submenu
            SidebarMenuItemWithSubmenu(
                title = "Transaction",
                icon = R.drawable.ic_transaction,
                isSelected = currentRoute is MainRoute.TransactionAll ||
                        currentRoute is MainRoute.TransactionSales,
                isExpanded = isTransactionExpanded,
                onToggleExpand = { isTransactionExpanded = !isTransactionExpanded },
                subMenuItems = listOf(
                    SubMenuItem("All Transaction", MainRoute.TransactionAll),
                    SubMenuItem("Product Favorite", MainRoute.TransactionSales),
                ),
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }

        // Menu
        SidebarMenuItem(
            title = "Menu",
            icon = R.drawable.menu_outlined,
            isSelected = currentRoute is MainRoute.Menu,
            isCompact = isCompact,
            onClick = { onNavigate(MainRoute.Menu) }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(16.dp))

        // Dark mode toggle
        if (isCompact) {
            IconButton(
                onClick = { onDarkModeChange(!isDarkMode) },
                modifier = Modifier.size(36.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (isDarkMode) R.drawable.moon_compact else R.drawable.sun_compact
                    ),
                    contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    "Dark Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                DarkModeSwitch(
                    checked = isDarkMode,
                    modifier = Modifier,
                    onCheckedChanged = onDarkModeChange
                )
            }
        }
    }
}

// Data class untuk submenu
data class SubMenuItem(
    val title: String,
    val route: MainRoute
)

// Composable untuk menu dengan submenu
@Composable
fun SidebarMenuItemWithSubmenu(
    title: String,
    icon: Int,
    isSelected: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    subMenuItems: List<SubMenuItem>,
    currentRoute: MainRoute,
    onNavigate: (MainRoute) -> Unit
) {
    Column {
        // Main menu item
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
                .clickable(onClick = onToggleExpand)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            // Arrow icon
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Submenu items
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(start = 32.dp)
            ) {
                subMenuItems.forEach { subItem ->
                    SubMenuItemView(
                        title = subItem.title,
                        isSelected = currentRoute == subItem.route,
                        onClick = { onNavigate(subItem.route) }
                    )
                }
            }
        }
    }
}

// Composable untuk submenu item
@Composable
fun SubMenuItemView(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFFE3F2FD).copy(alpha = 0.5f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun SidebarMenuItem(
    title: String,
    icon: Int,
    isSelected: Boolean,
    isCompact: Boolean = false,
    onClick: () -> Unit = {}
) {
    if (isCompact) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}