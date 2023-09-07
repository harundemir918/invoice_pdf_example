package org.harundemir.invoicepdfexample

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.harundemir.invoicepdfexample.databinding.ActivityMainBinding
import org.harundemir.invoicepdfexample.models.InvoiceItem
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var permissionCode = 101
    private val items = mutableListOf(
        InvoiceItem(
            title = "Apple",
            price = 12.0,
            quantity = 1,
            total = 12.0
        ),
        InvoiceItem(
            title = "Banana",
            price = 24.0,
            quantity = 2,
            total = 48.0
        ),
        InvoiceItem(
            title = "Strawberry",
            price = 10.0,
            quantity = 1,
            total = 10.0
        ),
        InvoiceItem(
            title = "Kiwi",
            price = 8.0,
            quantity = 3,
            total = 24.0
        )
    )
    private var subtotal = 0.0
    private var tax = 0.0
    private val taxRate = 0.18
    private var total = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createInvoiceButton.setOnClickListener {
            if (checkStoragePermission()) {
                createPDF()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun createPDF() {
        val locale = Locale.getDefault()
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val rectBottom = 340f
        val verticalSpacing = 30f
        var lastVerticalPosition = 0f
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val defaultAlignedLeft = Paint().apply {
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
        }

        val defaultAlignedRight = Paint().apply {
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
        }

        val boldAlignedLeft = Paint().apply {
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
        }

        val paint = Paint()

        val companyNamePaint = Paint(boldAlignedLeft)
        val invoicePaint = Paint()

        val companyAddressLine1Paint = Paint(defaultAlignedLeft)
        val companyAddressLine2Paint = Paint(defaultAlignedLeft)
        val companyPhonePaint = Paint(defaultAlignedLeft)
        val companyFaxPaint = Paint(defaultAlignedLeft)
        val invoiceNoPaint = Paint(defaultAlignedRight)
        val invoiceDatePaint = Paint(defaultAlignedRight)

        val customerInfoTitlePaint = Paint(boldAlignedLeft)
        val customerNamePaint = Paint(defaultAlignedLeft)
        val customerAddressLine1Paint = Paint(defaultAlignedLeft)
        val customerAddressLine2Paint = Paint(defaultAlignedLeft)
        val customerPhonePaint = Paint(defaultAlignedLeft)

        val tablePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        invoicePaint.apply {
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            color = Color.parseColor("#595959")
        }

        canvas.apply {
            drawText(
                getString(R.string.app_name),
                40f, 60f, companyNamePaint
            )
            drawText(
                getString(R.string.invoice).uppercase(),
                (pageWidth - 40).toFloat(),
                60f + (companyNamePaint.textSize / 4),
                invoicePaint
            )

            drawText(
                "1234 Elm Street, East", 40f, 120f, companyAddressLine1Paint
            )
            drawText(
                "Anytown, USA 12345", 40f, 140f, companyAddressLine2Paint
            )
            drawText(
                getString(R.string.phone_with_value, "555-123-4567"), 40f, 160f, companyPhonePaint
            )
            drawText(
                getString(R.string.fax_with_value, "555-123-4568"), 40f, 180f, companyFaxPaint
            )
            drawText(
                getString(R.string.invoice_no_with_value, "ABCD1234"),
                (pageWidth - 40).toFloat(),
                160f,
                invoiceNoPaint
            )
            drawText(
                getString(R.string.invoice_date_with_value, getLocalizedDate()),
                (pageWidth - 40).toFloat(),
                180f,
                invoiceDatePaint
            )

            drawText(
                getString(R.string.billed_to).uppercase(locale),
                40f, 210f, customerInfoTitlePaint
            )
            drawText(
                "John Doe", 40f, 230f, customerNamePaint
            )
            drawText(
                "12346 Mel Underpass, West", 40f, 250f, customerAddressLine1Paint
            )
            drawText(
                "Anytown, USA 12347", 40f, 270f, customerAddressLine2Paint
            )
            drawText(
                "555-124-4548", 40f, 290f, customerPhonePaint
            )

            drawRect(40f, 310f, (pageWidth - 40).toFloat(), rectBottom, tablePaint)

            paint.apply {
                textAlign = Paint.Align.LEFT
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                style = Paint.Style.FILL
            }
            drawText(getString(R.string.qty).uppercase(locale), 50f, 329f, paint)
            drawText(getString(R.string.item).uppercase(locale), 110f, 329f, paint)
            drawText(getString(R.string.price).uppercase(locale), 390f, 329f, paint)
            drawText(
                getString(R.string.amount).uppercase(locale),
                (pageWidth - 115).toFloat(),
                329f,
                paint
            )

            drawLine(100f, 310f, 100f, 340f, paint)
            drawLine(380f, 310f, 380f, 340f, paint)
            drawLine(470f, 310f, 470f, 340f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            for (i in 0 until items.size) {
                lastVerticalPosition = rectBottom + 30f + i * verticalSpacing
                drawText(
                    items[i].quantity.toString(),
                    50f,
                    lastVerticalPosition,
                    paint
                )
                drawText(
                    items[i].title,
                    110f,
                    lastVerticalPosition,
                    paint
                )
                drawText(
                    getString(R.string.value_with_currency, items[i].price.toString()),
                    390f,
                    lastVerticalPosition,
                    paint
                )
                drawText(
                    getString(R.string.value_with_currency, items[i].total.toString()),
                    (pageWidth - 115).toFloat(),
                    lastVerticalPosition,
                    paint
                )
            }

            calculateTotal()

            drawLine(
                40f,
                lastVerticalPosition + 20f,
                (pageWidth - 40).toFloat(),
                lastVerticalPosition + 20f,
                paint
            )

            drawText(
                getString(R.string.subtotal).uppercase(locale),
                390f,
                lastVerticalPosition + 50f,
                paint
            )
            drawText(
                getString(R.string.tax).uppercase(locale),
                390f,
                lastVerticalPosition + 80f,
                paint
            )
            drawText(
                getString(R.string.total).uppercase(locale),
                390f,
                lastVerticalPosition + 110f,
                paint
            )

            drawText(
                getString(R.string.value_with_currency, subtotal.toString()),
                (pageWidth - 115).toFloat(),
                lastVerticalPosition + 50f,
                paint
            )
            drawText(
                getString(R.string.value_with_currency, tax.toString()),
                (pageWidth - 115).toFloat(),
                lastVerticalPosition + 80f,
                paint
            )
            drawText(
                getString(R.string.value_with_currency, total.toString()),
                (pageWidth - 115).toFloat(),
                lastVerticalPosition + 110f,
                paint
            )

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            drawText(
                getString(R.string.tax_rate_description, (taxRate * 100).toString()),
                40f,
                lastVerticalPosition + 150f,
                paint
            )
        }

        pdfDocument.finishPage(page)

        val directory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        }
        val file = File(
            directory,
            "invoice.pdf"
        )
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, getString(R.string.pdf_file_created), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.failed_to_create_pdf_file), Toast.LENGTH_SHORT)
                .show()
        }
        pdfDocument.close()
    }

    private fun calculateTotal(): Double {
        for (i in items) {
            subtotal += i.total
        }
        tax = roundToTwoDecimalPlaces(subtotal * taxRate)
        total = subtotal + tax
        return total
    }

    private fun roundToTwoDecimalPlaces(value: Double): Double {
        val bigDecimalValue = BigDecimal(value)
        val roundedValue = bigDecimalValue.setScale(2, RoundingMode.HALF_UP)
        return roundedValue.toDouble()
    }

    fun getLocalizedDate(): String {
        val calendar = Calendar.getInstance()
        val locale = Locale.getDefault() // Get the user's device locale
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", locale) // Define your desired date format

        return dateFormat.format(calendar.time)
    }

    private fun checkStoragePermission(): Boolean {
        val writeStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            WRITE_EXTERNAL_STORAGE
        )
        val readStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            READ_EXTERNAL_STORAGE
        )
        return writeStoragePermission == PackageManager.PERMISSION_GRANTED && readStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
            permissionCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionCode) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT)
                        .show()

                } else {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
        }
    }
}