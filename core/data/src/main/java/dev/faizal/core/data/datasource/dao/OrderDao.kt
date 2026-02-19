package dev.faizal.core.data.datasource.dao

import androidx.room.*
import dev.faizal.core.data.datasource.entity.CategorySales
import dev.faizal.core.data.datasource.entity.DailySales
import dev.faizal.core.data.datasource.entity.MonthlySales
import dev.faizal.core.data.datasource.entity.OrderEntity
import dev.faizal.core.data.datasource.entity.OrderWithDetails
import dev.faizal.core.data.datasource.entity.TopProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>): List<Long>
    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET orderStatus = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET paymentStatus = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updatePaymentStatus(orderId: Int, status: String, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Int)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()

    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Int): OrderEntity?

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber")
    suspend fun getOrderByNumber(orderNumber: String): OrderEntity?

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderWithDetails(orderId: Int): OrderWithDetails?

    @Query("""
        SELECT * FROM orders 
        WHERE orderDate BETWEEN :startDate AND :endDate 
        ORDER BY orderDate DESC
    """)
    fun getOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<OrderEntity>>

    @Query("""
        SELECT * FROM orders 
        WHERE orderStatus = :status 
        ORDER BY orderDate DESC
    """)
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

    @Query("""
        SELECT * FROM orders 
        WHERE paymentStatus = :status 
        ORDER BY orderDate DESC
    """)
    fun getOrdersByPaymentStatus(status: String): Flow<List<OrderEntity>>

    @Query("""
        SELECT * FROM orders 
        WHERE customerName LIKE '%' || :query || '%' 
        OR orderNumber LIKE '%' || :query || '%'
        OR menuName LIKE '%' || :query || '%'
        ORDER BY orderDate DESC
    """)
    fun searchOrders(query: String): Flow<List<OrderEntity>>

    @Query("""
        SELECT 
            strftime('%d/%m/%Y', orderDate / 1000, 'unixepoch', 'localtime') as date,
            CAST(strftime('%d', orderDate / 1000, 'unixepoch', 'localtime') AS INTEGER) as dayOfMonth,
            SUM(totalPrice) as totalAmount,
            COUNT(DISTINCT orderNumber) as orderCount
        FROM orders
        WHERE strftime('%Y-%m', orderDate / 1000, 'unixepoch', 'localtime') = :yearMonth
        AND orderStatus = 'COMPLETED'
        AND paymentStatus = 'PAID'
        GROUP BY date, dayOfMonth
        ORDER BY dayOfMonth ASC
    """)
    fun getDailySalesByMonth(yearMonth: String): Flow<List<DailySales>>


    @Query("""
        SELECT 
            CAST(strftime('%m', orderDate / 1000, 'unixepoch', 'localtime') AS INTEGER) as month,
            CAST(strftime('%Y', orderDate / 1000, 'unixepoch', 'localtime') AS INTEGER) as year,
            SUM(totalPrice) as totalAmount,
            COUNT(DISTINCT orderNumber) as orderCount,
            SUM(quantity) as productCount,
            COUNT(DISTINCT customerName) as customerCount,
            SUM(totalPrice * 0.3) as netProfit
        FROM orders
        WHERE strftime('%Y-%m', orderDate / 1000, 'unixepoch', 'localtime') = :yearMonth
        AND orderStatus = 'COMPLETED'
        AND paymentStatus = 'PAID'
        GROUP BY month, year
    """)
    suspend fun getMonthlySales(yearMonth: String): MonthlySales? // Format: "2024-05"


    @Query("""
        SELECT 
            o.menuName,
            o.categoryName,
            COUNT(*) as orderCount,
            SUM(o.totalPrice) as totalAmount,
            m.imageUri as imageUri,
            m.imageResourceId as imageResourceId
        FROM orders o
        LEFT JOIN menu_items m ON o.menuId = m.id
        WHERE strftime('%Y-%m', o.orderDate / 1000, 'unixepoch', 'localtime') = :yearMonth
        AND o.orderStatus = 'COMPLETED'
        AND o.paymentStatus = 'PAID'
        GROUP BY o.menuName, o.categoryName, m.imageUri, m.imageResourceId
        ORDER BY orderCount DESC
        LIMIT :limit
    """)
    fun getTopProductsByMonth(yearMonth: String, limit: Int = 3): Flow<List<TopProduct>>

    @Query("""
        SELECT * FROM orders 
        WHERE strftime('%d/%m/%Y', orderDate / 1000, 'unixepoch', 'localtime') = :date
        ORDER BY orderDate DESC
    """)
    fun getOrdersByDate(date: String): Flow<List<OrderEntity>>

    @Transaction
    @Query("SELECT * FROM orders WHERE DATE(createdAt / 1000, 'unixepoch') = DATE('now')")
    fun getTodayOrdersWithDetails(): Flow<List<OrderWithDetails>>

    @Transaction
    @Query("SELECT COUNT(*) FROM orders WHERE DATE(createdAt / 1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayOrdersCount(): Int

    @Query("""
        SELECT 
            categoryName,
            SUM(totalPrice) as totalAmount,
            COUNT(*) as orderCount
        FROM orders
        WHERE strftime('%Y-%m', orderDate / 1000, 'unixepoch', 'localtime') = :yearMonth
        AND orderStatus = 'COMPLETED'
        AND paymentStatus = 'PAID'
        GROUP BY categoryName
        ORDER BY totalAmount DESC
    """)
    fun getCategorySalesByMonth(yearMonth: String): Flow<List<CategorySales>>

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int

    @Query("""
        SELECT COUNT(*) FROM orders 
        WHERE orderDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getOrderCountByDateRange(startDate: Long, endDate: Long): Int

    @Query("""
        SELECT MAX(CAST(SUBSTR(orderNumber, -3) AS INTEGER)) 
        FROM orders 
        WHERE DATE(orderDate / 1000, 'unixepoch', 'localtime') = DATE('now', 'localtime')
    """)
    suspend fun getLastOrderNumberToday(): Int?

    @Query("""
        SELECT 
            SUM(totalPrice) as totalAmount
        FROM orders
        WHERE strftime('%Y-%m', orderDate / 1000, 'unixepoch', 'localtime') = :yearMonth
        AND orderStatus = 'COMPLETED'
        AND paymentStatus = 'PAID'
    """)
    suspend fun getTotalSalesByMonth(yearMonth: String): Double?
}