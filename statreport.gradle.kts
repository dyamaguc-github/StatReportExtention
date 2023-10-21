version = "0.0.1"
description = "Stat. Report"

zapAddOn {
    addOnName.set("Stat. Report")

    manifest {
        author.set("dymgc")
    }
}

crowdin {
    configuration {
        val resourcesPath = "org/zaproxy/addon/${zapAddOn.addOnId.get()}/resources/"
        tokens.put("%messagesPath%", resourcesPath)
        tokens.put("%helpPath%", resourcesPath)
    }
}
