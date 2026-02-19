package dev.faizal.order.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.faizal.core.domain.model.order.Order

@Composable
fun OrderDetailsPanel(
    isDineIn: Boolean,
    onDineInChange: (Boolean) -> Unit,
    orderItems: List<Order>,
    onQuantityChange: (Order, Int) -> Unit,
    onRemoveItem: (Order) -> Unit,
    onEditItem: (Order, Order) -> Unit,
    selectedPaymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    onMakeOrder: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(380.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {

        OrderDetailsPanelContent(
            isDineIn = isDineIn,
            onDineInChange = onDineInChange,
            orderItems = orderItems,
            onQuantityChange = onQuantityChange,
            onRemoveItem = onRemoveItem,
            modifier = Modifier.fillMaxSize(),
            onEditItem = onEditItem,
            selectedPaymentMethod = selectedPaymentMethod,
            onPaymentMethodChange = onPaymentMethodChange,
            onMakeOrder = onMakeOrder
        )
    }
}