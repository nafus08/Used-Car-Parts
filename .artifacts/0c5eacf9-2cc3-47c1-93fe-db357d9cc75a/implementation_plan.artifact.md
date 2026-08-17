# Project Milestone 2 Implementation Plan

Implement Registration, Login, and 3 additional features (Splash/Language, Home Feed, Post Listing) using XML Views and Room database.

## User Review Required

> [!IMPORTANT]
> Since we are using Room (local database), the data will only persist on the device. I will implement a multi-role system where users can register as either a Shopper or a Trader.

## Proposed Changes

### Dependencies & Setup

#### [MODIFY] [build.gradle.kts](file:///E:/UsedCarParts/app/build.gradle.kts)
Add dependencies for Room, Navigation, Lifecycle, and ViewBinding.

### Database Layer (Room)

#### [NEW] [AppDatabase.kt](file:///E:/UsedCarParts/app/src/main/java/com/example/usedcarparts/data/AppDatabase.kt)
#### [NEW] [Entities.kt](file:///E:/UsedCarParts/app/src/main/java/com/example/usedcarparts/data/Entities.kt)
Define `User`, `Trader`, and `Listing` entities.
#### [NEW] [Daos.kt](file:///E:/UsedCarParts/app/src/main/java/com/example/usedcarparts/data/Daos.kt)
Define `UserDao` and `ListingDao`.

### UI - Authentication

#### [NEW] [LoginActivity.kt](file:///E:/UsedCarParts/app/src/main/java/com/example/usedcarparts/ui/auth/LoginActivity.kt) / [activity_login.xml](file:///E:/UsedCarParts/app/src/main/res/layout/activity_login.xml)
#### [NEW] [RegisterActivity.kt](file:///E:/UsedCarParts/app/src/main/java/com/example/usedcarparts/ui/auth/RegisterActivity.kt) / [activity_register.xml](file:///E:/UsedCarParts/app/src/main/res/layout/activity_register.xml)
Handle role selection and data persistence in Room.

### UI - Features

#### [NEW] [SplashActivity.kt](file:///E:/UsedCarParts/app/src/main/java/com/example/usedcarparts/ui/SplashActivity.kt) / [activity_splash.xml](file:///E:/UsedCarParts/app/src/main/res/layout/activity_splash.xml)
Language selection (English/Arabic) as per wireframe 1.
#### [NEW] [MainActivity.kt](file:///E:/UsedCarParts/app/src/main/java/com/example/usedcarparts/ui/main/MainActivity.kt) / [activity_main.xml](file:///E:/UsedCarParts/app/src/main/res/layout/activity_main.xml)
Home screen with Bottom Navigation and a RecyclerView for Listings (Wireframe 4).
#### [NEW] [PostListingActivity.kt](file:///E:/UsedCarParts/app/src/main/java/com/example/usedcarparts/ui/trader/PostListingActivity.kt) / [activity_post_listing.xml](file:///E:/UsedCarParts/app/src/main/res/layout/activity_post_listing.xml)
Trader-only feature to add new parts (Wireframe 7).

## Verification Plan

### Manual Verification
1.  Launch app: Verify Splash screen and Language selection.
2.  Registration: Create a Trader account with business details.
3.  Login: Login with the created account.
4.  Post Listing: Add a new car part.
5.  Home Screen: Verify the new listing appears in the feed.
