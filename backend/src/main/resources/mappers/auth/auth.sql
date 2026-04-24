-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 172.30.1.94    Database: noos_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `eeg_results`
--

DROP TABLE IF EXISTS `eeg_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eeg_results` (
  `eeg_result_id` bigint NOT NULL AUTO_INCREMENT,
  `eeg_session_id` bigint NOT NULL,
  `delta` float NOT NULL,
  `theta` float NOT NULL,
  `alpha` float NOT NULL,
  `beta` float DEFAULT NULL,
  `gamma` float DEFAULT NULL,
  `dominant_band` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `state_label` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `confidence` float DEFAULT NULL,
  `focus_score` float DEFAULT NULL,
  `relax_score` float DEFAULT NULL,
  `stress_score` float DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`eeg_result_id`),
  UNIQUE KEY `uq_eeg_results_session` (`eeg_session_id`),
  KEY `idx_eeg_results_session_id` (`eeg_session_id`),
  CONSTRAINT `fk_eeg_results_session` FOREIGN KEY (`eeg_session_id`) REFERENCES `eeg_sessions` (`eeg_session_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eeg_results`
--

LOCK TABLES `eeg_results` WRITE;
/*!40000 ALTER TABLE `eeg_results` DISABLE KEYS */;
/*!40000 ALTER TABLE `eeg_results` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eeg_sessions`
--

DROP TABLE IF EXISTS `eeg_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eeg_sessions` (
  `eeg_session_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `device_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `measured_at` timestamp NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`eeg_session_id`),
  KEY `idx_eeg_sessions_user_id` (`user_id`),
  KEY `idx_eeg_sessions_measured_at` (`measured_at`),
  CONSTRAINT `fk_eeg_sessions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eeg_sessions`
--

LOCK TABLES `eeg_sessions` WRITE;
/*!40000 ALTER TABLE `eeg_sessions` DISABLE KEYS */;
/*!40000 ALTER TABLE `eeg_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedbacks`
--

DROP TABLE IF EXISTS `feedbacks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedbacks` (
  `feedback_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `eeg_session_id` bigint NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `comment` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rating` tinyint NOT NULL,
  `planet` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`feedback_id`),
  KEY `idx_feedbacks_user_id` (`user_id`),
  KEY `idx_feedbacks_session_id` (`eeg_session_id`),
  CONSTRAINT `fk_feedbacks_session` FOREIGN KEY (`eeg_session_id`) REFERENCES `eeg_sessions` (`eeg_session_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_feedbacks_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_feedbacks_rating` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedbacks`
--

LOCK TABLES `feedbacks` WRITE;
/*!40000 ALTER TABLE `feedbacks` DISABLE KEYS */;
/*!40000 ALTER TABLE `feedbacks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `login_id` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `display_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `provider` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'local',
  `provider_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `login_id` (`login_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'1','1','a','2026-03-15 07:26:29','2026-03-15 07:26:29','local',NULL),(4,'1@naver.com','1','aa','2026-03-15 07:32:44','2026-03-15 07:32:44','local',NULL),(5,'jh9man@gmail.com',NULL,'권순하','2026-03-15 07:39:54','2026-03-24 03:55:59','google','111933448587600909241'),(25,'jh1man@naver.com',NULL,'soonha01','2026-03-16 04:02:35','2026-03-16 04:04:52','github','210504729'),(29,'dd@naver.com','$2a$10$HUMeyfz.FGIXtZqYw0NkAO3HaDg9FB0.UQ4sz175fIMDU7/Jitj2i','sssssss','2026-03-22 08:26:20','2026-03-22 08:26:20','local',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-05 15:36:51

USE noos_db;
SELECT user_id, login_id, display_name, provider FROM users;

SELECT user_id, login_id, display_name FROM users WHERE login_id = 'atfqwe80@gmail.com';