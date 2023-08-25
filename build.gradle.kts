// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application").version("8.0.2") apply false
    id("com.android.library").version("8.0.2") apply false
    id("org.jetbrains.kotlin.android").version("1.8.10") apply false
    id("com.google.devtools.ksp").version("1.8.10-1.0.9") apply false
    id("com.diffplug.spotless").version("6.20.0")
}

spotless {
    kotlinGradle {
        ktlint("0.50.0")
    }
    format("misc") {
        target("**/*.gradle", "**/*.md", "**/.gitignore")
        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

project(":app") {
    apply(plugin = "com.diffplug.spotless")
    spotless {
        kotlin {
            target("src/*/java/**/*.kt")
            ktlint("0.50.0")
        }
        format("xml") {
            target("src/*/res/**/*.xml")
            indentWithSpaces()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
