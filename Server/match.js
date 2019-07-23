var painting = require('./painting.js');
var maze = require('./maze.js');
var hideAndSeek = require('./hideAndSeek.js');
var conquer = require('./conquer.js');

var utils = require('./utils.js');
var server = require('./index.js');

var maxPlayers = 2;
var maxModes = 4;

var MODE_PAINTING = 0;
var MODE_MAZE = 1;
var MODE_HIDE_AND_SEEK = 2;
var MODE_CONQUER = 3;

var modes = new Array();
modes.push(MODE_PAINTING);
modes.push(MODE_MAZE);
modes.push(MODE_HIDE_AND_SEEK);
modes.push(MODE_CONQUER);

function convertByteToMode(byte, match){
    switch(byte){
        case MODE_PAINTING :
            return new painting.PaintingMatch(match);

        case MODE_MAZE :
            return new maze.MazeMatch(match);

        case MODE_HIDE_AND_SEEK :
            return new hideAndSeek.HideAndSeekMatch(match);

        case MODE_CONQUER :
            return new conquer.ConquerMatch(match);
    }
}

class Match{
    constructor(matchNumber){
        try{
        this.matchNumber = matchNumber;
        this.players = new Array();
        this.reconnectionDataSender = new Array(maxPlayers);
        this.usernamesCallbackArray = new Array();
        this.matchFoundCallbackArray = new Array();
        this.stateCallbackArray = new Array();
        this.modes = new Array();
        this.modeIndex = new Array();
        this.points = new Array();
        this.timers = new Array();
        for(var i = 0; i < maxPlayers; i++){
            this.points[i] = 0;
        }
        this.state = "start";
        var self = this;
        this.allReadyTimer = setInterval(function(){
            try{
            clearInterval(self.allReadyTimer);
            for(var i = 0; i < self.matchFoundCallbackArray.length; i++){
                queue.unshift(utils.getUsernameFromSocket(self.matchFoundCallbackArray[i]));
            }
            self.endGame();
            }catch(err){console.log(err);}
        }, 10 * 1000);
        this.timers.push(this.allReadyTimer);
        }catch(err){console.log(err);}
    }
    matchFound(){
        try{
        for(var i = 0; i < this.players.length; i++){
            this.emitWithAck('matchFound', {}, i, this.playerReady);
        }
        }catch(err){console.log(err);}
    }
    playerReady(socket, self){
        try{
        if(utils.checkIfAlreadyInArray(socket, self.matchFoundCallbackArray)) return;
        self.matchFoundCallbackArray.push(socket);
        if(utils.haveSameMembers(self.matchFoundCallbackArray, self.players)){
            clearInterval(self.allReadyTimer);
            self.generateModes(maxModes);
            self.startGame();
        }
        }catch(err){console.log(err);}
    }
    startGame(){
        try{
        this.startGameTimer();
        this.distributeUsernames();
        }catch(err){console.log(err);}
    }
    startGameTimer(){
        try{
        var self = this;
        this.gameTimer = setInterval(function(){
            try{
            clearInterval(self.gameTimer);
            self.endGame();
            }catch(err){console.log(err);}
        }, 30 * 60 * 1000);
        this.timers.push(this.gameTimer);
        }catch(err){console.log(err);} // Continue from here for try catches.
    }
    distributeUsernames(){
        var usernames = new Array();
        var self = this;
        for(var i = 0; i < this.players.length; i++){
            usernames[i] = utils.getUsernameFromSocket(this.players[i]);
            this.reconnectionDataSender[i] = new Array();
            this.reconnectionDataSender[i].push(usernames[i]);
        }
        for(var i = 0; i < this.players.length; i++){
            this.emitWithAck('sendUsernames', {usernames : usernames, modes : this.modeIndex}, i, this.usernamesDistributed);
        }
        var distributeUsernamesTimeout = setInterval(function(){
            clearInterval(distributeUsernamesTimeout);
            if(self.state != "start") return;
            var disconnectedUsersSocket = utils.getElementsNotInSecondArray(self.players, self.usernamesCallbackArray);
            while(disconnectedUsersSocket.length > 0){
                var player = disconnectedUsersSocket.pop();
                self.addReconnectionCommand(getUsernameFromSocket(player), "reconnectionSendUsername", {usernames : usernames, modes : self.modeIndex}, self);
            }
            self.mode = self.modes.pop();
            self.state = "startState";
            self.mode.startState();
        }, 15 * 1000);
        this.timers.push(distributeUsernamesTimeout);
    }
    usernamesDistributed(socket, self){
        if(self.state != "start") return;
        if(utils.checkIfAlreadyInArray(socket, self.usernamesCallbackArray)) return;
        self.usernamesCallbackArray.push(socket);
        if(utils.haveSameMembers(self.usernamesCallbackArray, self.players)){
            self.mode = self.modes.pop();
            self.state = "startState";
            self.mode.startState();
        }
    }
    calcPoints(){
        for(var i = 0; i < this.players.length; i++){
            this.emitWithAck('sendFinalPoints', {points : this.points}, i);
        }
    }
    addPoints(p){
        for(var i = 0; i < maxPlayers; i++){
            this.points[i] += p[i];
        }
    }
    endMode(){
        this.stateCallbackArray = new Array();
        this.addPoints(this.mode.points);
        if(this.modes.length != 0){
            var self = this;
            var betweenModesTimer = setInterval(function(){
                clearInterval(betweenModesTimer);
                self.mode = self.modes.pop();
                self.state = "startState";
                self.mode.startState();
            }, 10 * 1000);
            this.timers.push(betweenModesTimer);
        }else{
            this.calcPoints();
            this.endGame();
        }
    }
    clearTimers(){
        while(this.timers.length != 0){
            clearInterval(this.timers.pop());
        }
    }
    endGame(){
        this.clearTimers();
        for(var i = 0; i < this.players.length; i++){
            utils.removeSocketToMatchMap(this.players[i]);
        }
        clearInterval(this.gameTimer);
        server.matches[this.matchNumber] = null;
        console.log("Done !");
    }
    emitWithAck(event, data, index, callbackFunction){
        var rec = false;
        var self = this;
        var args = new Array();
        var timerEmit = setInterval(function(){
            if(rec == false){
                self.players[index].emit(event, data, function(){
                    clearInterval(timerEmit);
                    rec = true;
                    if(callbackFunction != null) callbackFunction(self.players[index], self);
                });
            }
        }, 3000);
        this.timers.push(timerEmit);
        self.players[index].emit(event, data, function(){
            clearInterval(timerEmit);
            rec = true;
            if(callbackFunction != null) callbackFunction(self.players[index], self);
        });
        var emitTimerTimeout = setInterval(function(){
            clearInterval(timerEmit);
            clearInterval(emitTimerTimeout);
        }, 20 * 1000);
        this.timers.push(emitTimerTimeout);
    }
    generateModes(num){
        for(var i = 0; i < maxModes; i++){
            do{
                var modeIndexTemp = Math.floor(Math.random() * modes.length);
            }while(utils.checkIfAlreadyInArray(modeIndexTemp, this.modeIndex));
            this.modeIndex.push(modeIndexTemp);
            this.modes.push(convertByteToMode(modeIndexTemp, this));
        }
//    this.modeIndex.push(MODE_CONQUER);
//    this.modes.push(convertByteToMode(MODE_CONQUER, this));
    }
    addReconnectionCommand(username, event, data, self){
        for(var i = 0; i < maxPlayers; i++){
            if(self.reconnectionDataSender[i][0] == username){
                var command = new Array();
                command.push(event, data);
                self.reconnectionDataSender[i].push(command);
                return;
            }
        }
    }
    startStateCallback(socket, self){
        if(self.state != "startState") return;
        if(utils.checkIfAlreadyInArray(socket, self.stateCallbackArray)) return;
        self.stateCallbackArray.push(socket);

        if(self.stateCallbackArray.length >= self.players.length){
            if(self.state != "startState") return;
            self.mode.startMode();
        }
    }
    addPlayer(val){
        this.players.push(val);
    }
    getPlayers(){
        return this.players;
    }
    playerDisconnected(val){
        try{
            this.mode.playerDisconnected(val);
        }catch(err){}
        for(var i = 0; i < this.players.length; i++){
            if(val == this.players[i]){
                this.players.splice(i, 1);
                i--;
            }else{
                this.emitWithAck('playerDisconnected', {}, i);
            }
        }
        if(this.players.length <= 1){
            this.calcPoints();
            this.endGame();
        }
    }
    replacePlayerSocket(s1, s2){
        for(var i = 0; i < this.players.length; i++){
            if(this.players[i] == s1){
                this.players[i] = s2;
                return;
            }
        }
        server.emitGlobal('logout', {}, s2);
    }
    getPlayerIndex(val){
        for(var i = 0; i < maxPlayers; i++){
            if(this.players[i] == null) continue;
            if(utils.getUsernameFromSocket(this.players[i]) == val) return i;
        }
        return -1;
    }
}

module.exports.convertByteToMode = convertByteToMode;
module.exports.Match = Match;
module.exports.maxPlayers = maxPlayers;