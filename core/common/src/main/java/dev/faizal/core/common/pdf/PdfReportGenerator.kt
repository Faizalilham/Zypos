package dev.faizal.core.common.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dev.faizal.core.designsystem.R
import dev.faizal.core.domain.model.report.*
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PdfReportGenerator(private val context: Context) {

    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f
    private val contentWidth = pageWidth - (margin * 2)


    private val colorBrand       = Color.parseColor("#1A237E")
    private val colorAccent      = Color.parseColor("#2979FF")
    private val colorSuccess     = Color.parseColor("#00897B")
    private val colorSuccessBg   = Color.parseColor("#E0F2F1")
    private val colorDanger      = Color.parseColor("#E53935")
    private val colorDangerBg    = Color.parseColor("#FFEBEE")
    private val colorWarning     = Color.parseColor("#F57C00")
    private val colorInk         = Color.parseColor("#1C1C1E")
    private val colorSubtext     = Color.parseColor("#6B7280")
    private val colorBorder      = Color.parseColor("#E5E7EB")
    private val colorSurface     = Color.parseColor("#F9FAFB")
    private val colorWhite       = Color.WHITE

    private val cardColors = listOf(
        Color.parseColor("#2979FF"),
        Color.parseColor("#00897B"),
        Color.parseColor("#F57C00")
    )

    private val rankColors = listOf(
        Color.parseColor("#F9A825"),
        Color.parseColor("#90A4AE") ,
        Color.parseColor("#A1887F")
    )

    private fun boldPaint(size: Float, color: Int) = Paint().apply {
        this.color = color
        textSize = size
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private fun regularPaint(size: Float, color: Int) = Paint().apply {
        this.color = color
        textSize = size
        isAntiAlias = true
    }

    private val paintH1        = boldPaint(20f, colorInk)
    private val paintBody      = regularPaint(11f, colorInk)
    private val paintCaption   = regularPaint(9f, colorSubtext)
    private val paintLabelBold = boldPaint(11f, colorInk)

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat   = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))


    @RequiresApi(Build.VERSION_CODES.O)
    fun generateReport(outputFile: File, year: Int, month: Int, report: CompleteMonthlyReport) {
        val document = PdfDocument()


        val p1 = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        var y = margin
        y = drawCoverHeader(p1.canvas, month, year)
        y += 14f
        y = drawSummaryCards(p1.canvas, y, report.monthlySales)
        y += 14f
        y = drawGrowthIndicator(p1.canvas, y, report.growth)
        y += 14f
        drawDailySalesChart(p1.canvas, y, report.dailySales)
        drawFooter(p1.canvas, year, month, 1)
        document.finishPage(p1)


        val p2 = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create())
        y = margin
        y = drawPageHeader(p2.canvas, y, "Top Products", "Best-selling items this period")
        y += 12f
        drawTopProducts(p2.canvas, y, report.topProducts)
        drawFooter(p2.canvas, year, month, 2)
        document.finishPage(p2)


        val p3 = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create())
        y = margin
        y = drawPageHeader(p3.canvas, y, "Category Sales", "Revenue distribution by category")
        y += 12f
        drawCategorySales(p3.canvas, y, report.categorySales)
        drawFooter(p3.canvas, year, month, 3)
        document.finishPage(p3)

        document.writeTo(FileOutputStream(outputFile))
        document.close()
    }


    private fun drawCoverHeader(canvas: Canvas, month: Int, year: Int): Float {
        val heroH = 110f

        canvas.drawRect(0f, 0f, pageWidth.toFloat(), heroH,
            Paint().apply { color = colorBrand; style = Paint.Style.FILL })


        canvas.drawCircle(pageWidth - 40f, 0f, 80f,
            Paint().apply { color = colorAccent; style = Paint.Style.FILL; alpha = 70 })


        ContextCompat.getDrawable(context, R.drawable.logo_zypos)?.apply {
            setColorFilter(colorWhite, PorterDuff.Mode.SRC_IN)
            setBounds(margin.toInt(), 16, (margin + 42  ).toInt(), 58)
            draw(canvas)
        }


        canvas.drawText("ZYPOS", margin + 52f, 46f, boldPaint(22f, colorWhite))


        canvas.drawText("MONTHLY SALES REPORT", margin + 52f, 62f,
            regularPaint(10f, Color.parseColor("#90CAF9")))

        val monthName  = getMonthName(month)
        val periodText = "$monthName $year"
        val pillPaint  = boldPaint(10f, colorWhite)
        val pillW      = pillPaint.measureText(periodText) + 24f
        canvas.drawRoundRect(margin, 72f, margin + pillW, 92f, 10f, 10f,
            Paint().apply { color = colorAccent; style = Paint.Style.FILL; alpha = 200 })
        canvas.drawText(periodText, margin + 12f, 86f, pillPaint)

        canvas.drawText("Generated: ${dateFormat.format(Date())}",
            pageWidth - margin - 140f, 88f,
            regularPaint(8.5f, Color.parseColor("#90CAF9")))

        return heroH + 6f
    }


    private fun drawPageHeader(
        canvas: Canvas, startY: Float, title: String, subtitle: String
    ): Float {
        var y = startY

        canvas.drawRoundRect(margin, y, margin + 4f, y + 38f, 2f, 2f,
            Paint().apply { color = colorAccent; style = Paint.Style.FILL })
        canvas.drawRect(margin + 2f, y, margin + 4f, y + 38f,
            Paint().apply { color = colorAccent; style = Paint.Style.FILL })


        canvas.drawText("ZYPOS Report", margin + 14f, y + 13f, paintCaption)


        canvas.drawText(title, margin + 14f, y + 32f, paintH1)

        y += 50f

        canvas.drawText(subtitle, margin, y, regularPaint(10f, colorSubtext))
        y += 16f

        drawDivider(canvas, y)
        return y + 12f
    }

    private fun drawSummaryCards(canvas: Canvas, startY: Float, sales: MonthlySalesReport?): Float {
        if (sales == null) return startY

        val cardW = (contentWidth - 16f) / 3
        val cardH = 76f
        val y     = startY

        val cards = listOf(
            Triple("Total Revenue",   numberFormat.format(sales.totalAmount), 0),
            Triple("Total Orders",    "${sales.orderCount} orders",           1),
            Triple("Avg Order Value", numberFormat.format(
                if (sales.orderCount > 0) sales.totalAmount / sales.orderCount else 0.0), 2)
        )

        cards.forEachIndexed { i, (label, value, ci) ->
            val x      = margin + i * (cardW + 8f)
            val accent = cardColors[ci]


            canvas.drawRoundRect(x + 2f, y + 3f, x + cardW + 2f, y + cardH + 3f, 10f, 10f,
                Paint().apply { color = Color.parseColor("#DCDCDC"); style = Paint.Style.FILL })


            canvas.drawRoundRect(x, y, x + cardW, y + cardH, 10f, 10f,
                Paint().apply { color = colorWhite; style = Paint.Style.FILL })


            canvas.drawRoundRect(x, y, x + 4f, y + cardH, 10f, 10f,
                Paint().apply { color = accent; style = Paint.Style.FILL })
            canvas.drawRect(x + 2f, y, x + 4f, y + cardH,
                Paint().apply { color = accent; style = Paint.Style.FILL })


            canvas.drawCircle(x + cardW - 22f, y + 22f, 16f,
                Paint().apply { color = accent; style = Paint.Style.FILL; alpha = 28 })

            canvas.drawText(label, x + 14f, y + 22f, paintCaption)


            canvas.drawText(value, x + 14f, y + 44f, boldPaint(12f, accent))

            canvas.drawText("This month", x + 14f, y + 62f, regularPaint(8f, colorSubtext))
        }

        return y + cardH
    }

    private fun drawGrowthIndicator(canvas: Canvas, startY: Float, growth: SalesGrowth): Float {
        var y = startY
        drawSectionLabel(canvas, y, "MONTH-OVER-MONTH GROWTH")
        y += 28f

        val positive  = growth.isPositive
        val bgColor   = if (positive) colorSuccessBg else colorDangerBg
        val lineColor = if (positive) colorSuccess   else colorDanger
        val boxH      = 52f


        canvas.drawRoundRect(margin, y, margin + contentWidth, y + boxH, 8f, 8f,
            Paint().apply { color = bgColor; style = Paint.Style.FILL })


        canvas.drawRoundRect(margin, y, margin + 4f, y + boxH, 8f, 8f,
            Paint().apply { color = lineColor; style = Paint.Style.FILL })
        canvas.drawRect(margin + 2f, y, margin + 4f, y + boxH,
            Paint().apply { color = lineColor; style = Paint.Style.FILL })

        val path = Path()
        val ax = margin + 22f; val ay = y + boxH / 2
        if (positive) {
            path.moveTo(ax, ay - 7f); path.lineTo(ax - 6f, ay + 5f); path.lineTo(ax + 6f, ay + 5f)
        } else {
            path.moveTo(ax, ay + 7f); path.lineTo(ax - 6f, ay - 5f); path.lineTo(ax + 6f, ay - 5f)
        }
        path.close()
        canvas.drawPath(path, Paint().apply { color = lineColor; style = Paint.Style.FILL })


        val pctPaint = boldPaint(20f, lineColor)
        val pctText  = String.format("%.1f%%", Math.abs(growth.growthPercentage))
        canvas.drawText(pctText, margin + 38f, y + 34f, pctPaint)


        val sepX = margin + 50f + pctPaint.measureText(pctText)
        canvas.drawLine(sepX, y + 10f, sepX, y + boxH - 10f,
            Paint().apply { color = lineColor; strokeWidth = 1f; alpha = 60 })


        canvas.drawText("vs last month", sepX + 12f, y + 22f, paintCaption)
        canvas.drawText(numberFormat.format(Math.abs(growth.growthAmount)),
            sepX + 12f, y + 40f, boldPaint(12f, lineColor))

        return y + boxH
    }

    private fun drawDailySalesChart(
        canvas: Canvas, startY: Float, dailySales: List<DailySalesReport>
    ): Float {
        var y = startY
        drawSectionLabel(canvas, y, "DAILY SALES OVERVIEW")
        y += 28f

        if (dailySales.isEmpty()) return y

        val chartH = 170f
        val gap    = 3f
        val barW   = (contentWidth / dailySales.size) - gap
        val maxAmt = dailySales.maxOfOrNull { it.totalAmount } ?: 1.0


        canvas.drawRoundRect(margin, y, pageWidth - margin, y + chartH + 20f, 8f, 8f,
            Paint().apply { color = colorSurface; style = Paint.Style.FILL })

        val gridSteps = 4
        for (s in 0..gridSteps) {
            val gy = y + chartH - (chartH / gridSteps * s)
            canvas.drawLine(margin + 4f, gy, pageWidth - margin - 4f, gy,
                Paint().apply { color = colorBorder; strokeWidth = 0.8f })
            val v = (maxAmt / gridSteps) * s
            canvas.drawText(
                if (v >= 1000) String.format("%.0fK", v / 1000) else "0",
                margin + 6f, gy - 3f, paintCaption
            )
        }

        // Bars
        dailySales.forEachIndexed { idx, day ->
            val barH = ((day.totalAmount / maxAmt) * chartH).toFloat().coerceAtLeast(4f)
            val bx   = margin + idx * (barW + gap)
            val topY = y + chartH - barH

            canvas.drawRoundRect(bx, topY, bx + barW, y + chartH, 3f, 3f,
                Paint().apply { color = colorAccent; style = Paint.Style.FILL })


            canvas.drawRoundRect(bx, topY, bx + barW, topY + 6f, 3f, 3f,
                Paint().apply { color = colorWhite; style = Paint.Style.FILL; alpha = 40 })

            if (idx % 3 == 0) {
                canvas.drawText("${day.dayOfMonth}", bx + barW / 2 - 4f,
                    y + chartH + 14f, paintCaption)
            }
        }

        canvas.drawLine(margin, y + chartH, pageWidth - margin, y + chartH,
            Paint().apply { color = colorBorder; strokeWidth = 1f })

        return y + chartH + 24f
    }

    private fun drawTopProducts(
        canvas: Canvas, startY: Float, products: List<TopProductReport>
    ): Float {
        var y = startY


        val headerH = 28f
        canvas.drawRoundRect(margin, y, pageWidth - margin, y + headerH, 6f, 6f,
            Paint().apply { color = colorBrand; style = Paint.Style.FILL })

        canvas.drawRect(margin, y + headerH / 2, pageWidth - margin, y + headerH,
            Paint().apply { color = colorBrand; style = Paint.Style.FILL })

        val htP = boldPaint(10f, colorWhite)
        canvas.drawText("#",       margin + 14f,  y + 19f, htP)
        canvas.drawText("Product", margin + 50f,  y + 19f, htP)
        canvas.drawText("Orders",  margin + 295f, y + 19f, htP)
        canvas.drawText("Revenue", margin + 380f, y + 19f, htP)
        y += headerH

        products.take(10).forEachIndexed { idx, product ->
            val rowH = 30f

            canvas.drawRect(margin, y, pageWidth - margin, y + rowH,
                Paint().apply {
                    color = if (idx % 2 == 0) colorWhite else colorSurface
                    style = Paint.Style.FILL
                })


            val badgeColor = rankColors.getOrElse(idx) { colorAccent }
            canvas.drawCircle(margin + 22f, y + rowH / 2, 12f,
                Paint().apply { color = badgeColor; style = Paint.Style.FILL })
            canvas.drawText("${idx + 1}", margin + 22f, y + rowH / 2 + 4f,
                boldPaint(10f, colorWhite).apply { textAlign = Paint.Align.CENTER })

            canvas.drawText(product.menuName,  margin + 50f,  y + 20f, paintBody)
            canvas.drawText("${product.orderCount}", margin + 295f, y + 20f, paintBody)
            canvas.drawText(numberFormat.format(product.totalAmount),
                margin + 380f, y + 20f, boldPaint(11f, colorAccent))

            y += rowH
            canvas.drawLine(margin, y, pageWidth - margin, y,
                Paint().apply { color = colorBorder; strokeWidth = 0.6f })
        }


        canvas.drawLine(margin, y, pageWidth - margin, y,
            Paint().apply { color = colorBrand; strokeWidth = 1.5f; alpha = 60 })

        return y
    }


    private fun drawCategorySales(
        canvas: Canvas, startY: Float, categories: List<CategorySalesReport>
    ): Float {
        var y = startY
        if (categories.isEmpty()) return y

        val total    = categories.sumOf { it.totalAmount }
        val labelW   = 120f
        val amountW  = 100f
        val pctW     = 55f
        val barMaxW  = contentWidth - labelW - amountW - pctW - 20f

        val barColors = listOf(colorAccent, colorSuccess, colorWarning,
            Color.parseColor("#8E24AA"), Color.parseColor("#E53935"))

        val colHP = boldPaint(9f, colorSubtext)
        canvas.drawText("CATEGORY", margin, y + 10f, colHP)
        canvas.drawText("AMOUNT",   margin + labelW, y + 10f, colHP)
        y += 16f
        drawDivider(canvas, y)
        y += 8f

        categories.forEachIndexed { idx, cat ->
            val pct   = (cat.totalAmount / total * 100).toFloat()
            val fillW = (barMaxW * (pct / 100)).coerceAtLeast(4f)
            val color = barColors[idx % barColors.size]
            val rowH  = 36f

            // Dot + name
            canvas.drawCircle(margin + 6f, y + rowH / 2, 5f,
                Paint().apply { this.color = color; style = Paint.Style.FILL })
            canvas.drawText(cat.categoryName, margin + 18f, y + rowH / 2 + 4f, paintLabelBold)

            // Amount
            canvas.drawText(numberFormat.format(cat.totalAmount),
                margin + labelW, y + rowH / 2 + 4f, regularPaint(10f, colorSubtext))

            // Bar track
            val barY = y + rowH / 2 - 7f
            canvas.drawRoundRect(
                margin + labelW + amountW, barY,
                margin + labelW + amountW + barMaxW, barY + 14f,
                7f, 7f, Paint().apply { this.color = colorBorder; style = Paint.Style.FILL })

            // Bar fill
            canvas.drawRoundRect(
                margin + labelW + amountW, barY,
                margin + labelW + amountW + fillW, barY + 14f,
                7f, 7f, Paint().apply { this.color = color; style = Paint.Style.FILL })

            // Percentage
            canvas.drawText(String.format("%.1f%%", pct),
                margin + labelW + amountW + barMaxW + 10f, barY + 11f,
                boldPaint(10f, color))

            y += rowH
            drawDivider(canvas, y)
            y += 4f
        }

        return y
    }

    // ── Footer ─────────────────────────────────────────────────────
    private fun drawFooter(canvas: Canvas, year: Int, month: Int, pageNum: Int) {
        val footerY = pageHeight - 28f

        // Navy footer bar
        canvas.drawRect(0f, footerY - 2f, pageWidth.toFloat(), pageHeight.toFloat(),
            Paint().apply { color = colorBrand; style = Paint.Style.FILL })

        canvas.drawText(
            "ZYPOS © $year  ·  Monthly Report  ·  ${getMonthName(month)} $year",
            margin, footerY + 14f,
            regularPaint(8.5f, Color.parseColor("#90CAF9")))

        canvas.drawText("Page $pageNum", pageWidth - margin, footerY + 14f,
            boldPaint(8.5f, colorWhite).apply { textAlign = Paint.Align.RIGHT })
    }

    // ── Shared helpers ─────────────────────────────────────────────
    private fun drawDivider(canvas: Canvas, y: Float) {
        canvas.drawLine(margin, y, pageWidth - margin, y,
            Paint().apply { color = colorBorder; strokeWidth = 0.8f })
    }

    private fun drawSectionLabel(canvas: Canvas, y: Float, text: String) {
        val lp = boldPaint(9f, colorAccent)
        canvas.drawText(text, margin, y + 13f, lp)
        canvas.drawLine(margin, y + 18f, margin + lp.measureText(text), y + 18f,
            Paint().apply { color = colorAccent; strokeWidth = 1.5f; alpha = 80 })
    }

    private fun getMonthName(month: Int) = when (month) {
        1 -> "Januari"; 2 -> "Februari"; 3 -> "Maret"; 4 -> "April"
        5 -> "Mei"; 6 -> "Juni"; 7 -> "Juli"; 8 -> "Agustus"
        9 -> "September"; 10 -> "Oktober"; 11 -> "November"; 12 -> "Desember"
        else -> ""
    }
}