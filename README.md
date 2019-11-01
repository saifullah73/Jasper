# Jasper

## Overview
Jasper is an Android chat Application based on the XMPP protocol, it uses SMACK as the client-side library and Openfire as its Xmpp Server.
A seperate MYSQL server is also set up to maintain chat histories. Jasper features a modular desgin, allowing for easy extension in
functionality.

![alt text](https://raw.githubusercontent.com/saifullah73/Jasper/main/1.png)

## Features
- One to One Chat
- Dynaminc Contact Addition
- Login/Signup
- Image/File Sharing
- Location Sharing
- Chat History
- Notifications


## How to setup
Steps to setup the app locally are as follows. If you wish To simply use the demo app, you can download and install the preconfigured app, 
please refer to the ***Links***.

### Dependencies
The following dependecies are required only if you wish to modify the app according to your needs, or extend it as per your will. 
#### Server
- Openfire (XMPP Server),perferably or any other XMPP supported server of your choice
#### History
- MySQL Server installed and running
#### Client Side
- Smack (Xmpp Client) (automatically downloaded)
- Your own Firebase's google-services.json file to connect the app to your Firebase project
#### IDE
- Android Studio
- Any text editor of your choice (for modification of node.js files)

### Android Application
To set up the android application, clone the code and build it locally. All the necessary libraries will be automatically downloaded and
integrated.
Modify the ***string.xml*** file to properly configure the android appliation to your own self-deployed Server.
If you wish to connect the app to your own Firebase project, please replace ***google-services.json*** file by your own version of the file.

### Chat History Server
Chat History Server is a MySQL Server. Follow the following steps to set it up
1. Download and install SQL Server
2. Ensure that SQL server is running after installation
3. Download ***node.js Server*** files from the ***Links*** section
4. Download and install Node.js
5. Download the following package using npm
   - mysql
   - express
   - sort-json-array
   - body-parser
   - Command to install `npm install --save [packagename]`
6. Modify ***db.js*** file acquired from ***node.js Server*** with your own credential for connection to SQL server
```
var conn = mysql.createConnection({
    host:"localhost",
    user:"root",
    password:"qwerty123",
})
```
7. Execute the following query with SQL `ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password'`
8. Run the ***app.js*** application by typing `node app.js ` in the terminal
9. Your chat history server will be setup properly at this stage.

### Xmpp Server
1. Download and install Openfire from "https://www.igniterealtime.org/downloads/"
2. You can either configure OpenFire to use an external database , or an embedded database, the choice will not affect you system.
3. Once installed Openfire can be accessed from "http://localhost:9090/"
4. Log in to your server via the admin credential set up by you during configuration. 
5. Make sure "Inbound Account Registration" is enabled under "Server Settings > Registration and Login"

## Links
- Link to demo app with backend deployed in Azure VM "https://drive.google.com/file/d/1mc15ieAIqX3xR1l_W_k0-j53xAAMBuaA/view?usp=sharing"
- Link to Node.js application to support Chat History "https://drive.google.com/drive/folders/1G0hv_f8Fg4KN6AhF9g6AOdYyKmXoXbYN?usp=sharing"


    
