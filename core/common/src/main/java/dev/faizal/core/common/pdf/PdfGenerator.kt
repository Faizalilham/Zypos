package dev.faizal.core.common.pdf

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import dev.faizal.core.domain.model.report.CompleteMonthlyReport
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PdfReportGenerator @Inject constructor() {

    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 40f

    fun generateReport(
        outputFile: File,
        year: Int,
        month: Int,
        report: CompleteMonthlyReport
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var yPosition = margin

        // Currency formatter
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
            maximumFractionDigits = 2
        }

        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, 1)
        }
        val monthName = dateFormat.format(calendar.time)

        // ==================== HEADER ====================
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = Color.BLACK
        }
        canvas.drawText("Monthly Sales Report", margin, yPosition, titlePaint)
        yPosition += 30f

        val subtitlePaint = Paint().apply {
            textSize = 16f
            color = Color.GRAY
        }
        canvas.drawText(monthName, margin, yPosition, subtitlePaint)
        yPosition += 40f

        // ==================== SUMMARY SECTION ====================
        val sectionTitlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.BLACK
        }
        canvas.drawText("Summary", margin, yPosition, sectionTitlePaint)
        yPosition += 30f

        val labelPaint = Paint().apply {
            textSize = 12f
            color = Color.DKGRAY
        }
        val valuePaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.BLACK
        }

        report.monthlySales?.let { sales ->
            // Total Sales Amount
            canvas.drawText("Total Sales Amount:", margin, yPosition, labelPaint)
            canvas.drawText(
                currencyFormat.format(sales.totalAmount),
                margin + 200f,
                yPosition,
                valuePaint
            )
            yPosition += 25f

            // Total Product Sales
            canvas.drawText("Total Product Sales:", margin, yPosition, labelPaint)
            canvas.drawText(
                "${sales.productCount} Items",
                margin + 200f,
                yPosition,
                valuePaint
            )
            yPosition += 25f

            // Total Customers
            canvas.drawText("Total Customers:", margin, yPosition, labelPaint)
            canvas.drawText(
                "${sales.customerCount} Persons",
                margin + 200f,
                yPosition,
                valuePaint
            )
            yPosition += 25f

            // Net Profit
            canvas.drawText("Net Profit:", margin, yPosition, labelPaint)
            canvas.drawText(
                currencyFormat.format(sales.netProfit),
                margin + 200f,
                yPosition,
                valuePaint
            )
            yPosition += 25f

            // Growth
            val growth = report.growth
            val growthColor = if (growth.isPositive) Color.rgb(76, 175, 80) else Color.RED
            val growthSign = if (growth.isPositive) "+" else ""

            canvas.drawText("Growth:", margin, yPosition, labelPaint)
            val growthPaint = Paint().apply {
                textSize = 14f
                isFakeBoldText = true
                color = growthColor
            }
            canvas.drawText(
                "$growthSign${currencyFormat.format(growth.growthAmount)} (${growthSign}${"%.2f".format(growth.growthPercentage)}%)",
                margin + 200f,
                yPosition,
                growthPaint
            )
            yPosition += 40f
        } ?: run {
            canvas.drawText("No data available for this month", margin, yPosition, labelPaint)
            yPosition += 40f
        }

        // ==================== TOP PRODUCTS SECTION ====================
        canvas.drawText("Top Favorite Products", margin, yPosition, sectionTitlePaint)
        yPosition += 30f

        // Table Header
        val headerPaint = Paint().apply {
            textSize = 11f
            isFakeBoldText = true
            color = Color.WHITE
        }
        val headerBgPaint = Paint().apply {
            color = Color.rgb(33, 150, 243)
            style = Paint.Style.FILL
        }

        canvas.drawRect(margin, yPosition - 15f, pageWidth - margin, yPosition + 5f, headerBgPaint)
        canvas.drawText("No", margin + 5f, yPosition, headerPaint)
        canvas.drawText("Product Name", margin + 40f, yPosition, headerPaint)
        canvas.drawText("Category", margin + 200f, yPosition, headerPaint)
        canvas.drawText("Orders", margin + 320f, yPosition, headerPaint)
        canvas.drawText("Total Sales", margin + 400f, yPosition, headerPaint)
        yPosition += 25f

        // Draw products
        val cellPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
        }

        report.topProducts.forEachIndexed { index, product ->
            if (yPosition > pageHeight - 100f) return@forEachIndexed

            canvas.drawText("${index + 1}", margin + 5f, yPosition, cellPaint)
            canvas.drawText(product.menuName, margin + 40f, yPosition, cellPaint)
            canvas.drawText(product.categoryName, margin + 200f, yPosition, cellPaint)
            canvas.drawText("${product.orderCount} times", margin + 320f, yPosition, cellPaint)
            canvas.drawText(currencyFormat.format(product.totalAmount), margin + 400f, yPosition, cellPaint)
            yPosition += 20f
        }

        if (report.topProducts.isEmpty()) {
            canvas.drawText("No products sold this month", margin + 5f, yPosition, cellPaint)
            yPosition += 20f
        }

        yPosition += 20f

        // ==================== CATEGORY SALES SECTION ====================
        if (yPosition < pageHeight - 200f) {
            canvas.drawText("Sales by Category", margin, yPosition, sectionTitlePaint)
            yPosition += 30f

            canvas.drawRect(margin, yPosition - 15f, pageWidth - margin, yPosition + 5f, headerBgPaint)
            canvas.drawText("Category", margin + 5f, yPosition, headerPaint)
            canvas.drawText("Total Sales", margin + 300f, yPosition, headerPaint)
            canvas.drawText("Order Count", margin + 450f, yPosition, headerPaint)
            yPosition += 25f

            report.categorySales.forEach { category ->
                if (yPosition > pageHeight - 100f) return@forEach

                canvas.drawText(category.categoryName, margin + 5f, yPosition, cellPaint)
                canvas.drawText(currencyFormat.format(category.totalAmount), margin + 300f, yPosition, cellPaint)
                canvas.drawText("${category.orderCount} orders", margin + 450f, yPosition, cellPaint)
                yPosition += 20f
            }

            if (report.categorySales.isEmpty()) {
                canvas.drawText("No category data available", margin + 5f, yPosition, cellPaint)
            }
        }

        // ==================== FOOTER ====================
        val footerPaint = Paint().apply {
            textSize = 10f
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
        }
        val generatedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText(
            "Generated on $generatedDate",
            pageWidth / 2f,
            pageHeight - 20f,
            footerPaint
        )

        pdfDocument.finishPage(page)

        FileOutputStream(outputFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }

        pdfDocument.close()
    }
}