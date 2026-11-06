package io.github.jaffa944099.oneapp

internal data class PilotTask(
    val id: String,
    val title: Int,
    val summary: Int,
    val checklist: Int,
    val provider: Int,
    val url: String,
    val referenceLabel: Int? = null,
)

internal val pilotTasks = listOf(
    PilotTask(
        id = "citizen-appointment",
        title = R.string.appointment_title,
        summary = R.string.appointment_summary,
        checklist = R.string.appointment_checklist,
        provider = R.string.leonberg,
        url = "https://www.leonberg.de/B%C3%BCrger-Stadt/Politik-und-Rathaus/Termine-online-buchen/",
    ),
    PilotTask(
        id = "meter-reading",
        title = R.string.meter_title,
        summary = R.string.meter_summary,
        checklist = R.string.meter_checklist,
        provider = R.string.netze_bw,
        url = "https://www.netze-bw.de/zaehlerstandseingabe",
        referenceLabel = R.string.meter_number,
    ),
    PilotTask(
        id = "waste-calendar",
        title = R.string.waste_title,
        summary = R.string.waste_summary,
        checklist = R.string.waste_checklist,
        provider = R.string.awb,
        url = "https://www.awb-bb.de/abfuhrtermine",
    ),
    PilotTask(
        id = "swimming-courses",
        title = R.string.swimming_title,
        summary = R.string.swimming_summary,
        checklist = R.string.swimming_checklist,
        provider = R.string.dlrg,
        url = "https://leonberg.dlrg.de/mitmachen/anmeldungen-und-lehrgaenge/",
    ),
    PilotTask(
        id = "netze-bw-account",
        title = R.string.utility_title,
        summary = R.string.utility_summary,
        checklist = R.string.utility_checklist,
        provider = R.string.netze_bw,
        url = "https://meine.netze-bw.de/",
    ),
)
