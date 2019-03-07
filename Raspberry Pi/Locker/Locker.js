// Bcrypt lib for hashing and store data in secure way
const bcrypt = require('bcrypt');
const saltRounds = 10;
var salt;
var hash;

var lockerName; 

module.exports = class Locker
{
    constructor(name, pin)
    {
        this.salt = bcrypt.genSaltSync(this.saltRounds);
        this.hash = bcrypt.hashSync(pin, this.salt);  
        this.lockerName = name;
    }

    setLockerHash()
    {
        this.hash = hash;
    }

    getLockerName()
    {
        return this.lockerName;
    }

    getLockerHash()
    {
        return this.hash;
    }
}