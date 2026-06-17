// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.google.services) apply false
}

// REDIRECT BUILD DIR: This moves the "build" folder out of OneDrive 
// to prevent "AccessDeniedException" and "Unable to delete directory" errors.
allprojects {
    val buildDirName = if (project == rootProject) "root" else project.name
    // Redirect build directory to user home (outside OneDrive) to avoid AccessDeniedException and OneDrive locks
    val userHome = System.getProperty("user.home")
    layout.buildDirectory.set(file("$userHome/android_builds/altiguide-mobile/$buildDirName"))
}