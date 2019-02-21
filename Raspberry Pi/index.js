var bleno = require('bleno');

//GPIO
var Gpio = require('onoff').Gpio; //include onoff to interact with the GPIO
var LED = new Gpio(4, 'out'); //use GPIO pin 4, and specify that it is output
//var blinkInterval = setInterval(blinkLED, 250); //run the blinkLED function every 250ms

var fs = require('fs');
var userFilename = 'users.txt';

// Once bleno starts, begin advertising our BLE address
bleno.on('stateChange', function(state) {
    console.log('State change: ' + state);
    if (state === 'poweredOn') {
        bleno.startAdvertising('RaspberryPi',['12ab']);
        usersCheck();
        console.log(__dirname);
    } else if(state === 'unauthorized'){
      console.log("unauthorized");
    }
    else {
        bleno.stopAdvertising();
    }
});
 
// Notify the console that we've accepted a connection
bleno.on('accept', function(clientAddress) {
    console.log("Accepted connection from address: " + clientAddress);
    if(verifyUser(clientAddress))
    {
      console.log("Jesteś!");
    }
    else
    {
      console.log("Nie ma Cię!");
      addUser(clientAddress);
    }
});

function verifyUser(clientAddress)
{
  let data = fs.readFileSync(__dirname+"/"+userFilename);
  
  var stringData = data.toString();
  console.log(clientAddress.length);
  console.log("XXXXXXXXXXXXXXX");

  var arr = stringData.split(",");
  console.log(arr);
  for(var i=0;i<arr.length;i++) 
  {
    console.log( arr[i] + " NIE ROWNA SIE " + clientAddress);
    console.log(arr[i].length);
    
    
    if( arr[i] == (clientAddress))
    {
      return true;
    }
    
  }
  return false;

  
}

function addUser(clientAddress)
{
  
  fs.appendFileSync(__dirname+"/"+userFilename, clientAddress + ",");
  console.log("dodalem " + clientAddress);
}

function usersCheck() {
  fs.open(__dirname+"/"+userFilename,'r',function(err, fd){
    if (err) {
      fs.writeFile(__dirname+"/"+userFilename, '', function(err) {
          if(err) {
              console.log(err);
          }
          console.log("The file was saved!");
      });
    } else {
      console.log("The file exists!");
    }
  });
}
 
// Notify the console that we have disconnected from a client
bleno.on('disconnect', function(clientAddress) {
    console.log("Disconnected from address: " + clientAddress);
});

function blinkLED() { //function to start blinking
  if (LED.readSync() === 0) { //check the pin state, if the state is 0 (or off)
    LED.writeSync(1); //set pin state to 1 (turn LED on)
  } else {
    LED.writeSync(0); //set pin state to 0 (turn LED off)
  }
  console.log("BLINK!");
}

function endBlink() { //function to stop blinking
  clearInterval(blinkInterval); // Stop blink intervals
  LED.writeSync(0); // Turn LED off
  LED.unexport(); // Unexport GPIO to free resources
}

// When we begin advertising, create a new service and characteristic
bleno.on('advertisingStart', function(error) {
    if (error) {
        console.log("Advertising start error:" + error);
    } else {
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
                            console.log(callback);
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
    }
});