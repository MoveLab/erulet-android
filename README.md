Erulet - Android Application
===========================

Erulet is an open source mobile application that aims to disseminate information, facilitate visits, and engage the public to participate in studying and protecting natural areas of the Val d'Aran.

Detailed Specifications
-----------------

**General**

* Multiplatform: Android, iOS, HTML5
* Free and open source software: Licensed under GPLv3.
* Multilingual: User can select between Aranese, Catalan, Casillian, French, and English.
* Optimized for outdoor use: Works offline with minimal battery drain

**Account Registration**

* When user first installs app, they are given option to create an account with a username and password. They can decline to do so, in which case they use the app anonymously but cannot later log in to view their data on the web, or they can choose a username and password that will let them log into to the server later. There is also the option of registering an "expert" account, which requires that the users already have a code.
* This activity will be implemented on the server side, so the app itself just needs a webview with javascript enabled.

**Content download**

* The app lets users choose which content to donwload for offline use. This includs maptiles, which can be served from within the app when there is no connection. 


**Route Selection**

* Displays large map with each route marked with one icon.
* When user taps the route icon, basic information about the route is displayed in a popup.
* When user taps again, the app changes to "explorer" mode, giving user options to follow the selected route (1) without any self-tracking, or (2) with self-tracking. A third option will be available for "expert" users who enter the app initially with a code: These users will also be able to track themselves along whatever path they follow in order to create new routes for future use. (Non-expert users do not have this free-form tracking option because we want to discourage people from hiking off established trails.)
 
**Explorer Mode Selector**

* This is a simple selector for the user to choose between the different explorer mode options.

**Explorer Mode: Map View**

* Regardless of the self-tracking option, the main view of the explorer mode activity is a map of the route displaying navigational waypoints, points of interest, and shared views -- each marked with a unique style of icon.
* Navigational waypoints display popup information that is useful for following the route: Distance to the next waypoint, directions, slope, etc.
* Points of interest display popups with information about important features that users pass along the route. This information will include text, photos, video, and audio content created by the project team.
* Visitor views contain content shared by other users. Only a limited number of these are downloaded for offline viewing when a user selects the given route.
* The map view includea a button that lets the user see their current location.
* It also includes a button that lets the user add "visitor views" -- multimedia content linked to a location on the map. Visitor views are made public only if the user selects this option, and only if sufficiently close to an actual route (since we want to discourage hiking off designated trails).
* If in self-tracking mode, the app estimates the user's location at regular intervals and displays the track on the map.

**Explorer Mode: Compass View**

* From the map view, users can select a compass icon to be taken to the compass view. This view shows a compass that is oriented based on the device's magnetic sensor. If the user enters this view after first clicking a navigational waypoint or a point of interest, the compass includes a small marker indicating the direction to this point.

**Ratings**

* Users can give ratings to any route, point of interest, visitor view, or indiviual photo or video.


**Data Model**

The following sql statements show the app's internal data madel, which is synced to the server:

```

CREATE TABLE `track` 
(
	`id` VARCHAR , 
	`name` VARCHAR , 
	PRIMARY KEY (`id`) 
) 

CREATE TABLE `route` 
(
	`description` VARCHAR , 
	`userId` VARCHAR , 
	`id` VARCHAR , 
	`idRouteBasedOn` VARCHAR , 
	`intImgId` VARCHAR , 				--> Reference to interactive image
	`localCarto` VARCHAR , 
	`name` VARCHAR , 
	`referenceId` VARCHAR , 
	`trackId` VARCHAR , 
	`upLoaded` SMALLINT , 
	`userRating` INTEGER , 
	PRIMARY KEY (`id`) 
) 

CREATE TABLE `step` 
(
	`absoluteTime` VARCHAR , 
	`trackId` VARCHAR , 
	`routeId` VARCHAR , 				--> For shared steps only
	`hlId` VARCHAR , 
	`id` VARCHAR , 
	`referenceId` VARCHAR , 
	`order` INTEGER , 
	`longitude` DOUBLE PRECISION , 
	`precision` DOUBLE PRECISION , 
	`latitude` DOUBLE PRECISION , 
	`altitude` DOUBLE PRECISION , 
	`absoluteTimeMillis` BIGINT , 
	PRIMARY KEY (`id`) 
) 

CREATE TABLE `highlight` 
(
	`name` VARCHAR , 
	`id` VARCHAR , 
	`longText` VARCHAR , 
	`mediaPath` VARCHAR , 
	`radius` DOUBLE PRECISION , 
	`userRating` INTEGER ,
	`type` INTEGER , 
	 PRIMARY KEY (`id`) 
) 

CREATE TABLE `reference` 
(
	`id` VARCHAR , 
	`name` VARCHAR , 
	`textContent` VARCHAR , 
	PRIMARY KEY (`id`) 
)

CREATE TABLE `interactiveimage` 
(
	`id` VARCHAR , 
	`mediaPath` VARCHAR , 
	`originalHeight` INTEGER , 
	`originalWidth` INTEGER , 
	PRIMARY KEY (`id`) 
) 

CREATE TABLE `box` 
(
	`id` VARCHAR , 
	`interactiveImageId` VARCHAR , 
	`message` VARCHAR , 
	`maxY` INTEGER , 
	`maxX` INTEGER , 
	`minX` INTEGER , 
	`minY` INTEGER , 
	PRIMARY KEY (`id`) 
) 
```
