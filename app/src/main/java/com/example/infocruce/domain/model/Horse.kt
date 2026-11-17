package com.example.infocruce.domain.model

/**
 * Enumeración del sexo del caballo.
 */
enum class HorseSex {
    MACHO,
    HEMBRA
}

/**
 * Modelo de dominio que representa a un caballo (ejemplar equino).
 *
 * @param id Identificador único del caballo
 * @param ownerId ID del propietario (criador)
 * @param name Nombre del caballo
 * @param registrationNumber Número de registro genealógico
 * @param sex Sexo del caballo
 * @param sireId ID del padre (semental) - puede ser null si no se conoce
 * @param damId ID de la madre (yegua nodriza) - puede ser null si no se conoce
 * @param height Altura en centímetros
 * @param temperament Temperamento del caballo (ej: "Tranquilo", "Enérgico")
 * @param isDnaVerified Indica si el ADN ha sido verificado por un veterinario
 * @param isPublicCatalog Indica si el caballo aparece en el catálogo público
 * @param photoUrl URL de la foto del caballo
 */
data class Horse(
    val id: Long,
    val ownerId: Long,
    val name: String,
    val registrationNumber: String,
    val sex: HorseSex,
    val sireId: Long? = null,
    val damId: Long? = null,
    val height: Int,
    val temperament: String,
    val isDnaVerified: Boolean = false,
    val isPublicCatalog: Boolean = false,
    val photoUrl: String? = null
)
