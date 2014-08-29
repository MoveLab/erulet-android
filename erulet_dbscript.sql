DROP TABLE `route` 
DROP TABLE `track` 
DROP TABLE `step` 
DROP TABLE `highlight` 
DROP TABLE `reference` 
DROP TABLE `interactiveimage`
DROP TABLE `box` 

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
	`globalRating` INTEGER , 			--> Ratings
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
	`name` VARCHAR , 
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
	`globalRating` INTEGER , 				--> Ratings
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
	`heatPath` VARCHAR , 
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
