//Importing bleno - Bluetooth server side control library
var bleno = require('bleno');

// Bcrypt lib for hashing and store data in secure way
const bcrypt = require('bcrypt');
const saltRounds = 10;
var salt,hash;

// readline-sync for reading the input in console to set PIN code from user
var readline = require('readline-sync');

// Reading system files 
var fs = require('fs');
var userFilename = 'users.txt';
var hashFilename = 'hash.txt';

//GPIO
var Gpio = require('onoff').Gpio; //include onoff to interact with the GPIO
var LED = new Gpio(4, 'out'); //use GPIO pin 4, and specify that it is output
//var blinkInterval = setInterval(blinkLED, 1000); //run the blinkLED function every 250ms

// Include User's functionality
var userClass = require('./User/User');
var user = new userClass();

//Locker init
var lockerObject = require('./Locker/Locker.js');
var locker;
var verification = false;
var lockerInit = onCreate();

function onCreate()
{
    checkLockerExist();
}

function checkLockerExist()
{
    try {
        fs.exists(__dirname+"/User/"+userFilename, function(exists)
        {
            initLocker();
        });
    } 
    catch(err)
    {   
        console.log("ERROR looking for file");
    }
}

// Inicializing Locker with entered Name and Pin code
function initLocker()
{
    user.usersCheck();
    var validateCode = false;
    var deviceName = readline.question("Set locker name:");
    var lockerPIN = readline.question("Set locker PIN code:");
    while(lockerPIN.length!=4)
    {
        console.log("Please enter 4 digits code");
        lockerPIN = readline.question("Set locker PIN code:");
        
    }
    while(!validateCode)
    {
        if(lockerPIN.match(/^-{0,1}\d+$/))
        {
            console.log("Code provided.");
            validateCode = true;
        }
        else
        {  
            console.log("Only number accepted.");
            validateCode = false;
            lockerPIN = readline.question("Set locker PIN code:");
        }
    }
    this.locker = new lockerObject(deviceName, lockerPIN,);

    //saving hash file to decode pin 
    createhashFile();

    console.log("Locker setup finished!");
}

function createhashFile()
{
    try {
        fs.open(__dirname+"/Locker/"+hashFilename,'r',function(err, fd){
            if (err) {
                fs.writeFile(__dirname+"/Locker/"+hashFilename, getLockerHash(), function(err) 
                {
                    if(err) {
                        console.log(err);
                    }
                    console.log("The hash file was saved!");
                });
            } 
            else 
            {
                console.log("The hash file exists!");
            }
        });
    }
    catch(err)
    {
        console.log("Error reading Hash file!");
    }
}

function getLockerName()
{
    return this.locker.getLockerName();
}

function getLockerHash()
{
    return this.locker.getLockerHash();
}

function setVerification(verStatus)
{
    this.verification = verStatus;
}

function getVerification()
{
    return this.verification;
}

/* GPIO FUNTIONS  */ 
function open() 
{  
    LED.writeSync(1);
    console.log("Doors open for 5 seconds");
}

function close() 
{
  LED.writeSync(0); // Turn Voltage off
  LED.unexport(); // Unexport GPIO to free resources
  console.log("Doors closed");
}

/* -------------- */ 

// Once bleno starts, begin advertising our BLE address
bleno.on('stateChange', function(state) 
{
    console.log('State change: ' + state);
    if (state === 'poweredOn') 
    {
        bleno.startAdvertising(getLockerName(), ['12ab']);
    } else if(state === 'unauthorized')
    {
      console.log("unauthorized");
    }
    else 
    {
        bleno.stopAdvertising();
    }
});
 
// Notify the console that we've accepted a connection
bleno.on('accept', function(clientAddress) 
{
    console.log("Accepted connection from address: " + clientAddress);
    
    user.setUUIDAdress(clientAddress);


    if(user.verifyUser())
    {
      console.log("User already paired.");
    }
    else
    {
      console.log("User not detected in paired devices base.");
    }
});

// Notify the console that we have disconnected from a client
bleno.on('disconnect', function(clientAddress) 
{
    console.log("Disconnected from address: " + clientAddress);
});

// When we begin advertising, create a new service and characteristic
bleno.on('advertisingStart', function(error) 
{
    if (error) 
    {
        console.log("Advertising start error:" + error);
    } 
    else 
    {
        bleno.setServices([
            
            // Service for autocheck User on connection init
            new bleno.PrimaryService({
                uuid : '0000ffe9-0000-1000-8000-00805f9b34fb',
                characteristics : [
                        new bleno.Characteristic({
                        value : null,
                        uuid : '00002902-0000-1000-8000-00805f9b34fb',
                        properties : ['read'],
                                                
                        // Send a message back to the client with the characteristic's value
                        onReadRequest : function(offset, callback) 
                        {
                                                    
                            console.log("Checking user access....");
                            console.log("User acces status : " + user.userLoginStatus);

                            // If user's already paired, open GPIO 
                            if(user.verifyUser())
                            {
                                open();
                                setTimeout(close, 10000);
                            }

                            callback(this.RESULT_SUCCESS, new Buffer(user.userLoginStatus.toString("utf-8")));
                        }
                    }),

                    // Service for new users ( acces with PIN code entered )
                    new bleno.Characteristic({
                        uuid : 'a922bc74-81dc-444a-8f5f-fbe1a4ec685c',
                        value : false,
                        properties : [ 'read', 'write' ],

                        // Reading pinVerification value
                        onReadRequest : function(offset, callback) 
                        {                            
                            callback(this.RESULT_SUCCESS, new Buffer("PINVER" + getVerification().toString("utf-8")));
                        },

                        // Parsing data writen by user
                        onWriteRequest : function(data, offset, withoutResponse, callback)
                        {
                            // Recognize action passed by user
                            dataStringParsed = data.toString('utf-8').split(":");

                            console.log("Data string parsed " +  dataStringParsed[1]);
                            
                            if( dataStringParsed[1] == "USER_CHECK" ) 
                            {
                                console.log( " Verify User : " + user.verifyUser());
                                if(user.verifyUser())
                                {
                                    open();
                                    setTimeout(close, 10000);
                                }
                            }
                            else if( dataStringParsed[1] == "PIN_CHECK" )
                            { 
                                // bcrypt - checking hased pin with the one from USer 
                                bcrypt.compare(dataStringParsed[3], getLockerHash() , function(err, res) 
                                {
                                    var PINValidation = res;
                                    console.log("PIN validation :  " + PINValidation);
                                    if(PINValidation)
                                    {
                                        setVerification(true);
                                        user.addUser();
                                        open();
                                        setTimeout(close, 5000);
                                    }
                                    else
                                    {
                                        setVerification(false);
                                    }
                                    callback(this.RESULT_SUCCESS);
                                });  
                            }
                        }                        
                    }),
                ]
            })
        ]);
    }
});