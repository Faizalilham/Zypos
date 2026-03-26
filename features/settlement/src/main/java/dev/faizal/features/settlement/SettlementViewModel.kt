package dev.faizal.features.settlement

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.core.domain.model.order.PaymentStatus
import dev.faizal.core.domain.repository.OrderRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettlementViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    var state by mutableStateOf(SettlementState())
        private set

    init {
        loadTodayOrders()
    }

    private fun loadTodayOrders() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            orderRepository.getOrdersByDate(today).collectLatest { orders ->
                val paidOrders = orders.filter { it.paymentStatus == PaymentStatus.PAID }

                // Group by menuName, sum qty and total
                val menuSummary = paidOrders
                    .groupBy { it.menuName }
                    .map { (menuName, items) ->
                        MenuSalesSummary(
                            menuName = menuName,
                            categoryName = items.first().categoryName,
                            totalQty = items.sumOf { it.quantity },
                            totalAmount = items.sumOf { it.totalPrice }
                        )
                    }
                    .sortedByDescending { it.totalAmount }

                val subtotal = paidOrders.sumOf { it.totalPrice }
                val serviceCharge = subtotal * SERVICE_CHARGE_RATE
                val tax = subtotal * TAX_RATE
                val totalPendapatan = subtotal + serviceCharge + tax

                state = state.copy(
                    todayDate = today,
                    totalTransaksiLunas = paidOrders.map { it.orderNumber }.distinct().size,
                    menuSalesList = menuSummary,
                    subtotalPenjualan = subtotal,
                    totalServiceCharge = serviceCharge,
                    totalPajak = tax,
                    totalPendapatan = totalPendapatan,
                    isLoading = false
                )
            }
        }
    }

    fun onPrintRecap() {
        // TODO: Implement print functionality
    }

    companion object {
        const val SERVICE_CHARGE_RATE = 0.05  // 5%
        const val TAX_RATE = 0.10             // 10%
    }
}

data class MenuSalesSummary(
    val menuName: String,
    val categoryName: String,
    val totalQty: Int,
    val totalAmount: Double
)

data class SettlementState(
    val todayDate: String = "",
    val totalTransaksiLunas: Int = 0,
    val menuSalesList: List<MenuSalesSummary> = emptyList(),
    val subtotalPenjualan: Double = 0.0,
    val totalServiceCharge: Double = 0.0,
    val totalPajak: Double = 0.0,
    val totalPendapatan: Double = 0.0,
    val isLoading: Boolean = true
)