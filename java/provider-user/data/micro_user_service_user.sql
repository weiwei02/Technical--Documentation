CREATE TABLE `micro-user-service`.user
(
    id bigint(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    username varchar(40) DEFAULT '' NOT NULL,
    name varchar(20) DEFAULT '',
    age int(3) DEFAULT '0',
    balance decimal(10,2) DEFAULT '0.00'
);
INSERT INTO `micro-user-service`.user (username, name, age, balance) VALUES ('account1', '张三', 20, 100.00);
INSERT INTO `micro-user-service`.user (username, name, age, balance) VALUES ('account2', '李四', 28, 180.00);
INSERT INTO `micro-user-service`.user (username, name, age, balance) VALUES ('account3', '王五', 32, 280.00);