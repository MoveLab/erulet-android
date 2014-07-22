DROP TABLE `route` 
DROP TABLE `track` 
DROP TABLE `step` 
DROP TABLE `highlight` 
DROP TABLE `reference` 

CREATE TABLE `track` 
(
	`id` VARCHAR , 
	`name` VARCHAR , 
	PRIMARY KEY (`id`) 
) 
CREATE TABLE `route` 
(
	`description` VARCHAR , 
	`ecoId` VARCHAR , 
	`userId` VARCHAR , 
	`id` VARCHAR , 
	`idRouteBasedOn` VARCHAR , 
	`name` VARCHAR , 
	`referenceId` VARCHAR , 
	`trackId` VARCHAR , 
	`upLoaded` SMALLINT , 
	`ecosystem` SMALLINT , 
	PRIMARY KEY (`id`) 
) 
CREATE TABLE `step` 
(
	`absoluteTime` VARCHAR , 
	`trackId` VARCHAR , 
	`referenceId` VARCHAR , 
	`hlId` VARCHAR , 
	`id` VARCHAR , 
	`name` VARCHAR , 
	`longitude` DOUBLE PRECISION , 
	`latitude` DOUBLE PRECISION , 
	`absoluteTimeMillis` BIGINT , 
	`precision` DOUBLE PRECISION , 
	`altitude` DOUBLE PRECISION , 
	`order` INTEGER , 
	PRIMARY KEY (`id`) 
) 
CREATE TABLE `highlight` 
(
	`id` VARCHAR , 
	`longText` VARCHAR , 
	`mediaPath` VARCHAR , 
	`name` VARCHAR , 
	`radius` DOUBLE PRECISION , 
	PRIMARY KEY (`id`) 
) 
CREATE TABLE `reference` 
(
	`id` VARCHAR , 
	`name` VARCHAR , 
	`textContent` VARCHAR , 
	PRIMARY KEY (`id`) 
) 
