package com.example.minitiendita2

import android.net.Uri

data class Product(
    val id: Int,               // Identificador único del producto
    val nombre: String,        // Nombre del producto
    val precio: Double,        // Precio del producto
    var cantidad: Int,         // Cantidad disponible del producto (puede cambiar)
    val estado: String,        // Estado del producto ("Nuevo" / "Usado")
    val categoria: String,     // Categoría del producto, tomada del ListView en el diálogo
    val disponible: Boolean,   // Si el producto está disponible para la venta
    val imageUri: Uri? = null  // URI de la imagen del producto (opcional)
)
