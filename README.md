# Follow Bellow Steps To Initialize Database On your machine

## - STEP 1
Install postgresql and pgAdmin as per https://www.youtube.com/watch?v=4qH-7w5LZsA

## - Step 2
**After installation is completed open a local terminal then run the following commands:**

cd '.\Database Initialization\'
cp .\.env.example .\.env

Open the newly created .env file and fill in the fields as per the instructions present in the file

## - Step 3
Open PgAdmin from start and follow the bellow steps one by one:

### 1 Left click on the 'Servers' in the left section and enter password  

### 2 Click on the dropdown arrow to show options like,
![Reference Img 1](Database%20Initialization/Reference%20Images/ref%201.png)

### 3 Right click on 'Databases' and click create, database and create a database with any name

### 4 Click the dropdown arrow next to 'Databases' and  right click the newly created database then press Query Tool

![Reference Img 1](Database%20Initialization/Reference%20Images/ref%202.png)

### 5 Click on the open file button and select the Database.sql file from the Database Initialization directory

![Reference Img 1](Database%20Initialization/Reference%20Images/ref%203.png)

### 6 Press the execute script button and if you get this output database has been initialized

![Reference Img 1](Database%20Initialization/Reference%20Images/ref%204.png)
![Reference Img 1](Database%20Initialization/Reference%20Images/ref%205.png)




