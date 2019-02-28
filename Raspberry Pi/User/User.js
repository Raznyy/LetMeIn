var fs = require('fs');
var userFilename = 'users.txt';
var userUUIDAdress;
var userLoginStatus;

module.exports =
{
  usersCheck: function ()
  {
    usersCheckFuntion();
  },
  verifyUser: function(clientAddress)
  {
    return verifyUserFunction(clientAddress);
  },
  addUser: function(clientAddress)
  {
    addUserFunction(clientAddress);
  },
  checkUserLoginStatus: function(clientAddress)
  {
    return checkUserLoginStatusFunction(clientAddress);
  }
}

function usersCheckFuntion() {
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

function verifyUserFunction(clientAddress)
{
  let data = fs.readFileSync(__dirname+"/"+userFilename);
  
  var stringData = data.toString();

  var users = stringData.split(",");
  for(var i=0;i<users.length;i++) 
  {
    if( users[i] == (clientAddress))
    {
      return true;
    }
    
  }
  return false;
}

function addUserFunction(clientAddress)
{
  fs.appendFileSync(__dirname+"/"+userFilename, clientAddress + ",");
  console.log("dodalem " + clientAddress);
}

function checkUserLoginStatusFunction( clientAddress )
{
  console.log(" User status " + this.userLoginStatus);
  return true;
}

