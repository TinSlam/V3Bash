var HashMap = require('hashmap');
//var fs = require('fs');

var udpAddressToSocketMap = new HashMap();
var socketToUdpAddressMap = new HashMap();
var socketToMatchMap = new HashMap();
var socketToUsernameMap = new HashMap();
var usernameToSocketMap = new HashMap();
var verificationCodeToUserMap = new HashMap();
var socketIdToSocketMap = new HashMap();

//fs.writeFile('accounts.txt', 'Hello World!', function (err) {
//    if (err)
//        return console.log(err);
//    console.log('Hello World > helloworld.txt');
//});

function distance(x1, y1, x2, y2){
    return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
}

function getDigits(num){
    var strNum = num.toString();
    var output = new Array();
    for(var i = 0; i < strNum.length; i++){
        output.push(+strNum.charAt(i));
    }
    return output;
}

function checkIfAlreadyInArray(ele, arr){
	for(var i = 0; i < arr.length; i++){
		if(ele == arr[i]) return true;
	}

	return false;
}

function removeElementFromArray(ele, arr){
    var index = arr.indexOf(ele);
    if (index > -1) {
        arr.splice(index, 1);
    }
}

function getElementsNotInSecondArray(a1, a2){
    var elements = new Array();
    for(var i = 0; i < a1.length; i++){
        if(!checkIfAlreadyInArray(a1[i], a2)){
            elements.push(a1[i]);
        }
    }
    return elements;
}

function haveSameMembers(a1, a2){
    if(a1.length != a2.length) return false;
    for(var i = 0; i < a1.length; i++){
        if(!checkIfAlreadyInArray(a1[i], a2)) return false;
    }
    return true;
}

function addUdpAddressToSocketMap(address, socket){
    if(socket == null, address == null) return;
    udpAddressToSocketMap.set(address.address + address.port.toString(), socket);
    socketToUdpAddressMap.set(socket, {address : address.address, port : address.port});
    var t = setInterval(function(){
        removeUdpAddressToSocketMap(address);
        clearInterval(t);
    }, 20 * 60 * 1000);
}

function addSocketToUsernameMap(socket, username){
    if(socket == null || username == null) return;
    username = username.toLowerCase();
    socketToUsernameMap.set(socket, username);
    usernameToSocketMap.set(username, socket);
    var t = setInterval(function(){
        removeSocketToUsernameMap(socket);
        clearInterval(t);
    }, 24 * 60 * 60 * 1000);
}

function addSocketToMatchMap(soc, match){
    if(soc == null, match == null) return;
	socketToMatchMap.set(soc, match);
	var t = setInterval(function(){
        removeSocketToMatchMap(soc);
        clearInterval(t);
    }, 24 * 60 * 60 * 1000);
}

function addVerificationCodeToUserMap(code, user){
    if(code == null || user == null) return;
    verificationCodeToUserMap.set(code, user);
    var t = setInterval(function(){
        removeVerificationCodeToUserMap(code);
        clearInterval(t);
    }, 24 * 60 * 60 * 1000);
}


function getSocketFromUdpAddress(address){
    if(address == null) return;
    return udpAddressToSocketMap.get(address.address + address.port.toString());
}

function getUdpAddressFromSocket(socket){
    if(socket == null) return;
    return socketToUdpAddressMap.get(socket);
}

function getMatchNumberFromSocket(soc){
    if(soc == null) return null;
	return socketToMatchMap.get(soc);
}

function getUsernameFromSocket(socket){
    if(socket == null) return null;
    return socketToUsernameMap.get(socket);
}

function getSocketFromUsername(username){
    if(username == null) return null;
    username = username.toLowerCase();
    return usernameToSocketMap.get(username);
}

function getUserFromVerificationCode(code){
    if(code == null) return null;
    return verificationCodeToUserMap.get(code);
}

function removeSocketToMatchMap(soc){
    if(soc == null) return;
	socketToMatchMap.remove(soc);
}

function removeUdpAddressToSocketMap(address){
    if(address == null) return address;
    socketToUdpAddressMap.remove(getSocketFromUdpAddress(address));
    udpAddressToSocketMap.remove(address);
}

function replaceSocketToMatchMap(soc, socket){
    if(soc == null || socket == null) return;
    var tempMatch = getMatchNumberFromSocket(soc);
    if(tempMatch == null) return;
    removeSocketToMatchMap(soc);
    addSocketToMatchMap(socket, tempMatch);
}

function removeSocketToUsernameMap(socket){
    if(socket == null) return null;
    usernameToSocketMap.remove(getUsernameFromSocket(socket));
    socketToUsernameMap.remove(socket);
}

function replaceSocketToUsernameMap(soc, socket){
    if(soc == null || socket == null) return;
    var username = getUsernameFromSocket(soc);
    if(username == null) return;
    removeSocketToUsernameMap(soc);
    addSocketToUsernameMap(socket, username);
}

function removeVerificationCodeToUserMap(code){
    if(code == null) return;
    verificationCodeToUserMap.remove(code);
}

function addSocketIdToSocketMap(id, soc){
    if(id == null || soc == null) return;
    socketIdToSocketMap.set(id, soc);
    var t = setInterval(function(){
        removeSocketIdToSocketMap(id);
        clearInterval(t);
    }, 24 * 60 * 60 * 1000);
}

function removeSocketIdToSocketMap(id){
    if(id == null) return;
    socketIdToSocketMap.remove(id);
}

function getSocketFromSocketId(id){
    if(id == null) return null;
    return socketIdToSocketMap.get(id);
}

module.exports.udpAddressToSocketMap = udpAddressToSocketMap;
module.exports.socketToUdpAddressMap = socketToUdpAddressMap;
module.exports.socketToMatchMap = socketToMatchMap;
module.exports.socketToUsernameMap = socketToUsernameMap;
module.exports.usernameToSocketMap = usernameToSocketMap;
module.exports.verificationCodeToUserMap = verificationCodeToUserMap;
module.exports.socketIdToSocketMap = socketIdToSocketMap;

module.exports.addSocketToUsernameMap = addSocketToUsernameMap;
module.exports.addVerificationCodeToUserMap = addVerificationCodeToUserMap;
module.exports.addSocketToMatchMap = addSocketToMatchMap;
module.exports.addUdpAddressToSocketMap = addUdpAddressToSocketMap;
module.exports.addSocketIdToSocketMap = addSocketIdToSocketMap;

module.exports.removeVerificationCodeToUserMap = removeVerificationCodeToUserMap;
module.exports.removeSocketIdToSocketMap = removeSocketIdToSocketMap;
module.exports.removeSocketToUsernameMap = removeSocketToUsernameMap;
module.exports.removeSocketToMatchMap = removeSocketToMatchMap;
module.exports.removeUdpAddressToSocketMap = removeUdpAddressToSocketMap;

module.exports.replaceSocketToUsernameMap = replaceSocketToUsernameMap;
module.exports.replaceSocketToMatchMap = replaceSocketToMatchMap;

module.exports.getSocketFromUdpAddress = getSocketFromUdpAddress;
module.exports.getSocketFromUsername = getSocketFromUsername;
module.exports.getSocketFromSocketId = getSocketFromSocketId;
module.exports.getUdpAddressFromSocket = getUdpAddressFromSocket;
module.exports.getUsernameFromSocket = getUsernameFromSocket;
module.exports.getMatchNumberFromSocket = getMatchNumberFromSocket;
module.exports.getUserFromVerificationCode = getUserFromVerificationCode;

module.exports.removeElementFromArray = removeElementFromArray;
module.exports.haveSameMembers = haveSameMembers;
module.exports.distance = distance;
module.exports.getElementsNotInSecondArray = getElementsNotInSecondArray;
module.exports.getDigits = getDigits;
module.exports.checkIfAlreadyInArray = checkIfAlreadyInArray;