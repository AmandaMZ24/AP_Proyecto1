package com.example.controldeflotillastransandina.data

enum class UserRole(val displayName: String) {
    ADMINISTRADOR("Administrador"),
    ENCARGADO_FLOTA("Encargado de flota"),
    CONDUCTOR("Conductor"),
    MECANICO("Mecánico")
}

enum class VehicleType(val displayName: String) {
    LIVIANO("Liviano"),
    PESADO("Pesado"),
    ESPECIAL("Especial")
}

enum class MaintenanceType(val displayName: String) {
    PREVENTIVO("Preventivo"),
    CORRECTIVO("Correctivo")
}

enum class MaintenanceCategory(val displayName: String) {
    ACEITE("Cambio de aceite"),
    FRENOS("Frenos"),
    LLANTAS("Llantas"),
    REVISION("Revisión general"),
    MOTOR("Motor"),
    SUSPENSION("Suspensión"),
    ELECTRICO("Sistema eléctrico"),
    OTRO("Otro")
}

enum class AlertType(val displayName: String) {
    MANTENIMIENTO_ATRASADO("Mantenimiento atrasado"),
    MANTENIMIENTO_PROXIMO("Mantenimiento próximo"),
    DOCUMENTO_PROXIMO("Documento próximo a vencer"),
    DOCUMENTO_VENCIDO("Documento vencido"),
    MANTENIMIENTO_REGISTRADO("Mantenimiento confirmado"),
    VEHICULO_REASIGNADO("Vehículo reasignado")
}