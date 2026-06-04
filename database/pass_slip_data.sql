-- MySQL dump 10.13  Distrib 9.7.0, for Win64 (x86_64)
--
-- Host: localhost    Database: pass_slip_db
-- ------------------------------------------------------
-- Server version	9.7.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '05196a1f-51ee-11f1-ac9e-8c8caaf81f30:1-57';

--
-- Table structure for table `activity_logs`
--

DROP TABLE IF EXISTS `activity_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_logs` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `emp_id` int DEFAULT NULL,
  `action` varchar(255) NOT NULL,
  `timestamp` datetime DEFAULT CURRENT_TIMESTAMP,
  `performed_by` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `emp_id` (`emp_id`),
  KEY `performed_by` (`performed_by`),
  CONSTRAINT `activity_logs_ibfk_1` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`emp_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_logs`
--

LOCK TABLES `activity_logs` WRITE;
/*!40000 ALTER TABLE `activity_logs` DISABLE KEYS */;
INSERT INTO `activity_logs` VALUES (1,2,'Pass slip created for Justin Gian','2026-06-02 15:54:08','Admin'),(2,2,'Pass slip #1 approved','2026-06-02 15:55:32','Admin'),(3,2,'Pass slip PS-0001 printed','2026-06-02 16:21:38','Admin'),(4,1,'Pass slip created for Kevin Brian','2026-06-04 13:31:58','Admin');
/*!40000 ALTER TABLE `activity_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `emp_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `department` varchar(100) NOT NULL,
  `position` varchar(100) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`emp_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'Kevin Brian','IT Department','IT Staff','2026-06-02 07:35:23'),(2,'Justin Gian','HR Department','HR Officer','2026-06-02 07:35:23'),(3,'Nico Ancheta','Finance','Finance Staff','2026-06-02 07:35:23'),(4,'Josiah David','Marketing','Marketing Staff','2026-06-02 07:35:23'),(5,'LJ Catindig','Operations','Operations Staff','2026-06-02 07:35:23'),(6,'Emil Fernandez','IT Department','IT Staff','2026-06-02 07:35:23'),(7,'Ryken Gabriel','Admin','Administrative Staff','2026-06-02 07:35:23');
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pass_slip`
--

DROP TABLE IF EXISTS `pass_slip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pass_slip` (
  `slip_id` int NOT NULL AUTO_INCREMENT,
  `emp_id` int NOT NULL,
  `reason` varchar(255) NOT NULL,
  `time_out` datetime NOT NULL,
  `time_in` datetime DEFAULT NULL,
  `duration` varchar(50) DEFAULT NULL,
  `issued_by` int NOT NULL,
  `status` enum('Pending','Approved','Rejected','Returned') DEFAULT 'Pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`slip_id`),
  KEY `emp_id` (`emp_id`),
  KEY `issued_by` (`issued_by`),
  CONSTRAINT `pass_slip_ibfk_1` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`emp_id`),
  CONSTRAINT `pass_slip_ibfk_2` FOREIGN KEY (`issued_by`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pass_slip`
--

LOCK TABLES `pass_slip` WRITE;
/*!40000 ALTER TABLE `pass_slip` DISABLE KEYS */;
INSERT INTO `pass_slip` VALUES (1,2,'iihi sa labas','2026-06-02 09:39:00','2026-06-02 11:20:00',NULL,1,'Approved','2026-06-02 07:54:08'),(2,1,'dsadada','2026-06-04 08:00:00','2026-06-04 17:00:00',NULL,1,'Pending','2026-06-04 05:31:58');
/*!40000 ALTER TABLE `pass_slip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('admin','staff') NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Administrator','admin@passlip.com','admin','Tei8uIWoDfKHfF5K7FUMsUI2YESiXhWTDHgZZGhtqaaa8oX30jI+CMTuqqQk6OSY','admin','2026-06-02 07:35:23'),(2,'Gian','gamingytgian@gmail.com','hexteria','hxf//Oh7lRd8WpkrnNeQsU7sfhdYzK7i6MqgiXBPZny1ss3bPR7VspasrwqaV+b2','admin','2026-06-02 07:38:49'),(3,'ryken lapating','ryken@gmail.com','ryken','eGLHGc2YxUcdlzZmXXciFdAEbK2Bk4bfOrzPSNaCNSIJQfpCLMyhTfVdDfSLyahM','staff','2026-06-02 08:29:12');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visitor`
--

DROP TABLE IF EXISTS `visitor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitor` (
  `visitor_id` int NOT NULL AUTO_INCREMENT,
  `visitor_name` varchar(100) NOT NULL,
  `company` varchar(100) DEFAULT NULL,
  `purpose` varchar(200) DEFAULT NULL,
  `time_out` datetime DEFAULT NULL,
  `time_in` datetime DEFAULT NULL,
  `host_employee` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `contact` varchar(20) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'Pending',
  PRIMARY KEY (`visitor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visitor`
--

LOCK TABLES `visitor` WRITE;
/*!40000 ALTER TABLE `visitor` DISABLE KEYS */;
INSERT INTO `visitor` VALUES (1,'ABDUL SALSALANI','PORNUHB','PAPAPWET','2026-06-02 16:17:10',NULL,'LAGUERTA',NULL,NULL,'Approved'),(2,'Gian','PUP Santa Rosa','aaaaaaaa','2026-06-03 10:00:00',NULL,'Emylou Alinsod','justingiansantos@gmail.com','09943502380','Approved'),(3,'Emil','PUP Santa Rosa','ssssssssasasasasasasa','2026-09-29 11:00:00',NULL,'Emy Alinsod','justingiansantos@gmail.com','09943502380','Approved'),(4,'JM','TEST','test','2026-09-09 10:00:00',NULL,'test','hexteriaaa@gmail.com','TEST','Approved'),(5,'test','test','asasfdjkfdsf','2026-08-09 11:00:00',NULL,'test','gamingytgian@gmail.com','test','Approved'),(6,'Mico Panguilinan','PUPSRC','Site Inspection','2026-06-03 17:00:00',NULL,'Engr. Emy Alinsod','panguilinan.mico@gmail.com','09763216543','Approved'),(7,'jm manalo','pupsrc','cisita','2026-06-03 10:20:00',NULL,'host','pup@email','09993939','Approved'),(8,'Kevin Brian A. Manalo','Cyber Security','Hacking The System','2026-06-03 10:00:00',NULL,'','manalo@gmail.com','09123213123','Approved'),(9,'Lj Delfino','PUP','sda','2026-06-03 09:30:00',NULL,'dasasddas','ljdelfino','0994754312','Approved'),(10,'Duan Jiaxu','Imong bahay','Wala lang','2026-06-04 15:00:00',NULL,'Ewan','delfinojm23@gmail.com','123456789','Approved'),(11,'Abdul Salsalani','Pornhub','Papawet','2026-06-04 03:30:00',NULL,'Laguerta','rykengabriell@gmail.com','0999 999 9999','Approved'),(12,'lj delfino','sadasd','','2026-06-03 09:00:00',NULL,'','virusinvictus1@gmail.com','sdadasd2','Approved'),(13,'Karl De Guzman','PUP','visit lang','2026-06-03 12:00:00',NULL,'host','karldeguzman08@gmail.com','0999838388282','Approved'),(14,'Angelo','gegeg','gegeg lj method','2026-07-01 05:00:00',NULL,'dfsdsd','anchetanico3@gmail.com','0993538383','Approved'),(15,'Anonymous','Cyber Security','Hacking The System','2026-06-03 10:00:00',NULL,'','manalokevin27@gmail.com','09123123123','Approved'),(16,'Test','Test','test','2026-09-09 10:00:00',NULL,'test','ayokongmabuhay@gmail.com','Test','Approved'),(17,'test','test','test','2026-06-04 17:00:00',NULL,'emy','','','Pending');
/*!40000 ALTER TABLE `visitor` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-04 13:51:25
