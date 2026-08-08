

https://github.com/user-attachments/assets/e561c694-f951-470b-af5f-9c7ea0f50894



https://github.com/user-attachments/assets/76a14873-d320-4930-a7a9-de3fa42b3bf6

# Expressive FAB for Jetpack Compose

Expressive FAB is a Jetpack Compose library that provides enhanced Floating Action Button components and menus inspired by the Material 3 Expressive design.

The library extends the standard behavior by allowing developers to choose whether menu items expand above or below the Floating Action Button, making it easier to adapt the component to different layouts and user experiences.

«Disclaimer

This is an independent open-source project inspired by the Material 3 Expressive Floating Action Button. It is not affiliated with, endorsed by, or maintained by Google. Material Design and Material 3 are trademarks of Google. This library is intended to complement the Jetpack Compose ecosystem by providing additional customization and functionality.»

## Key Features

- **Expressive FAB Menu (`ExpressiveFabMenu`)**: Easily build expandable action menus anchored to your Floating Action Button.
- **Flexible Expansion Directions**: Menu items can expand either **above** or **below** the main trigger button to seamlessly fit your UI layout requirements.
- **Toggleable FAB (`ToggleFloatingActionButton`)**: Smoothly animate state changes with interpolated container size, corner radius, color transitions, and custom icon animations.
- **Staggered Animations & Scroll Support**: Polished motion scheme integration with support for scrollable menus when items exceed available screen height.
- **Keyboard & Accessibility Support**: Built-in keyboard navigation support (Tab / Down arrow) and focus management out-of-the-box.

  ### Step 1. Add the JitPack repository

Add the JitPack repository to your `settings.gradle` file:

```gradle
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' }
	}
}
```

### Step 1. Add the dependency

```
dependencies {
	implementation 'com.github.rJoel01:expressive-fab:1.0.0'
}
```

[![](https://jitpack.io/v/rJoel01/expressive-fab.svg)](https://jitpack.io/#rJoel01/expressive-fab)


### In code usage:

```
var expanded by remember { mutableStateOf(false) }

                    ExpressiveFabMenu(
                        expanded = expanded,
                        button = {
                            // YOUR BUTTON COMPOSABLE
                        },
                        expandDirection = FabMenuExpandDirection.BELOW
                    ) {
                        this.ExpressiveFabMenuItem(
                            onClick = {},
                            text = {
                                //YOUR TEXT COMPOSABLE
                            },
                            icon = {
                                //YOUR ICON COMPOSABLE
                            }
                        )
                    }

```
