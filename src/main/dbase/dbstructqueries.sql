create database inventorydb;
use inventorydb;
drop database inventorydb;
insert into logincredentials(username , password) VALUES('abilesh','abilesh1002');
truncate table logindetails;
truncate table customer;
truncate table user;
INSERT INTO productlist (businessId, productName, description, price, category, isActive, createdDate)
VALUES (1, 'Burger', 'Chicken Burger', 120, 'Fast Food', true, CURDATE());
INSERT INTO ingredients (businessId, ingredientName, quantity, createdAt, isActive) VALUES
(1, 'Bun', 100, CURDATE(), true),
(1, 'Chicken Patty', 50, CURDATE(), true),
(1, 'Lettuce', 30, CURDATE(), true),
(1, 'Tomato', 30, CURDATE(), true),
(1, 'Cheese Slice', 40, CURDATE(), true),
(1, 'Mayonnaise', 20, CURDATE(), true),
(1, 'Ketchup', 20, CURDATE(), true),
(1, 'Onion', 30, CURDATE(), true);