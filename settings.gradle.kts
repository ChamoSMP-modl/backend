rootProject.name = "backend"


val useLocalProto: Boolean = providers.gradleProperty("modl.useLocalProto").map { it.toBoolean() }.getOrElse(false)
val localProtoSettings = file("../proto/settings.gradle.kts")

if (useLocalProto && localProtoSettings.isFile()) {
    includeBuild("../proto")
}

include("mongo-fields-annotations")
include("mongo-fields-processor")