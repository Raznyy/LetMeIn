var LockerName = "RaspberryPi";
var lockerPIN = "1111";

module.exports = class Locker
{
    constructor(name, pin)
    {
        this.LockerName = name;
        this.lockerPIN = pin;
    }

    checkPIN( pin )
    {
        if ( this.lockerPIN == pin )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}