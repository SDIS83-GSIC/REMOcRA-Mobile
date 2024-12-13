package fr.sdis83.remocra.mobile.utils

enum class TypeNatureEnum(private val code: String) {
    PI("PI"),
    BI("BI"),
    PA("PA"),
    CI("CI"),
    ;

    fun getCode() =
        this.code
}
