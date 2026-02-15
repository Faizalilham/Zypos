package dev.faizal.zypos.domain.model.report

data class DailySalesReport(
    val date: String,
    val dayOfMonth: Int,
    val totalAmount: Double,
    val orderCount: Int
)

data class MonthlySalesReport(
    val month: Int,
    val year: Int,
    val totalAmount: Double,
    val orderCount: Int,
    val productCount: Int,
    val customerCount: Int,
    val netProfit: Double
)

data class CategorySalesReport(
    val categoryName: String,
    val totalAmount: Double,
    val orderCount: Int
)

data class TopProductReport(
    val menuName: String,
    val categoryName: String,
    val orderCount: Int,
    val totalAmount: Double,
    val imageUri: String? = null,
    val imageResourceId: Int? = null
)

data class SalesGrowth(
    val currentMonthSales: Double,
    val previousMonthSales: Double,
    val growthAmount: Double,
    val growthPercentage: Double,
    val isPositive: Boolean
)

data class CompleteMonthlyReport(
    val monthlySales: MonthlySalesReport?,
    val dailySales: List<DailySalesReport>,
    val topProducts: List<TopProductReport>,
    val categorySales: List<CategorySalesReport>,
    val growth: SalesGrowth
)