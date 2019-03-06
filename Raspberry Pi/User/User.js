//Import file handling library
var fs = require('fs');
var userFilename = 'users.txt';

var userUUIDAdress;
var userLoginStatus;

module.exports = class User
{

  setUUIDAdress(userUUIDAdress)
  {
    this.userUUIDAdress = userUUIDAdress;
    console.log("New user appear");
  }

  usersCheck() {
    fs.open(__dirname+"/"+userFilename,'r',function(err, fd){
      if (err) {
        fs.writeFile(__dirname+"/"+userFilename, '', function(err) {
            if(err) {
                console.log(err);
            }
            console.log("The file was saved!");
            
        });
      } 
      else 
      {
        console.log("The file exists!");
      }
    });
  }
  
  verifyUser()
  {
    let data = fs.readFileSync(__dirname+"/"+userFilename);
    
    var stringData = data.toString();
  
    var users = stringData.split(",");
    for(var i=0;i<users.length;i++) 
    {
      if( users[i] == (this.userUUIDAdress))
      {
        this.userLoginStatus = 'approved';
        return true;
      }      
    }
    this.userLoginStatus = 'declined';
    return false;
  }
  
  addUser()
  {
    fs.appendFileSync(__dirname+"/"+userFilename, this.userUUIDAdress + ",");
    console.log("dodalem " + this.userUUIDAdress);
  }
  
  checkUserLoginStatus()
  {
    console.log(" User status " + this.userLoginStatus);
    return true;
  }

}
