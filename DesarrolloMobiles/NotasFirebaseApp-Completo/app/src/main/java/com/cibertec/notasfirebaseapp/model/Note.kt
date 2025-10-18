package com.cibertec.notasfirebaseapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Note(
    var id: String? = null,
    var title: String = "",
    var description: String = "",
    @ServerTimestamp var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null
)
