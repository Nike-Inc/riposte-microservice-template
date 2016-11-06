import java.util.concurrent.CountDownLatch
import java.util.function.Function

def printDots(message, Closure closure) {
    print(message)
    def latch = new CountDownLatch(1)
    def dotsThread = Thread.start {
        while (latch.getCount() > 0) {
            print(".")
            sleep(100)
        }
    }

    closure.run()

    latch.countDown()
    println()
}

String NEW_PROJECT_NAME_SYSPROP_KEY() { return "newProjectName" }
String MYORG_NAME_SYSPROP_KEY() { return "myOrgName" }
String ALLOW_DASHES_SYSPROP_KEY() { return "allowDashes" }

def printUsageAndQuit(String exceptionMsg) {
    println("USAGE: ./gradlew replaceTemplate -D${NEW_PROJECT_NAME_SYSPROP_KEY()}=foobar -D${MYORG_NAME_SYSPROP_KEY()}=baz")
    println("\tARG ${NEW_PROJECT_NAME_SYSPROP_KEY()}: The name for the new project.")
    println("\tARG ${MYORG_NAME_SYSPROP_KEY()}: The replacement name for 'myorg', i.e. if this was 'foo' then com.myorg becomes com.foo")
    println("\tNOTE: You might not want ${NEW_PROJECT_NAME_SYSPROP_KEY()} to contain a dash '-' character")
    println("\t      depending on how the project will be deployed. If you want your project name to have dashes")
    println("\t      then you must pass in an additional System property: -D${ALLOW_DASHES_SYSPROP_KEY()}=true")
    println("ERROR: $exceptionMsg")

    throw new RuntimeException(exceptionMsg)
}

// Get the name of the new project that will be used to replace the template name, and the replacement for 'myorg'.
String replacementName = System.getProperty(NEW_PROJECT_NAME_SYSPROP_KEY())
String myOrgReplacementName = System.getProperty(MYORG_NAME_SYSPROP_KEY())

if (replacementName == null) {
    printUsageAndQuit("Missing required System property: -D${NEW_PROJECT_NAME_SYSPROP_KEY()}=[name]")
}

if (myOrgReplacementName == null) {
    printUsageAndQuit("Missing required System property: -D${MYORG_NAME_SYSPROP_KEY()}=[name]")
}

replacementName = replacementName.toLowerCase().trim()
myOrgReplacementName = myOrgReplacementName.toLowerCase().trim()

// Check for dashes in the name, and bail if there are unless the user asked for an override.
if (replacementName =~ "-") {
    String allowDashes = System.getProperty(ALLOW_DASHES_SYSPROP_KEY())
    if (!"true".equals(allowDashes)) {
        printUsageAndQuit("Some deployment systems do not work with dashes in the application name. To override this restriction " +
                          "you must pass an additional -D${ALLOW_DASHES_SYSPROP_KEY()}=true flag.")
    }
}

// Create a no-dashes version for package name.
String replacementPackageName = replacementName.replace("-", "")

// Extract the optional field replacement values
List<String> getOptReplacementPairForKey(String key) {
    return [key, System.getProperty(key, key)]
}

List<List<String>> optionalReplacementPairs = [getOptReplacementPairForKey("fixme_eureka_domain_test"),
                                               getOptReplacementPairForKey("fixme_eureka_domain_prod"),
                                               getOptReplacementPairForKey("fixme_project_remotetest_url_test"),
                                               getOptReplacementPairForKey("fixme_project_remotetest_url_prod")
]

// ================ DO IT! =================
println("Setting up project named: $replacementName")
String rootLocation = System.getProperty("rootProjectLocation")
if (rootLocation == null) {
    throw new RuntimeException("The rootProjectLocation System Property must be specified. " +
                               "There must be something wrong with the gradle task that calls this setup script.")
}
File rootLocationFile = new File(rootLocation)

println("Replacing project name and org name starting at location: ${rootLocation}")
if (!rootLocationFile.exists()) {
     throw new RuntimeException("The root project location ${rootLocation} must exist. " +
                                "There must be something wrong with the gradle task that calls this setup script.")
}

def safeRenameFileOrDirectory(File it, String stringToReplace, String replacementValue) {
    String oldFilename = it.getAbsolutePath()
    String base = oldFilename.substring(0, oldFilename.lastIndexOf('/') + 1)
    String newFilename = base + it.getName().replaceAll(stringToReplace, replacementValue)
    String fileOrDir = (it.isDirectory()) ? "directory" : "file"
    println("\nRenaming $fileOrDir $oldFilename to $newFilename")
    try {
        boolean success = it.renameTo(new File(newFilename))
        if (!success) {
            throw new Exception("Unable to rename $fileOrDir $oldFilename to $newFilename")
        }
    }
    catch(Throwable ex) {
        ex.printStackTrace()
        System.exit(1)
    }
}

def replaceText(File it, String name, String packageName, String orgName, List<List<String>> optReplacementPairs) {
    def text = it.text
    text = text.replaceAll("riposte-microservice-template", "$name")
    text = text.replaceAll("ripostemicroservicetemplate", "$packageName")
    text = text.replaceAll("myorg", "$orgName")
    for (List<String> pair : optReplacementPairs) {
        text = text.replaceAll((String)pair[0], (String)pair[1])
    }
    println("\nReplacing content of file: " + it.getAbsolutePath())
    it.write(text)
}

def numForwardSlashes(String line) {
    return line.length() - line.replace("/", "").length()
}

printDots("Updating files with project name: $replacementName, and org name: $myOrgReplacementName\n", {
    Comparator<File> fileDepthComparator = Comparator.comparing(new Function<File, Integer>() {
        @Override
        Integer apply(File file) {
            return numForwardSlashes(file.getAbsolutePath())
        }
    }).reversed()
    List<File> normalNameDirectoryRenameList = new ArrayList<>()
    List<File> projectPackageNameDirectoryRenameList = new ArrayList<>()
    List<File> orgPackageNameDirectoryRenameList = new ArrayList<>()
    List<File> fileRenameList = new ArrayList<>()

    // Find and collect the project name package directories that need to be renamed
    rootLocationFile.eachFileRecurse {
        if (it.isDirectory() && it.name =~ /.*ripostemicroservicetemplate*./)
            projectPackageNameDirectoryRenameList.add(it)
    }

    // Sort the project package directory rename list based on file depth so we rename the deep ones first
    projectPackageNameDirectoryRenameList.sort(fileDepthComparator)

    // Rename the project package directories
    projectPackageNameDirectoryRenameList.each {
        safeRenameFileOrDirectory(it, "ripostemicroservicetemplate", replacementPackageName)
    }

    // Find and collect the org name package directories that need to be renamed
    rootLocationFile.eachFileRecurse {
        if (it.isDirectory() && it.name =~ /.*myorg*./)
            orgPackageNameDirectoryRenameList.add(it)
    }

    // Sort the org package directory rename list based on file depth so we rename the deep ones first
    orgPackageNameDirectoryRenameList.sort(fileDepthComparator)

    // Rename the org package directories
    orgPackageNameDirectoryRenameList.each {
        safeRenameFileOrDirectory(it, "myorg", myOrgReplacementName)
    }

    // Find and collect the non-package directories that need to be renamed
    rootLocationFile.eachFileRecurse {
        if (it.isDirectory() && it.name =~ /.*riposte-microservice-template*./)
            normalNameDirectoryRenameList.add(it)
        }

    // Sort the non-package directory rename list based on file depth so we rename the deep ones first
    normalNameDirectoryRenameList.sort(fileDepthComparator)

    // Rename the non-package directories
    normalNameDirectoryRenameList.each {
        safeRenameFileOrDirectory(it, "riposte-microservice-template", replacementName)
    }

    // Replace file content and collect the filenames for the non-directory files that need to be renamed
    rootLocationFile.eachFileRecurse {
        if (it.name =~ /.*riposte-microservice-template*./) {
            // Filename matches template name, so remember for later
            fileRenameList.add(it)

            if (!it.isDirectory()) {
                // Not a directory, so replace text
                replaceText(it, replacementName, replacementPackageName, myOrgReplacementName, optionalReplacementPairs)
            }
        } else if (!it.isDirectory() && it.name =~ /.*\.properties|.*\.java|.*\.xml|.*\.pp|.*\.sh|.*\.json|.*\.txt|.*\.gradle|.*\.md|.*\.yaml/) {
            // Filename does not match template name, but it is a non-directory file that might have template stuff in it
            // so replace the contents.
            replaceText(it, replacementName, replacementPackageName, myOrgReplacementName, optionalReplacementPairs)
        }
    }

    // Finally, root through the non-directory files we found that need to be renamed and rename them.
    for (File fileToRename : fileRenameList) {
        safeRenameFileOrDirectory(fileToRename, "riposte-microservice-template", replacementName)
    }
})

println("\nDeleting setup script groovy folder: " + (new File("${rootLocation}/${replacementName}-core-code/src/main/groovy").deleteDir()))
println("\nDeleting bootstrap_template.sh: " + (new File("${rootLocation}/bootstrap_template.sh").delete()))
println("\nDeleting bootstrap_template.log (if it exists): " + (new File("${rootLocation}/bootstrap_template.log").delete()))
