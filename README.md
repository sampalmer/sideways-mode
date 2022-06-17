# Sideways Mode

<a href='https://play.google.com/store/apps/details?id=samuelpalmer.sensorautorotation&utm_campaign=github&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='240'/></a>

# Credits

* Design & development: Sam Palmer
* Russian translation: Евгений Черник

# Status

This project currently needs a new maintainer. Please reach out if you're interested.

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
| Service settings | `ServiceSettings` | `service.xml` | Persistent service state and cached copies of user preferences | Service might need to save data across process or device restarts, and Android [`SharedPreferences`](https://developer.android.com/reference/android/content/SharedPreferences) is not safe to share between processes |

# Release process

To release a new version of Sideways Mode, follow these steps:
1. Increment version numbers in `build.gradle`
1. If any persistent data has changed (`ApplicationSettings` or `ServiceSettings`), then ensure data migrations have been added to `Migrations`
1. Update store listing description (`/Store Listing/Full Description.html`) and screenshots (`/Store Listing/Graphics`)
1. Manually create release notes (`/Store Listing/Changes`)
1. Update privacy policy (`/Store Listing/privacy-policy.txt`)
1. Create a Git tag and set it to the `versionName` value
1. Test and promote through all environments
1. Manually copy store listing description, screenshots, and release notes into Google Play Console

# Other documentation

See the documentation in the Notes folder at the root of the repo.
