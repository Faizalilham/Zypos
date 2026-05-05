package dev.faizal.features.settlement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.faizal.core.designsystem.PrimaryBlue
import dev.faizal.core.designsystem.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SettlementScreen(
    viewModel: SettlementViewModel = hiltViewModel(),
) {
    val state = viewModel.state

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBlue.copy(alpha = 0.05f))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        // ===================== HEADER =====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Settlement (Tutup Kasir)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                // Tampilkan nama toko di subtitle
                Text(
                    text = state.storeName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }

            OutlinedButton(
                onClick = viewModel::onPrintRecap,
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, PrimaryBlue),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryBlue,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Print",
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Print Rekap",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===================== TANGGAL + TOTAL TRANSAKSI =====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TANGGAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.todayDate,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL TRANSAKSI LUNAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.totalTransaksiLunas.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===================== DETAIL MENU + RINGKASAN KEUANGAN =====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // ---- Detail Menu Terjual ----
            Card(
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detail Menu Terjual Hari Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("MENU", style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary, modifier = Modifier.weight(2f),
                            letterSpacing = 1.sp)
                        Text("QTY", style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary, modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center, letterSpacing = 1.sp)
                        Text("TOTAL", style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary, modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.End, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.menuSalesList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Belum ada menu terjual hari ini.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        state.menuSalesList.forEach { item ->
                            MenuSalesRow(item = item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(2f),
                                color = MaterialTheme.colorScheme.onSurface)
                            Text(state.menuSalesList.sumOf { it.totalQty }.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text(formatRupiah(state.subtotalPenjualan),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1.5f),
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // ---- Ringkasan Keuangan (DYNAMIC dari settings) ----
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Ringkasan Keuangan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FinancialRow(
                        label = "Subtotal Penjualan",
                        value = formatRupiah(state.subtotalPenjualan),
                    )

                    // Service Charge — hanya tampil kalau aktif
                    if (state.serviceChargeEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FinancialRow(
                            label = "Service Charge (${state.serviceChargePercentage.formatPercent()}%)",
                            value = formatRupiah(state.totalServiceCharge),
                        )
                    }

                    // Tax — hanya tampil kalau aktif
                    if (state.taxEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FinancialRow(
                            label = "Pajak / Tax (${state.taxPercentage.formatPercent()}%)",
                            value = formatRupiah(state.totalPajak),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = PrimaryBlue.copy(alpha = 0.4f), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "TOTAL PENDAPATAN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        letterSpacing = 0.5.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatRupiah(state.totalPendapatan),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MenuSalesRow(item: MenuSalesSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = item.menuName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = item.categoryName,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        Text(
            text = item.totalQty.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = formatRupiah(item.totalAmount),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FinancialRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

private fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}

/** Format 10.0 → "10", 7.5 → "7.5" */
private fun Double.formatPercent(): String =
    if (this % 1.0 == 0.0) toInt().toString() else toString()