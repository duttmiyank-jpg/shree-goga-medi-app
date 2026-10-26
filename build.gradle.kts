plugins {
    id("com.android.application") version "9.3.3" apply false
    id("com.android.library") version "9.3.3" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.1.0" apply false
    id("org.jetbrains.compose") version "1.7.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
    alias(libs.plugins.google.services) apply false
}
