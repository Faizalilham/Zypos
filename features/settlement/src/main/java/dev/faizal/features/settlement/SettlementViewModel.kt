package dev.faizal.features.settlement

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizal.core.domain.model.order.PaymentStatus
import dev.faizal.core.domain.model.store.Store
import dev.faizal.core.domain.repository.OrderRepository
import dev.faizal.core.domain.repository.StoreRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettlementViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val storeSettingsRepository: StoreRepository,
) : ViewModel() {

    var state by mutableStateOf(SettlementState())
        private set

    init {
        loadTodayOrdersWithSettings()
    }

    /**
     * Combine order data + store settings agar perhitungan tax/service charge
     * pakai persentase dari pengaturan toko (bukan hardcoded 5%/10%).
     *
     * Kalau settings null (user belum onboarding), pakai 0% — better safe than wrong.
     */
    private fun loadTodayOrdersWithSettings() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            combine(
                orderRepository.getOrdersByDate(today),
                storeSettingsRepository.observeSettings(),
            ) { orders, settings ->
                Pair(orders, settings)
            }.collectLatest { (orders, settings) ->
                val paidOrders = orders.filter { it.paymentStatus == PaymentStatus.PAID }

                val menuSummary = paidOrders
                    .groupBy { it.menuName }
                    .map { (menuName, items) ->
                        MenuSalesSummary(
                            menuName = menuName,
                            categoryName = items.first().categoryName,
                            totalQty = items.sumOf { it.quantity },
                            totalAmount = items.sumOf { it.totalPrice },
                        )
                    }
                    .sortedByDescending { it.totalAmount }

                val subtotal = paidOrders.sumOf { it.totalPrice }

                // Pakai persentase dari settings, atau 0 kalau setting tidak aktif/null
                val serviceChargeRate = settings.activeServiceChargeRate()
                val taxRate = settings.activeTaxRate()

                val serviceCharge = subtotal * serviceChargeRate
                val tax = subtotal * taxRate
                val totalPendapatan = subtotal + serviceCharge + tax

                state = state.copy(
                    todayDate = today,
                    totalTransaksiLunas = paidOrders.map { it.orderNumber }.distinct().size,
                    menuSalesList = menuSummary,
                    subtotalPenjualan = subtotal,
                    totalServiceCharge = serviceCharge,
                    totalPajak = tax,
                    totalPendapatan = totalPendapatan,
                    // Untuk label dynamic di UI
                    taxEnabled = settings?.taxEnabled ?: false,
                    taxPercentage = settings?.taxPercentage ?: 0.0,
                    serviceChargeEnabled = settings?.serviceChargeEnabled ?: false,
                    serviceChargePercentage = settings?.serviceChargePercentage ?: 0.0,
                    storeName = settings?.storeName ?: "ZyPos",
                    isLoading = false,
                )
            }
        }
    }

    fun onPrintRecap() {
        // TODO: Implement print functionality
    }

    /**
     * Convert "10.0" (10%) jadi "0.10" (rate untuk multiplier).
     * Return 0.0 kalau tax tidak aktif atau settings null.
     */
    private fun Store?.activeTaxRate(): Double {
        if (this == null || !taxEnabled) return 0.0
        return taxPercentage / 100.0
    }

    private fun Store?.activeServiceChargeRate(): Double {
        if (this == null || !serviceChargeEnabled) return 0.0
        return serviceChargePercentage / 100.0
    }
}

data class MenuSalesSummary(
    val menuName: String,
    val categoryName: String,
    val totalQty: Int,
    val totalAmount: Double,
)

data class SettlementState(
    val todayDate: String = "",
    val totalTransaksiLunas: Int = 0,
    val menuSalesList: List<MenuSalesSummary> = emptyList(),
    val subtotalPenjualan: Double = 0.0,
    val totalServiceCharge: Double = 0.0,
    val totalPajak: Double = 0.0,
    val totalPendapatan: Double = 0.0,
    val isLoading: Boolean = true,

    // Settings-driven fields untuk UI
    val taxEnabled: Boolean = false,
    val taxPercentage: Double = 0.0,
    val serviceChargeEnabled: Boolean = false,
    val serviceChargePercentage: Double = 0.0,
    val storeName: String = "ZyPos",
)