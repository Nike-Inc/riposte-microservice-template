#!/usr/bin/env bash

# This script is for bootstrapping a new project from this template repository with a one line command. You don't even need to checkout the project.
#   Just run the following curl command:
#   curl -s 'https://raw.githubusercontent.com/Nike-Inc/riposte-microservice-template/master/bootstrap_template.sh' | bash /dev/stdin <newprojectname> <myorgname> </optional/target/dir> <-DoptionalSystemProps=stuff>
#   <newprojectname> - REQUIRED - The name of the new project.
#   <myorgname> - REQUIRED - The company/organization name you want to use for the final java class packaging.
#                            i.e. if you pass in 'foo' then all the java classes in the resulting project will be in the package com.foo
#   </optional/target/dir> - OPTIONAL - The path to the directory where the template project should be checked out and renamed.
#                                       Defaults to <newprojectname> if not specified.
#   <-DoptionalSystemProps=stuff> - OPTIONAL - A series of -D Java System Property flags that will get passed to the setup.groovy script
#                                              during the renaming process for renaming optional environment-specific properties.
#                                              See the README.md for more details and a list of recognized flags.

projectName="$1"
orgName="$2"
targetDir="$3"

if [[ "$3" =~ ^-D.* ]]; then
    targetDir=""
fi

function die {
	local code=$?
	echo $1
	exit $code
}

[ "$projectName" != "" ] || die "Missing projectName. Usage: bootstrap_template.sh <projectName> <orgName> [<path>] [<-DoptionalSystemPropertyFlags=foo>]"
[ "$orgName" != "" ] || die "Missing orgName. Usage: bootstrap_template.sh <projectName> <orgName> [<path>] [<-DoptionalSystemPropertyFlags=foo>]"

if [ "$targetDir" = "" ]; then
	targetDir="$projectName"
fi

ALL_ARGS_ARRAY=( "$@" )
SYSTEM_PROPS_ARRAY=()
for (( i=0; i<$#; i++ ));
do
    THE_ARG="${ALL_ARGS_ARRAY[i]}"
    if [[ "${THE_ARG}" =~ ^-D.* ]]; then
        SYSTEM_PROPS_ARRAY+=("${THE_ARG}")
    fi
done

echo -e "\nInitializing new Riposte project into target directory \"$targetDir\" with new project name \"$projectName\" and company/org name \"$orgName\""

echo -e "\nCreating target directory (if necessary): \"$targetDir\""
mkdir -p "$targetDir" || die "ERROR: Unable to create directory \"$targetDir\""
cd "$targetDir"
[ "$(ls -A . | grep -v "^\.git[/]\?$")" ] && die "ERROR: \"$targetDir\" not empty - the only thing it may contain is a .git folder (so you can initialize into an empty git repo)"

echo -e "\nFetching repository archive into target directory"
curl -L https://github.com/Nike-Inc/riposte-microservice-template/archive/master.tar.gz | tar xfz - --strip-components 1 || die "An error occurred while retrieving or unpacking the template project's repository archive"

echo -e "\nChanging project name to \"$projectName\" and company/org name \"$orgName\""
echo "(Extra system properties being sent to the gradle replacer task: ${SYSTEM_PROPS_ARRAY[@]})"
echo -e "\nNOTE: The first time this is run on your machine it may take a few minutes as the gradle wrapper and buildscript dependencies are downloaded. Subsequent executions should complete much quicker."

# Run the gradle replaceTemplate task to do the desired template renaming/setup
#   NOTE: We have to do this as a background task and then capture the PID, wait for the background task to finish, and
#   capture the resulting background task status because otherwise in some cases (e.g. doing the recommended
#   bootstrap-via-curl mechanism to start this script) gradle eats stdout/stderr and we can't echo anything after it runs.
(./gradlew replaceTemplate -DnewProjectName="$projectName" -DmyOrgName="$orgName" -DallowDashes=true "${SYSTEM_PROPS_ARRAY[@]}" >bootstrap_template.log 2>&1) &
GRADLE_REPLACER_PID=$!
wait ${GRADLE_REPLACER_PID}
GRADLE_REPLACER_STATUS=$?
if [ ${GRADLE_REPLACER_STATUS} != 0 ]; then
	echo "ERROR: Gradle replaceTemplate failed. See log output below for info."
	echo -e "\n--- START OF bootstrap_template.log ---"
	cat bootstrap_template.log
	echo -e "--- END OF bootstrap_template.log ---\n"
	exit ${GRADLE_REPLACER_STATUS}
fi

echo -e "\nProject name change successful."
echo -e "You can now change directory into \"$targetDir\" and run the following gradle wrapper command to build and run your new project: ./gradlew clean build run\n"
