//Importing bleno - Bluetooth server side control library
var bleno = require('bleno');

//GPIO
var Gpio = require('onoff').Gpio; //include onoff to interact with the GPIO
var LED = new Gpio(4, 'out'); //use GPIO pin 4, and specify that it is output
//var blinkInterval = setInterval(blinkLED, 1000); //run the blinkLED function every 250ms

// Include User's functionality
var userClass = require('./User/User');
var user;


function blinkLED() 
{ //function to start blinking
  if (LED.readSync() === 0) { //check the pin state, if the state is 0 (or off)
    LED.writeSync(1); //set pin state to 1 (turn LED on)
  } else {
    LED.writeSync(0); //set pin state to 0 (turn LED off)
  }
  console.log("BLINK!");
}

function endBlink() 
{ //function to stop blinking
  clearInterval(blinkInterval); // Stop blink intervals
  LED.writeSync(0); // Turn LED off
  LED.unexport(); // Unexport GPIO to free resources
}

// Once bleno starts, begin advertising our BLE address
bleno.on('stateChange', function(state) 
{
    user = new userClass("");
    console.log('State change: ' + state);
    if (state === 'poweredOn') 
    {
        bleno.startAdvertising('RaspberryPi',['12ab']);
        user.usersCheck();
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

    // TO DO : Implement User class
    if(user.verifyUser())
    {
      console.log("Jesteś!");
    }
    else
    {
      console.log("Nie ma Cię!");
    //   user.addUser();
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
    var userStatus = user.checkUserLoginStatus();
    var testData = "";
    if (error) 
    {
        console.log("Advertising start error:" + error);
    } 
    /* else {
        console.log("Advertising start success");
        bleno.setServices([
            
            // Define a new service
            new bleno.PrimaryService({
                uuid : '12ab',
                characteristics : [
                    
                    // Define a new characteristic within that service
                    new bleno.Characteristic({
                        value : null,
                        uuid : '34cd',
                        properties : ['notify', 'read', 'write'],
                        
                        // If the client subscribes, we send out a message every 1 second
                        onSubscribe : function(maxValueSize, updateValueCallback) {
                            console.log("Device subscribed");
                            this.intervalId = setInterval(function() {
                                console.log("Sending: Hi!");
                                updateValueCallback(new Buffer("Hi!"));
                            }, 1000);
                        },
                        
                        // If the client unsubscribes, we stop broadcasting the message
                        onUnsubscribe : function() {
                            console.log("Device unsubscribed");
                            clearInterval(this.intervalId);
                        },
                        
                        // Send a message back to the client with the characteristic's value
                        onReadRequest : function(offset, callback) {
                            console.log("Read request received");
                            callback(this.RESULT_SUCCESS, new Buffer("Echo: " + 
                                    (this.value ? this.value.toString("utf-8") : "")));
                            console.log(this.value);
                        },
                        
                        // Accept a new value for the characterstic's value
                        onWriteRequest : function(data, offset, withoutResponse, callback) {
                            this.value = data;
                            console.log('Write request: value = ' + this.value.toString("utf-8"));
                            callback(this.RESULT_SUCCESS);
                        }
 
                    })
                    
                ]
            })
        ]);
    }*/
    else 
    {
        bleno.setServices([
            
            
            // Define a new service
            new bleno.PrimaryService({
                uuid : '0000ffe9-0000-1000-8000-00805f9b34fb',
                characteristics : [
                    
                    // Define a new characteristic within that service
                    
                    new bleno.Characteristic({
                        value : null,
                        uuid : '00002902-0000-1000-8000-00805f9b34fb',
                        properties : ['read'],
                                                
                        // Send a message back to the client with the characteristic's value
                        onReadRequest : function(offset, callback) {
                            console.log("Read request received");
                            
                            console.log("------------------");
                            console.log(user.userLoginStatus);

                            callback(this.RESULT_SUCCESS, new Buffer(user.userLoginStatus.toString("utf-8")));
                        }
                    }),

                    // Define a new characteristic within that service
                    new bleno.Characteristic({
                        uuid : 'a922bc74-81dc-444a-8f5f-fbe1a4ec685c',
                        properties : ['write', "read" ],

                        // Accept a new value for the characterstic's value
                        onWriteRequest : function(data, offset, withoutResponse, callback)
                        {
                            dataStringParsed = data.toString('utf-8').split(":");
                            if( dataStringParsed[0] == 'A' )
                            {
                                switch(dataStringParsed[1])
                                {
                                    case "USER_CHECK":
                                    {
                                        console.log( " Verify User : " + user.verifyUser());
                                    }
                                }
                            }
                            if ( dataStringParsed[0] == "V" )
                            {
                                console.log("Value : " + dataStringParsed[1]);
                            }
                            callback(this.RESULT_SUCCESS);
                        },

                        // Send a message back to the client with the characteristic's value
                        onReadRequest : function(offset, callback) 
                        {
                            console.log("------------------");
                            console.log(user.userLoginStatus);
                            console.log("------------------");
                            console.log(this.RESULT_SUCCESS);
                            //callback(this.RESULT_SUCCESS, new Buffer("Echo: " + (this.value ? this.value.toString("utf-8") : "xd")));
                            
                            // TO DO: Respond with all data 
                            callback(this.RESULT_SUCCESS, new Buffer("Echo:"));
                        }
                    })    
                ]
            })
        ]);
    }
});