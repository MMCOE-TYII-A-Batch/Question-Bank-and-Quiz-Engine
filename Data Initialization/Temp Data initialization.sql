insert into "user" (username, email)
values
('Tushar Borate','tusharborate2024.comp@mmcoe.edu.in'),
('Sahil Veer','sahilveer2024.comp@mmcoe.edu.in'),
('Vishwesh Bhilare','vishweshbhilare2024.comp@mmcoe.edu.in'),
('Abhijeet Bedse','abhijeetbedse2024.comp@mmcoe.edu.in'),
('Pravin Ambhore','pravinambhore2024.comp@mmcoe.edu.in'),
('Avishkar Patil','avishkarpatil2024.comp@mmcoe.edu.in'),
('Neel Kadam','neelkadam2024.comp@mmcoe.edu.in'),
('Kshitij Bhate','kshitijbhate2024.comp@mmcoe.edu.in'),
('Mahesh Gaikwad','maheshgaikwad2024.comp@mmcoe.edu.in'),
('Pooja Umare','poojaumare2024.comp@mmcoe.edu.in'),
('Ayush Chandekar','ayushchandekar2024.comp@mmcoe.edu.in'),
('Ritik Mirase','ritikmirase2024.comp@mmcoe.edu.in'),
('Vedantika Ranaware','vedantikaranaware2024.comp@mmcoe.edu.in'),
('Rugved Deshwant','rugveddeshwant2024.comp@mmcoe.edu.in'),
('Swarada Deshpande','swaradadeshpande2024.comp@mmcoe.edu.in'),
('Jahnavi Dhanapune','jahnavidhanapune2024.comp@mmcoe.edu.in'),
('Vaidehi Shinde','vaidehishinde2024.comp@mmcoe.edu.in'),
('Yugant Varekar','yugantvarekar2024.comp@mmcoe.edu.in'),
('Sajiree Damle','sajireedamle2024.comp@mmcoe.edu.in'),
('Yash More','yashmore2024.comp@mmcoe.edu.in'),
('Gayatri Dethe','gayatridethe2024.comp@mmcoe.edu.in'),
('Srushti Shewale','srushtishewale2024.comp@mmcoe.edu.in'),
('Shravani Pataskar','shravanipataskar2024.comp@mmcoe.edu.in'),
('Jidnyasa Patil','jidnyasapatil2024.comp@mmcoe.edu.in'),
('Ashutosh Kulkarni','ashutoshkulkarni2024.comp@mmcoe.edu.in'),
('Balraj Rasal','balrajrasal2024.comp@mmcoe.edu.in'),
('Anula Borse','anulaborse2024.comp@mmcoe.edu.in'),
('Gunaji Mohite','gunajimohite2024.comp@mmcoe.edu.in'),
('Altamash Mujawar','altamashmujawar2024.comp@mmcoe.edu.in')
on conflict (email) do nothing;

select * from "user";

insert into role (role_id, role_name)
values (1,'Teacher'),
(2,'Student'),
(3,'Admin');

select * from role;

INSERT INTO user_role (user_id, role_id)
SELECT user_id, 2
FROM "user"
WHERE username IN (
    'Tushar Borate',
    'Sahil Veer',
    'Vishwesh Bhilare',
    'Abhijeet Bedse',
    'Pravin Ambhore',
    'Avishkar Patil', 
    'Neel Kadam',
    'Kshitij Bhate',
    'Mahesh Gaikwad',
    'Pooja Umare',
    'Ayush Chandekar',
    'Ritik Mirase',
    'Vedantika Ranaware',
    'Rugved Deshwant',
    'Swarada Deshpande',
    'Jahnavi Dhanapune',
    'Vaidehi Shinde',
    'Yugant Varekar',
    'Sajiree Damle',
    'Yash More',
    'Gayatri Dethe',
    'Srushti Shewale',
    'Shravani Pataskar',
    'Jidnyasa Patil',
    'Ashutosh Kulkarni',
    'Balraj Rasal',
    'Anula Borse',
    'Gunaji Mohite',
    'Altamash Mujawar'
)
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO "user" (username, email)
VALUES
('Dr. Sankirti S. Shiravale', 'sankirtishiravale@mmcoe.edu.in'),
('Dr. Kalpana S. Thakre', 'kalpanathakre@mmcoe.edu.in'),
('Dr. Girija G. Chiddarwar', 'girijachiddarwar@mmcoe.edu.in'),
('Dr. Swati N. Shekapure', 'swatishekapure@mmcoe.edu.in'),
('Dr. Geetha R. Chillarge', 'geethasb@mmcoe.edu.in'),
('Dr. Smita M. Chaudhari', 'smitachaudhari@mmcoe.edu.in'),
('Dr. Neha Jain', 'nehajain17@mmcoe.edu.in'),
('Dr. Anita V. Shinde', 'anitashinde@mmcoe.edu.in'),
('Dr. Shailaja B. Jadhav', 'jadhavsb@mmcoe.edu.in'),
('Ms. Rupali M. Dalvi', 'rupalidalvi@mmcoe.edu.in'),
('Ms. Shubhada P. Mone', 'shubhadamone@mmcoe.edu.in'),
('Ms. Jagruti A. Wagh', 'jagrutiwagh@mmcoe.edu.in'),
('Dr. Sarita D. Sapkal', 'saritasapkal@mmcoe.edu.in'),
('Ms. Vandana S. Rupnar', 'vandanarupnar@mmcoe.edu.in'),
('Ms. Swarupa M. Deshpande', 'swarupadeshpande@mmcoe.edu.in'),
('Mr. Rudragouda G. Patil', 'rudragoudapatil@mmcoe.edu.in'),
('Ms. Aishwarya S. Mane', 'aishwaryamane@mmcoe.edu.in'),
('Ms. Mayuri S. Shelke', 'mayurishelke@mmcoe.edu.in'),
('Ms. Kirti B. Satpute', 'kirtisatpute@mmcoe.edu.in'),
('Ms. Suvarna K. Patil', 'suvarnapatil@mmcoe.edu.in'),
('Ms. Reshma D. Kapadi', 'reshmakapadi@mmcoe.edu.in'),
('Ms. Mayuri G. Narkhede', 'mayurinarkhede@mmcoe.edu.in'),
('Ms. Snehal H. Kuche', 'snehalkuchee@mmcoe.edu.in'),
('Ms. Nikita D. Kolambe', 'nikitakolambe21@mmcoe.edu.in'),
('Ms. Yashashree V. Belkhede', 'yashashreebelkhede@mmcoe.edu.in'),
('Ms. Vidya Jaykumar Menoki', 'vidyamenoki@mmcoe.edu.in'),
('Ms. Gayatri S. Chavan', 'gayatrichavan@mmcoe.edu.in'),
('Mr. Mahesh A. Kandekar', 'maheshkandekar@mmcoe.edu.in')
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT user_id, 1
FROM "user"
WHERE username IN (
    'Dr. Sankirti S. Shiravale',
    'Dr. Kalpana S. Thakre',
    'Dr. Girija G. Chiddarwar',
    'Dr. Swati N. Shekapure',
    'Dr. Geetha R. Chillarge',
    'Dr. Smita M. Chaudhari',
    'Dr. Neha Jain',
    'Dr. Anita V. Shinde',
    'Dr. Shailaja B. Jadhav',
    'Ms. Rupali M. Dalvi',
    'Ms. Shubhada P. Mone',
    'Ms. Jagruti A. Wagh',
    'Dr. Sarita D. Sapkal',
    'Ms. Vandana S. Rupnar',
    'Ms. Swarupa M. Deshpande',
    'Mr. Rudragouda G. Patil',
    'Ms. Aishwarya S. Mane',
    'Ms. Mayuri S. Shelke',
    'Ms. Kirti B. Satpute',
    'Ms. Suvarna K. Patil',
    'Ms. Reshma D. Kapadi',
    'Ms. Mayuri G. Narkhede',
    'Ms. Snehal H. Kuche',
    'Ms. Nikita D. Kolambe',
    'Ms. Yashashree V. Belkhede',
    'Ms. Vidya Jaykumar Menoki',
    'Ms. Gayatri S. Chavan',
    'Mr. Mahesh A. Kandekar'
)
ON CONFLICT (user_id, role_id) DO NOTHING;

select * from user_role; 