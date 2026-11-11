# Hide Kotlin metadata
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }
# Obfuscate everything else aggressively
-repackageclasses ''
-allowaccessmodification
