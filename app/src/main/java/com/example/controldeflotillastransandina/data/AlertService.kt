package com.example.controldeflotillastransandina.data

import com.example.controldeflotillastransandina.data.entity.AlertEntity
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.data.entity.VehicleEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class AlertService(private val db: AppDatabase) {

    private val stateTypes = listOf(
        AlertType.MANTENIMIENTO_ATRASADO.name,
        AlertType.MANTENIMIENTO_PROXIMO.name,
        AlertType.DOCUMENTO_PROXIMO.name,
        AlertType.DOCUMENTO_VENCIDO.name
    )

    suspend fun refreshFleetAlerts() {
        val vehicles = db.vehicleDao().getAllOnce().filter { it.activo }
        val alertDao = db.alertDao()
        alertDao.deleteByTypes(stateTypes)

        val today = LocalDate.now().toEpochDay()
        val todayDate = LocalDate.ofEpochDay(today)
        val alerts = mutableListOf<AlertEntity>()

        for (v in vehicles) {
            val lastPrev = db.maintenanceDao().getLastPreventive(v.id)
            val currentKm = v.kilometrajeActual

            if (lastPrev != null) {
                val dueKm = lastPrev.kilometraje + v.frecuenciaKm
                val remainingKm = (dueKm - currentKm).toLong()
                val windowKm = (v.frecuenciaKm / 5).toLong()
                val dueDate = LocalDate.ofEpochDay(lastPrev.fecha).plusDays(v.frecuenciaDias.toLong())
                val remainingDays = ChronoUnit.DAYS.between(todayDate, dueDate)

                val overdueByKm = currentKm > dueKm
                val overdueByDays = remainingDays < 0

                when {
                    overdueByKm || overdueByDays -> {
                        val detail = when {
                            overdueByKm -> "supera el intervalo en ${fmtKm(remainingKm)} km"
                            else -> "la fecha prevista venció hace ${-remainingDays} días"
                        }
                        alerts += build(v, AlertType.MANTENIMIENTO_ATRASADO, 1,
                            "Requiere mantenimiento preventivo: $detail.")
                    }
                    remainingKm < windowKm || remainingDays <= 15 -> {
                        val detail = if (remainingKm < windowKm) {
                            "a ${fmtKm(remainingKm)} km del próximo servicio"
                        } else {
                            "dentro de ${remainingDays} días"
                        }
                        alerts += build(v, AlertType.MANTENIMIENTO_PROXIMO, 2,
                            "Mantenimiento preventivo próximo $detail.")
                    }
                }
            }

            documentAlerts(v, v.fechaMarchamo, "marchamo", todayDate, alerts)
            documentAlerts(v, v.fechaRevision, "revisión técnica", todayDate, alerts)
            documentAlerts(v, v.fechaSeguro, "seguro", todayDate, alerts)
        }

        alertDao.insertAll(alerts)
    }

    private fun documentAlerts(
        v: VehicleEntity,
        date: Long?,
        label: String,
        todayDate: LocalDate,
        alerts: MutableList<AlertEntity>
    ) {
        if (date == null) return
        val diff = ChronoUnit.DAYS.between(todayDate, LocalDate.ofEpochDay(date))
        when {
            diff < 0 -> alerts += build(v, AlertType.DOCUMENTO_VENCIDO, 1,
                "El $label venció hace ${-diff} días.")
            diff <= 30 -> alerts += build(v, AlertType.DOCUMENTO_PROXIMO, 2,
                "El $label vence en $diff días.")
        }
    }

    suspend fun notifyMaintenanceRegistered(v: VehicleEntity) {
        db.alertDao().insert(
            AlertEntity(
                vehicleId = v.id,
                userId = v.conductorId,
                tipo = AlertType.MANTENIMIENTO_REGISTRADO.name,
                mensaje = "${v.placa} · Se confirmó un mantenimiento registrado.",
                prioridad = 3
            )
        )
    }

    suspend fun notifyVehicleReassigned(vehicleId: Long, driverId: Long?, detail: String) {
        db.alertDao().insert(
            AlertEntity(
                vehicleId = vehicleId,
                userId = driverId,
                tipo = AlertType.VEHICULO_REASIGNADO.name,
                mensaje = detail,
                prioridad = 3
            )
        )
    }

    private fun build(v: VehicleEntity, type: AlertType, priority: Int, detail: String): AlertEntity =
        AlertEntity(
            vehicleId = v.id,
            userId = v.conductorId,
            tipo = type.name,
            mensaje = "${v.placa} · $detail",
            prioridad = priority
        )

    private fun fmtKm(km: Long): String =
        "%,d".format(km).replace(',', ' ').trim()
}