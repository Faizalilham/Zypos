package dev.faizal.core.common.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.annotation.RequiresApi
import dev.faizal.core.domain.model.report.*
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PdfReportGenerator() {

    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 40f
    private val contentWidth = pageWidth - (margin * 2)

    // Colors
    private val primaryColor = Color.parseColor("#2196F3")
    private val secondaryColor = Color.parseColor("#757575")
    private val accentColor = Color.parseColor("#4CAF50")
    private val lightGray = Color.parseColor("#F5F5F5")
    private val darkGray = Color.parseColor("#212121")

    // Paint objects
    private val titlePaint = Paint().apply {
        color = darkGray
        textSize = 24f
        val paint = Paint()
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val headerPaint = Paint().apply {
        color = darkGray
        textSize = 18f
        val paint = Paint()
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val subHeaderPaint = Paint().apply {
        color = secondaryColor
        textSize = 14f
        val paint = Paint()
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val normalPaint = Paint().apply {
        color = darkGray
        textSize = 12f
        isAntiAlias = true
    }

    private val smallPaint = Paint().apply {
        color = secondaryColor
        textSize = 10f
        isAntiAlias = true
    }

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateReport(
        outputFile: File,
        year: Int,
        month: Int,
        report: CompleteMonthlyReport
    ) {
        val document = PdfDocument()
        var yPosition = margin

        // ==================== PAGE 1: OVERVIEW ====================
        val page1 = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        )
        val canvas = page1.canvas

        // Header with company info
        yPosition = drawHeader(canvas, yPosition, month, year)
        yPosition += 20f

        // Summary cards
        yPosition = drawSummaryCards(canvas, yPosition, report.monthlySales)
        yPosition += 30f

        // Sales growth indicator
        yPosition = drawGrowthIndicator(canvas, yPosition, report.growth)
        yPosition += 30f

        // Daily sales chart
        yPosition = drawDailySalesChart(canvas, yPosition, report.dailySales)

        document.finishPage(page1)

        // ==================== PAGE 2: TOP PRODUCTS ====================
        val page2 = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        )
        val canvas2 = page2.canvas
        yPosition = margin

        yPosition = drawPageHeader(canvas2, yPosition, "Top Products")
        yPosition += 20f

        yPosition = drawTopProducts(canvas2, yPosition, report.topProducts)

        document.finishPage(page2)

        // ==================== PAGE 3: CATEGORY BREAKDOWN ====================
        val page3 = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
        )
        val canvas3 = page3.canvas
        yPosition = margin

        yPosition = drawPageHeader(canvas3, yPosition, "Category Sales")
        yPosition += 20f

        yPosition = drawCategorySales(canvas3, yPosition, report.categorySales)
        yPosition += 30f

        // Footer
        drawFooter(canvas3, year, month)

        document.finishPage(page3)

        // Save document
        document.writeTo(FileOutputStream(outputFile))
        document.close()
    }

    private fun drawHeader(canvas: Canvas, startY: Float, month: Int, year: Int): Float {
        var y = startY

        // Company logo area (placeholder)
        val logoPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            margin, y, margin + 60f, y + 60f,
            8f, 8f, logoPaint
        )

        // Company name
        canvas.drawText("ZYPOS", margin + 75f, y + 25f, titlePaint)

        // Report title
        val monthName = getMonthName(month)
        canvas.drawText(
            "Monthly Sales Report",
            margin + 75f,
            y + 50f,
            headerPaint
        )

        y += 70f

        // Period
        canvas.drawText(
            "Period: $monthName $year",
            margin,
            y,
            normalPaint
        )

        // Generated date
        val generatedDate = dateFormat.format(Date())
        canvas.drawText(
            "Generated: $generatedDate",
            pageWidth - margin - 150f,
            y,
            smallPaint
        )

        y += 10f

        // Divider line
        val linePaint = Paint().apply {
            color = lightGray
            strokeWidth = 2f
        }
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)

        return y + 10f
    }

    private fun drawSummaryCards(
        canvas: Canvas,
        startY: Float,
        monthlySales: MonthlySalesReport?
    ): Float {
        if (monthlySales == null) return startY

        val cardWidth = (contentWidth - 20f) / 3
        val cardHeight = 80f
        var y = startY

        // Background rectangles
        val cardPaint = Paint().apply {
            color = lightGray
            style = Paint.Style.FILL
        }

        val cards = listOf(
            Triple("Total Revenue", numberFormat.format(monthlySales.totalAmount), accentColor),
            Triple("Total Orders", "${monthlySales.orderCount}", primaryColor),
            Triple("Avg Order Value", numberFormat.format(monthlySales.totalAmount / monthlySales.orderCount), Color.parseColor("#FF9800"))
        )

        cards.forEachIndexed { index, (label, value, color) ->
            val x = margin + (index * (cardWidth + 10f))

            // Card background
            canvas.drawRoundRect(
                x, y, x + cardWidth, y + cardHeight,
                12f, 12f, cardPaint
            )

            // Color accent bar
            val accentPaint = Paint().apply {
                this.color = color
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                x, y, x + cardWidth, y + 5f,
                12f, 12f, accentPaint
            )

            // Label
            canvas.drawText(label, x + 15f, y + 30f, smallPaint)

            // Value
            val valuePaint = Paint().apply {
                this.color = color
                textSize = 18f
                val paint = Paint()
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(value, x + 15f, y + 60f, valuePaint)
        }

        return y + cardHeight
    }

    private fun drawGrowthIndicator(
        canvas: Canvas,
        startY: Float,
        growth: SalesGrowth
    ): Float {
        var y = startY

        // Title
        canvas.drawText("Growth vs Previous Month", margin, y, subHeaderPaint)
        y += 25f

        // Growth box
        val boxWidth = 200f
        val boxHeight = 60f
        val boxColor = if (growth.isPositive) Color.parseColor("#E8F5E9") else Color.parseColor("#FFEBEE")
        val textColor = if (growth.isPositive) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")

        val boxPaint = Paint().apply {
            color = boxColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            margin, y, margin + boxWidth, y + boxHeight,
            8f, 8f, boxPaint
        )

        // Arrow icon (simple triangle)
        val arrowPaint = Paint().apply {
            color = textColor
            style = Paint.Style.FILL
        }
        val path = Path()
        val arrowX = margin + 20f
        val arrowY = y + 30f
        if (growth.isPositive) {
            // Up arrow
            path.moveTo(arrowX, arrowY + 5f)
            path.lineTo(arrowX - 8f, arrowY - 5f)
            path.lineTo(arrowX + 8f, arrowY - 5f)
        } else {
            // Down arrow
            path.moveTo(arrowX, arrowY - 5f)
            path.lineTo(arrowX - 8f, arrowY + 5f)
            path.lineTo(arrowX + 8f, arrowY + 5f)
        }
        path.close()
        canvas.drawPath(path, arrowPaint)

        // Percentage
        val percentPaint = Paint().apply {
            color = textColor
            textSize = 24f
            val paint = Paint()
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val percentText = String.format("%.1f%%", Math.abs(growth.growthPercentage))
        canvas.drawText(percentText, margin + 45f, y + 40f, percentPaint)

        // Amount
        canvas.drawText(
            numberFormat.format(Math.abs(growth.growthAmount)),
            margin + 10f,
            y + boxHeight - 10f,
            smallPaint
        )

        return y + boxHeight
    }

    private fun drawDailySalesChart(
        canvas: Canvas,
        startY: Float,
        dailySales: List<DailySalesReport>
    ): Float {
        var y = startY

        // Title
        canvas.drawText("Daily Sales Overview", margin, y, subHeaderPaint)
        y += 30f

        if (dailySales.isEmpty()) return y

        val chartHeight = 200f
        val chartWidth = contentWidth
        val barWidth = chartWidth / dailySales.size - 5f
        val maxAmount = dailySales.maxOfOrNull { it.totalAmount } ?: 1.0

        // Draw axes
        val axisPaint = Paint().apply {
            color = secondaryColor
            strokeWidth = 1f
        }
        canvas.drawLine(margin, y + chartHeight, pageWidth - margin, y + chartHeight, axisPaint) // X-axis

        // Draw bars
        dailySales.forEachIndexed { index, day ->
            val barHeight = ((day.totalAmount / maxAmount) * chartHeight).toFloat()
            val x = margin + (index * (barWidth + 5f))

            // Bar
            val barPaint = Paint().apply {
                color = primaryColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                x, y + chartHeight - barHeight,
                x + barWidth, y + chartHeight,
                4f, 4f, barPaint
            )

            // Day label
            if (index % 3 == 0) { // Show every 3rd label to avoid crowding
                canvas.drawText(
                    day.dayOfMonth.toString(),
                    x + barWidth / 2 - 5f,
                    y + chartHeight + 15f,
                    smallPaint
                )
            }
        }

        // Y-axis labels
        val steps = 5
        for (i in 0..steps) {
            val value = (maxAmount / steps) * i
            val labelY = y + chartHeight - ((chartHeight / steps) * i)
            canvas.drawText(
                String.format("%.0fK", value / 1000),
                margin - 35f,
                labelY + 5f,
                smallPaint
            )
        }

        return y + chartHeight + 30f
    }

    private fun drawPageHeader(canvas: Canvas, startY: Float, title: String): Float {
        var y = startY

        // Mini header
        canvas.drawText("ZYPOS Report", margin, y, normalPaint)
        y += 20f

        // Title
        canvas.drawText(title, margin, y, headerPaint)
        y += 10f

        // Divider
        val linePaint = Paint().apply {
            color = lightGray
            strokeWidth = 2f
        }
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)

        return y + 10f
    }

    private fun drawTopProducts(
        canvas: Canvas,
        startY: Float,
        topProducts: List<TopProductReport>
    ): Float {
        var y = startY

        // Table header
        val headerBg = Paint().apply {
            color = lightGray
            style = Paint.Style.FILL
        }
        canvas.drawRect(margin, y, pageWidth - margin, y + 30f, headerBg)

        canvas.drawText("Rank", margin + 10f, y + 20f, subHeaderPaint)
        canvas.drawText("Product", margin + 60f, y + 20f, subHeaderPaint)
        canvas.drawText("Orders", margin + 280f, y + 20f, subHeaderPaint)
        canvas.drawText("Revenue", margin + 380f, y + 20f, subHeaderPaint)

        y += 40f

        // Rows
        topProducts.take(10).forEachIndexed { index, product ->
            // Rank badge
            val rankPaint = Paint().apply {
                color = when (index) {
                    0 -> Color.parseColor("#FFD700") // Gold
                    1 -> Color.parseColor("#C0C0C0") // Silver
                    2 -> Color.parseColor("#CD7F32") // Bronze
                    else -> primaryColor
                }
                style = Paint.Style.FILL
            }
            canvas.drawCircle(margin + 25f, y + 8f, 15f, rankPaint)

            val rankTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 12f
                val paint = Paint()
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("${index + 1}", margin + 25f, y + 13f, rankTextPaint)

            // Product name
            canvas.drawText(product.menuName, margin + 60f, y + 15f, normalPaint)

            // Orders
            canvas.drawText("${product.orderCount}", margin + 280f, y + 15f, normalPaint)

            // Revenue
            canvas.drawText(
                numberFormat.format(product.totalAmount),
                margin + 380f,
                y + 15f,
                normalPaint
            )

            y += 30f

            // Divider
            if (index < topProducts.size - 1) {
                val dividerPaint = Paint().apply {
                    color = Color.parseColor("#EEEEEE")
                    strokeWidth = 1f
                }
                canvas.drawLine(margin, y - 5f, pageWidth - margin, y - 5f, dividerPaint)
            }
        }

        return y
    }

    private fun drawCategorySales(
        canvas: Canvas,
        startY: Float,
        categorySales: List<CategorySalesReport>
    ): Float {
        var y = startY

        if (categorySales.isEmpty()) return y

        val totalSales = categorySales.sumOf { it.totalAmount }
        val chartWidth = 200f
        val barMaxWidth = contentWidth - chartWidth - 40f

        categorySales.forEach { category ->
            val percentage = (category.totalAmount / totalSales * 100).toFloat()
            val barWidth = (barMaxWidth * (percentage / 100))

            // Category name
            canvas.drawText(category.categoryName, margin, y + 15f, normalPaint)

            // Progress bar background
            val bgPaint = Paint().apply {
                color = lightGray
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                margin + chartWidth, y,
                margin + chartWidth + barMaxWidth, y + 25f,
                4f, 4f, bgPaint
            )

            // Progress bar fill
            val fillPaint = Paint().apply {
                color = primaryColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                margin + chartWidth, y,
                margin + chartWidth + barWidth, y + 25f,
                4f, 4f, fillPaint
            )

            // Percentage text
            canvas.drawText(
                String.format("%.1f%%", percentage),
                margin + chartWidth + barMaxWidth + 10f,
                y + 18f,
                normalPaint
            )

            // Revenue
            canvas.drawText(
                numberFormat.format(category.totalAmount),
                margin + 100f,
                y + 15f,
                smallPaint
            )

            y += 40f
        }

        return y
    }

    private fun drawFooter(canvas: Canvas, year: Int, month: Int) {
        val y = pageHeight - 30f

        // Divider
        val linePaint = Paint().apply {
            color = lightGray
            strokeWidth = 1f
        }
        canvas.drawLine(margin, y - 10f, pageWidth - margin, y - 10f, linePaint)

        // Footer text
        canvas.drawText(
            "ZYPOS © $year - Monthly Report - ${getMonthName(month)} $year",
            margin,
            y,
            smallPaint
        )

        canvas.drawText(
            "Page Generated: ${dateFormat.format(Date())}",
            pageWidth - margin - 150f,
            y,
            smallPaint
        )
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Januari"; 2 -> "Februari"; 3 -> "Maret"; 4 -> "April"
            5 -> "Mei"; 6 -> "Juni"; 7 -> "Juli"; 8 -> "Agustus"
            9 -> "September"; 10 -> "Oktober"; 11 -> "November"; 12 -> "Desember"
            else -> ""
        }
    }
}