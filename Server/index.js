var database = require('./database.js');
var utils = require('./utils.js');
var email = require('./email.js');
var match = require('./match.js');

database.start();
var maxPlayers = match.maxPlayers;

// UDP ---------------------------------------------------------------------------------------------

var dgram = require('dgram');
var udpServer = dgram.createSocket('udp4');

udpServer.on('error', (err) => {
  console.log(`udpServer error:\n${err.stack}`);
  udpServer.close();
});

var UDP_PLAYER_POSITIONS = 0;
var UDP_SELF_POSITIONS = 1;
var UDP_CONNECTED = 2;

udpServer.on('message', (msg, rinfo) => {
    try{
    var msg = JSON.parse(msg.toString());
    if(msg.id != null){
        switch(msg.id){
            case UDP_PLAYER_POSITIONS :
                var socket = utils.getSocketFromUdpAddress(rinfo);
//                console.log(socket);
                try{
                    matches[utils.getMatchNumberFromSocket(socket)].mode.setPositions(socket, msg.x, msg.y, rinfo.address, rinfo.port);
                }catch(err){console.log(err)}
                break;

            case UDP_CONNECTED :
                utils.addUdpAddressToSocketMap(rinfo, utils.getSocketFromUsername(msg.username));
                break;
        }
    }
    }catch(err){console.log(err);}
});

udpServer.on('listening', () => {
  const address = udpServer.address();
});

udpServer.bind(31028);

// Socket.IO ---------------------------------------------------------------------------------------
var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);

io.set('heartbeat timeout', 15000);
io.set('heartbeat interval', 7000);
//io.set('heartbeat timeout', 1000);
//io.set('heartbeat interval', 500);

server.listen(31028);

var matches = new Array();
var numberOfMatches = 0;
var queue = new Array();

io.on('connection', function(socket){
    try{
    utils.addSocketIdToSocketMap(socket.id, socket);
    }catch(err){console.log(err);}
    socket.on('findMatch', function(){
        try{
            var matchNum = utils.getMatchNumberFromSocket(socket);
            if(matchNum != null) return;
            addToQueue(utils.getUsernameFromSocket(socket));
        }catch(err){console.log(err);}
    }).on('verifyAccount', function(code, callback){
        try{
        callback();
        var user = utils.getUserFromVerificationCode(code);
        if(user != null){
            database.createUser(user, socket);
//            createUser(user, socket);
            utils.removeVerificationCodeToUserMap(code);
        }else{
            emitGlobal('verifyCodeInvalid', {}, socket);
        }
        }catch(err){console.log(err);}
    }).on('stopFindingMatch', function(callback){
        callback();
        try{
        removeFromQueue(utils.getUsernameFromSocket(socket));
        }catch(err){console.log(err);}
    }).on('registerAttempt', function(username, password, email){
        try{
        database.register(socket, username, password, email);
//        dbRegister(socket, username, password, email);
        }catch(err){console.log(err);}
    }).on('loginAttempt', function(username, password){
        try{
        if(utils.getUsernameFromSocket(socket) == null){
            database.login(socket, username, password);
//            dbLogin(socket, username, password);
        }
        }catch(err){console.log(err);}
    }).on('disconnect', function(){
        try{
        var matchNum = utils.getMatchNumberFromSocket(socket);
        if(matchNum != null){
            matches[matchNum].playerDisconnected(socket);
            console.log("Player disconnected.");
            utils.removeSocketToMatchMap(socket);
        }else{
            removeFromQueue(utils.getUsernameFromSocket(socket));
        }
        var disconnectSocketIdToSocketMapTimer = setInterval(function(){
            try{
            utils.removeSocketIdToSocketMap(socket.id);
            clearInterval(disconnectSocketIdToSocketMapTimer);
            }catch(err){console.log(err);}
        }, 10 * 60 * 1000);
        }catch(err){console.log(err);}
	}).on('reconnected', function(socId, callback){
        callback();
        try{
        emitGlobal('socketId', {socketId : socket.id}, socket);
        var tempSoc = utils.getSocketFromSocketId(socId);
        if(tempSoc == null){
//            emitGlobal('logout', {}, socket);
            return;
        }
        utils.replaceSocketToUsernameMap(tempSoc, socket);
        utils.replaceSocketToMatchMap(tempSoc, socket);
        var matchNum = utils.getMatchNumberFromSocket(socket);
        if(matchNum == null || matches[matchNum] == null) return;
        if(matches[matchNum] != null){
            matches[matchNum].replacePlayerSocket(tempSoc, socket);
        }
        }catch(err){console.log(err);}
	}).on('logout', function(callback){
        callback();
        try{
        if(utils.getUsernameFromSocket(socket) == null) return;
        utils.removeSocketToUsernameMap(socket);
        emitGlobal('logout', {}, socket);
        }catch(err){console.log(err);}
    });

	socket.on('paintingReceivedAllPaintings', function(callback){
      	callback();
      	try{
      	    matches[utils.getMatchNumberFromSocket(socket)].mode.addPaintingsReceived(socket);
      	}catch(err){console.log(err);}
    }).on('paintingReceivedAllVotes', function(callback){
      	callback();
        try{
      	matches[utils.getMatchNumberFromSocket(socket)].mode.addVotesReceived(socket);
      	}catch(err){console.log(err);}
    }).on('paintingSendingPainting', function(nodes, ratio, callback){
        callback();
        try{
        matches[utils.getMatchNumberFromSocket(socket)].mode.sendPainting(nodes, ratio, socket);
        }catch(err){console.log(err);}
	}).on('paintingSendingVotes', function(votes, callback){
	    callback();
	    try{
	    matches[utils.getMatchNumberFromSocket(socket)].mode.sendVotes(votes, socket);
	    }catch(err){console.log(err);}
	});

	socket.on('mazeReceiveMaze', function(array1D, callback){
	    callback();
	    try{
        matches[utils.getMatchNumberFromSocket(socket)].mode.receiveMaze(socket, array1D);
        }catch(err){console.log(err);}
	}).on('mazeMazeReceived', function(callback){
	    callback();
	    try{
        matches[utils.getMatchNumberFromSocket(socket)].mode.mazeReceivedFromClient(socket);
        }catch(err){console.log(err);}
	}).on('mazeTrophyReached', function(callback){
	    callback();
	    try{
        matches[utils.getMatchNumberFromSocket(socket)].mode.trophyReached(socket);
        }catch(err){console.log(err);}
	});

	socket.on('hideAndSeekPlayerCaught', function(username, callback){
	    callback();
	    try{
            matches[utils.getMatchNumberFromSocket(socket)].mode.playerCaught(socket, username);
	    }catch(err){console.log(err);}
	});

	socket.on('conquerSendTroops', function(src, troops, dst, callback){
	    callback();
	    try{
            matches[utils.getMatchNumberFromSocket(socket)].mode.sendTroops(socket, src, troops, dst);
	    }catch(err){console.log(err);}
	});
});

function emitGlobal(event, data, socket){
    var rec = false;
    socket.emit(event, data, function(){
         rec = true;
    });
    var timerGE = setInterval(function(){
        if(rec){
            clearInterval(timerGE);
            return;
        }
        socket.emit(event, data, function(){
            rec = true;
            clearInterval(timerGE);
        });
    }, 3000);
}

function matchMaking(){
    if(queue.length >= match.maxPlayers){
        console.log(queue);
        matches[numberOfMatches] = new match.Match(numberOfMatches);
        var tempUsernames = new Array();
        for(var i = 0; i < match.maxPlayers; i++){
            var tempUsername = queue.shift();
            if(utils.checkIfAlreadyInArray(tempUsername, tempUsernames)){
                for(var j = 0; j < tempUsernames.length; j++){
                    queue.unshift(tempUsernames[j]);
                }
                matches[numberOfMatches] = null;
                return;
            }
            var soc = utils.getSocketFromUsername(tempUsername);
            if(soc == null){
                for(var j = 0; j < tempUsernames.length; j++){
                    queue.unshift(tempUsernames[j]);
                }
                matches[numberOfMatches] = null;
                return;
            }
            matches[numberOfMatches].addPlayer(soc);
            utils.addSocketToMatchMap(soc, numberOfMatches);
            tempUsernames.push(tempUsername);
        }
        console.log("New match made : " + numberOfMatches);
//        console.log(matches[numberOfMatches]);
        matches[numberOfMatches].matchFound();
        if(numberOfMatches == 32000) numberOfMatches = 0;
        numberOfMatches++;
    }
}

function addToQueue(username){
    if(username == null) return;
    queue.push(username);
    var queueTimeout = setInterval(function(){
        removeFromQueue(username);
        clearInterval(queueTimeout);
    }, 10 * 60 * 1000);
    matchMaking();
}

function removeFromQueue(username){
    var index = queue.indexOf(username);
    if (index > -1) {
        queue.splice(index, 1);
    }
}

module.exports.UDP_PLAYER_POSITIONS = UDP_PLAYER_POSITIONS;
module.exports.UDP_SELF_POSITIONS = UDP_SELF_POSITIONS;
module.exports.matches = matches;
module.exports.udpServer = udpServer;
module.exports.emitGlobal = emitGlobal;