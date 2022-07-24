# Sideways Mode

<a href='https://play.google.com/store/apps/details?id=samuelpalmer.sensorautorotation&utm_campaign=github&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='240'/></a>

# Credits

* Design & development: Sam Palmer
* Russian translation: Евгений Черник

# Status

This project needs a new maintainer. Please reach out if you're interested.

# Project structure

This is a standard Android Studio project. The project contains a number of modules. The `app` module is Sideways Mode itself. The other modules are separate testing apps for local use to assist development and troubleshooting for specific situations.  

# Architecture

Sideways Mode runs two separate processes:
| Process | Purpose | Usage |
| ------- | ------- | ----- |
| UI | UI code. Can be killed by OS when UI is not visible. | For all UI code. Allowed to use plenty of RAM for images and icons etc as needed. |
| Service | Background code. Designed to minimise risk of being killed by OS. | Must use low amount of RAM so OS doesn't kill process. Must not run UI code since that uses a lot of RAM that can't all be freed. |

Having two processes adds some complexity but provides the following benefits:
* Background service won't die if UI crashes
* Less chances of background service being killed by OS during memory pressure, since the OS kills memory hogs first
* Leaves more RAM free for other apps since the OS can readily kill the UI process

Most multi-process complexity is already taken care of in the codebase. Main things to keep in mind for this are:
* The UI process communicates requests to the service process through `ApplicationCommand`, which are received by `ApplicationCommandReceiver`
* The service process communicates requests to the UI process through `ApplicationStatusService`, which are received by `ApplicationStatusClient`
* Application settings (`ApplicationSettings`) are owned by the UI process and replicated to the service process by `AppSettingsSynchroniser`
* Service settings (`ServiceSettings`) are owned by the service process

The reason the preferences are replicated across both processes is Android's [`SharedPreferences`](https://developer.android.com/reference/android/content/SharedPreferences) implementation doesn't support use across multiple processes, so each settings file is specific to a particular process.

In general, code that runs in the UI process lives in the `samuelpalmer.sensorautorotation.processes.ui` package, and code that runs in the service process lives in the `samuelpalmer.sensorautorotation.processes.service` package.

# Persistent data

Sideways Mode stores persistent data in a few different places:
|  Name  | Class | Location | Data | Reason |
| ------ | ----- | -------- | ------- | ------ |
| Application settings | `ApplicationSettings` | `samuelpalmer.sensorautorotation_preferences.xml` | User preferences | Standard practice |
| Service settings | `ServiceSettings` | `service.xml` | Persistent service state and cached copies of user preferences | Service might need to persist state across process or device restarts, and Android [`SharedPreferences`](https://developer.android.com/reference/android/content/SharedPreferences) is not safe to share between processes |

# Updating Fastlane

Sideways Mode uses [Fastlane](https://fastlane.tools/) in its CI/CD pipeline. Fastlane occasionally needs to be updated for compatibility with changes to Google Play Console. To update Fastlane, you need a Ruby environment. One way to upgrade Fastlane is to open Docker Desktop and then run the upgrade from within a container:

```shell
docker run --rm -v C:\your\path\to\sideways-mode:/home/project ruby bash -c "cd /home/project && bundle update fastlane"
```

# Release process

To release a new version of Sideways Mode, follow these steps:
1. Remove support for any old unused version of Android
2. Increment version numbers in `build.gradle`
3. If any persistent data has changed (`ApplicationSettings` or `ServiceSettings`), then ensure data migrations have been added to `Migrations`
4. Run automated instrumentation tests across all supported Android versions
5. Update store listing description (`/Store Listing/Full Description.html`) and screenshots (`/Store Listing/Graphics`)
6. Manually create release notes (`/Store Listing/Changes`)
7. Update privacy policy (`/Store Listing/privacy-policy.txt`)
8. Create a Git tag and set it to the `versionName` value
9. Wait for CI/CD pipeline to publish to Play Store internal track
10. Manually install from Play Store as internal tester and perform manual smoke test
11. Wait for Play Console pre-launch report to generate and ensure no issues are detected
12. Manually copy store listing description, screenshots, and release notes into Google Play Console
13. Promote to Production using CI/CD pipeline
14. Monitor Production errors

# Other documentation

Other developer documentation and user documentation can be found in `/Notes`.
