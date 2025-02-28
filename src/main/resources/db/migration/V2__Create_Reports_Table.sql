INSERT INTO AreaType (id, type_name)
VALUES ('URBAN', 'Urban'),
       ('RURAL', 'Rural');

INSERT INTO Unit (id, unit_name)
VALUES (1, 'REB'),
       (2, 'BPLA'),
       (3, 'Logistik'),
       (4, 'IT');

INSERT INTO Region (id, region_name)
VALUES (1, 'Kharkiv'),
       (2, 'Khortitsya'),
       (3, 'Pivden'),
       (4, 'Kursk');

INSERT INTO positions (id, position_name)
VALUES (1, 'Zapor'),
       (2, 'GuliayPole');

INSERT INTO positions (position_name)
VALUES ('Командир роти'),
       ('Командир взводу'),
       ('Командир відділення'),
       ('Оператор звязку'),
       ('Оператор радіолокаційної станції'),
       ('Оператор БПЛА'),
       ('Водій автомобіля'),
       ('Водій танка'),
       ('Водій БТР'),
       ('Механік'),
       ('Технік'),
       ('Сержант'),
       ('Старший сержант'),
       ('Молодший лейтенант'),
       ('Лейтенант'),
       ('Старший лейтенант'),
       ('Капітан'),
       ('Майор'),
       ('Підполковник'),
       ('Полковник'),
       ('Бригадний генерал'),
       ('Генерал-майор'),
       ('Генерал-лейтенант'),
       ('Генерал-полковник');

INSERT INTO Contractor (id, first_name, last_name, middle_name, nick_name, gender, birth_date, nationality,
                        date_of_arrival_to_unit, contractor_status, c_rank, position_id, unit_id, created_by,
                        updated_by)
VALUES ('12345', 'John', 'Doe', 'Middle', 'JD', 'M', '1990-01-01', 'American',
        '2020-01-01', 'Service', 'Private', 1, 2, 'Admin', 'Admin');


-- Insert Regions
INSERT INTO Region (region_name)
VALUES ('Київська область'),
       ('Харківська область'),
       ('Одеська область'),
       ('Львівська область');

-- Insert Groups
INSERT INTO `Group` (id, name, region_id)
VALUES (1, 'Група Київ', 1),
       (2, 'Група Харків', 2),
       (3, 'Група Одеса', 3),
       (4, 'Група Львів', 4);

-- Insert Group Reports
INSERT INTO GroupReport (id, group_id, description, is_worked, created_by, updated_by)
VALUES (1, 1, 'Опис діяльності групи Київ', TRUE, 'Admin', 'Admin'),
       (2, 2, 'Опис діяльності групи Харків', TRUE, 'Admin', 'Admin'),
       (3, 3, 'Опис діяльності групи Одеса', TRUE, 'Admin', 'Admin'),
       (4, 4, 'Опис діяльності групи Львів', TRUE, 'Admin', 'Admin');

-- Insert Region Reports
INSERT INTO RegionReport (id, report_date, region_id, region_description, created_by, updated_by)
VALUES (1, '2023-01-01', 1, 'Опис регіону Київ', 'Admin', 'Admin'),
       (2, '2023-01-02', 2, 'Опис регіону Харків', 'Admin', 'Admin'),
       (3, '2023-01-03', 3, 'Опис регіону Одеса', 'Admin', 'Admin'),
       (4, '2023-01-04', 4, 'Опис регіону Львів', 'Admin', 'Admin');

