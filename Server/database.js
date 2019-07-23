var server = require('./index.js');
var emailModule = require('./email.js');
var utils = require('./utils.js');
var mysql = require('mysql');

var maxCharsUsername = 25;
var minCharsUsername = 3;
var maxCharsPassword = 255;
var minCharsPassword = 7;
var maxCharsEmail = 255;

var con = mysql.createConnection({
    host: "localhost",
    user: "TinSlam",
    password: "NimaTLS246davari246",
    database: "V3BashDB"
});

var start = function(){
    con.connect(function(err) {
        if (err) throw err;
        console.log("Connected!");
           // var sql = "CREATE TABLE accounts (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(25), email VARCHAR(255), hash VARCHAR(255), salt VARCHAR(255))";
        //    var sql = "INSERT INTO accounts (username, email, hash, salt) VALUES ('TinSlam', 'tinslam33@gmail.com', ?, ?)";
        //    var sql = "DROP TABLE accounts";
        var sql = "SELECT * FROM accounts";
        //    var sql = "DELETE FROM accounts WHERE id = 4";
        con.query(sql, function (err, result) {
        if (err) throw err;
        console.log(result);
        });
    });
}

var login = function dbLogin(socket, username, password){
    var sql = "SELECT * FROM accounts WHERE username = ?";
    con.query(sql, [username], function(err, result){
        if(err || result[0] == null){
            server.emitGlobal('loginFailed', {}, socket);
            return;
        }
        var hash = sha512(password, result[0].salt);
        var sql2 = "SELECT * FROM accounts WHERE username = ? AND hash = ?";
        con.query(sql2, [username, hash.passwordHash], function(err2, result2){
            if(err2 || result2[0] == null){
                server.emitGlobal('loginFailed', {}, socket);
                return;
            }
            var soc = utils.getSocketFromUsername(username);
            if(soc != null){
                server.emitGlobal('multipleLogins', {}, soc);
//                soc.disconnect();
                utils.removeSocketToUsernameMap(soc);
            }
            utils.addSocketToUsernameMap(socket, result[0].username);
            server.emitGlobal('loginSuccessful', {username : result[0].username}, socket);
        });
    });
}

function validEmail(email){
    var length = email.length;
    if(length > maxCharsEmail) return false;
    return true;
}

function validCharUsername(code){
    return (code >= 48 && code <= 57) ||
                    (code >= 65 && code <= 90) ||
                    (code >= 97 && code <= 122) ||
                    code == 95;
}

function validCharPassword(code){
    return code >= 33 && code <= 126;
}

function validPassword(password){
    var length = password.length;
    if(length < minCharsPassword || length > maxCharsPassword) return false;
    for(var i = 0; i < length; i++){
        if(!validCharPassword(password.charCodeAt(i))){
            return false;
        }
    }
    return true;
}

function validUsername(username){
    var length = username.length;
    if(length < minCharsUsername || length > maxCharsUsername) return false;
    for(var i = 0; i < length; i++){
        if(!validCharUsername(username.charCodeAt(i))){
            return false;
        }
    }
    return true;
}

var register = function dbRegister(socket, username, password, email){
    if(!(validUsername(username)) || !(validPassword(password)) || !(validEmail(email))){
        server.emitGlobal('registerFailed', {}, socket);
        return;
    }
    var sql = "SELECT * FROM accounts WHERE username = ? OR email = ?";
    con.query(sql, [username, email], function(err, result){
        if(result[0] != null){
            server.emitGlobal('registerFailed', {}, socket);
            return;
        }
        var sql2 = "INSERT INTO accounts (username, email, hash, salt) VALUES (?, ?, ?, ?)";
        var pass = saltHashPassword(password);
        var code = genRandomString(4);
        while(utils.verificationCodeToUserMap.has(code)){
            code = genRandomString(4);
        }
        var myVar = setInterval(myTimer, 1000 * 60 * 10);
        function myTimer() {
//            if(verificationCodeToUserMap.has(code)){
            utils.removeVerificationCodeToUserMap(code);
            clearInterval(myVar);
//            }
        }
        emailModule.sendVerificationEmail(email, code);
        utils.addVerificationCodeToUserMap(code, {username : username, email : email, hash : pass.passwordHash, salt : pass.salt});
        server.emitGlobal('verify', {verifyCode : code}, socket);
    });
}

var createUser = function createUser(user, socket){
    var username = user.username;
    var email = user.email;
    var hash = user.hash;
    var salt = user.salt;
    var sql = "SELECT * FROM accounts WHERE username = ? OR email = ?";
    con.query(sql, [username, email], function(err, result){
        if(result[0] != null){
            server.emitGlobal('verifyFailed', {}, socket);
            return;
        }
        var sql2 = "INSERT INTO accounts (username, email, hash, salt) VALUES (?, ?, ?, ?)";
        con.query(sql2, [username, email, hash, salt], function(err2, result2){
            server.emitGlobal('registerSuccessful', {}, socket);
            console.log("User registered " + user.username);
        });
    });
}

// Password hashing --------------------------------------------------------------------------------

var crypto = require('crypto');

var genRandomString = function(length){
    return crypto.randomBytes(Math.ceil(length/2))
            .toString('hex') /** convert to hexadecimal format */
            .slice(0,length);   /** return required number of characters */
};

var sha512 = function(password, salt){
    var hash = crypto.createHmac('sha512', salt); /** Hashing algorithm sha512 */
    hash.update(password);
    var value = hash.digest('hex');
    return {
        salt:salt,
        passwordHash:value
    };
};

function saltHashPassword(userpassword) {
    var salt = genRandomString(16); /** Gives us salt of length 16 */

    return passwordData = sha512(userpassword, salt);
}

module.exports.start = start;
module.exports.login = login;
module.exports.register = register;
module.exports.createUser = createUser;