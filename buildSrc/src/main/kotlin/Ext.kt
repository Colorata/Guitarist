import gradle.kotlin.dsl.accessors._8837c7559f19dda1cc75c062cb6746f9.android
import org.gradle.api.Project

fun Project.androidNamespaceExtension(extension: String) {
    android {
        this.namespace = "it.vfsfitvnm.compose.$extension"
    }
}