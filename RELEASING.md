# Release Process

## Releasing a new version

splunk-otel-android is released via a private Splunk gitlab installation.

This is the process to use to do a release:

1) Make sure that all the required changes are merged. This includes updating the upstream OTel
   libraries' versions, and making sure that the project version in the `gradle.properties` file is
   correctly set to the next planned release version.

2) Run the `scripts/tag-release.sh` script with latest release version number,
   eg: ./tag-release.sh 1.2.3 to create and push a signed release tag. Note that it assumes that the
   remote is named `origin`, if you named yours differently you might have to push the tag manually.
   (Following step 4 should manually push the tag.)

3) Wait for gitlab android-releaser to run the release job. If all goes well, it will automatically
   close and release the "staging" repository...which means the build has been published to sonatype
   and will appear in maven with in a day or two at most (typically a few hours).

4) Once this PR is merged, create a release in Github that points at the newly created version,
   and make sure to provide release notes that at least mirror the contents of the CHANGELOG.md

5) Create a PR to update the version in the `gradle.properties` to the next development
   version. This PR can and probably should also include updating any documentation (CHANGELOG.md,
   README.md, etc) that mentions the previous version. Make sure the badge in the top README.md
   reflects the accurate upstream otel version.

6) Go to Slack and notify relevant channels about the release.

## Releasing a patch version for a previously released version

1) If not already available, create a branch from the tag of the previously released version for
   which you're creating the patch. For example, create a v1.8.x branch. All changes for the patch
   release should be made on this branch.

2) Follow steps 1 to 4 outlined in the "Releasing a New Version" section.

3) Update the `main` branch with similar fixes. This PR can and probably should also include updating
   any documentation (CHANGELOG.md, README.md, etc) to reflect the version you just released. Make
   sure the badge in the top README.md reflects the accurate upstream otel version.

4) Go to Slack and notify relevant channels about the release.
