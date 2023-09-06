package org.harundemir.invoicepdfexample.models

data class InvoiceItem(
    val title: String,
    val price: Double,
    val quantity: Int,
    val total: Double
)
