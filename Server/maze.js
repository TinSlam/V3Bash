var utils = require('./utils.js');
var server = require('./index.js');
var match = require('./match.js');

var MAZE_WIDTH = 65;
var MAZE_HEIGHT = 65;
var MAZE_TILE_HEIGHT = 64;
var MAZE_TILE_WIDTH = 64;
var MAZE_SPACE = 0;
var MAZE_WALL = 1;
var MAZE_GOBLET = 2;
var MAZE_TIME = 2 * 60 * 1000;
var MAZE_TROPHY_POINTS = 5;

class MazeMatch{
    constructor(matchVar){
        this.match = matchVar;
    }
    startState(){
        this.positions = new Array(match.maxPlayers);
        for(var i = 0; i < match.maxPlayers; i++){
            this.positions[i] = new Array(2);
            this.positions[i][0] = 0;
            this.positions[i][1] = 0;
        }
        this.mazeReceived = new Array();
        this.state = "mazeStartState";
        this.points = new Array();
        for(var i = 0; i < match.maxPlayers; i++){
            this.points[i] = 0;
        }

        for(var i = 0; i < this.match.players.length; i++){
            this.match.emitWithAck("mazeInstantiateState", {}, i, this.match.startStateCallback);
        }

        var self = this;
        var startStateTimeout = setInterval(function(){
            clearInterval(startStateTimeout);
            if(self.state != "mazeStartState") return;
            self.startMode();
        }, 15 * 1000);
        this.match.timers.push(startStateTimeout);
    }
    startMode(){
        this.state = "mazeWaitingForMaze";
        this.match.state = this.state;
        this.requestMaze();
    }
    requestMaze(){
        var i = Math.floor(Math.random() * this.match.players.length);
        if(this.mazeSender != null){
            while(this.mazeSender == this.match.players[i]){
                i = Math.floor(Math.random() * this.match.players.length);
            }
            this.mazeSender = this.match.players[i];
        }else{
            i = Math.floor(Math.random() * this.match.players.length);
            this.mazeSender = this.match.players[i];
        }
        this.match.emitWithAck('mazeRequestMaze', {}, i);
        var self = this;
        this.changeMazeSenderTimeout = setInterval(function(){
           clearInterval(self.changeMazeSenderTimeout);
            if(self.state != "mazeWaitingForMaze") return;
            self.requestMaze();
        }, 15 * 1000);
        this.match.timers.push(this.changeMazeSenderTimeout);
    }
    receiveMaze(socket, array1D){
        if(this.state != "mazeWaitingForMaze") return;
        if(socket != this.mazeSender) return;
        this.state = "mazeSendingMaze";
        this.mazeReceived.push(socket);
        for(var i = 0; i < this.match.players.length; i++){
            if(this.match.players[i] == socket) continue;
            this.match.emitWithAck('mazeReceiveMaze', {array1D : array1D}, i);
        }
        var self = this;
        this.mazeSendTimeout = setInterval(function(){
            clearInterval(self.mazeSendTimeout);
            if(self.state != "mazeSendingMaze") return;
            self.state = "mazeMatchStarted";
            self.startMatch();
        }, 15 * 1000);
        this.match.timers.push(this.mazeSendTimeout);
        this.addTiles(array1D);
    }
    mazeReceivedFromClient(socket){
        if(this.state != "mazeSendingMaze") return;
        if(utils.checkIfAlreadyInArray(socket, this.mazeReceived)) return;
        this.mazeReceived.push(socket);

        if(this.mazeReceived.length >= this.match.players.length){
            clearInterval(this.mazeSendTimeout);
            this.state = "mazeMatchStarted";
            this.startMatch();
        }
    }
    startMatch(){
        for(var i = 0; i < this.match.players.length; i++){
            this.match.emitWithAck('mazeStartMatch', {}, i);
        }
        var self = this;
        var mazeMatchTimer = setInterval(function(){
            clearInterval(mazeMatchTimer);
            if(self.state != "mazeMatchStarted") return;
            self.state = 'mazeMatchEnd';
            self.calcPoints();
            self.endMode();
        }, MAZE_TIME);
        this.match.timers.push(mazeMatchTimer);
    }
    addTiles(array1D){
        var counter = 0;
        var tens = 0;
        this.tiles = new Array(MAZE_WIDTH);
        for(var i = 0; i < MAZE_WIDTH; i++){
            this.tiles[i] = new Array(MAZE_HEIGHT);
            for(var j = 0; j < MAZE_HEIGHT; j++){
                this.tiles[i][j] = (Math.floor(array1D[counter] / Math.pow(10, tens))) % 10;
                tens++;
                if(tens == 10){
                    tens = 0;
                    counter++;
                }
            }
        }
    }
//    tcpSendPositions(x, y, socket){
////        if(this.state != "hideAndSeekMatchStarted" && this.state != "hideAndSeekStartState") return;
//        var index = this.match.getPlayerIndex(getUsernameFromSocket(socket));
////        if(this.isDead[index]) return;
//        var x0 = this.positions[index][0];
//        var y0 = this.positions[index][1];
//        if(this.isSolid(x, y)) return;
//        if(distance(x, y, x0, y0) <= 128){
//            this.positions[index][0] = x;
//            this.positions[index][1] = y;
//            this.sendTcpPositions(socket, x, y);
//        }else{
//            this.match.emitWithAck('tcpSendSelfPositions', {x : x0, y : y0}, index);
////            var json = JSON.stringify({id : UDP_SELF_POSITIONS, x : x0, y : y0});
////            udpServer.send(json, 0, json.length, port, ip);
//        }
//    }
//    sendTcpPositions(socket, x, y){
//        for(var i = 0; i < this.match.players.length; i++){
//            if(this.match.players[i] == socket) continue;
//            this.match.emitWithAck('tcpSendPositions', {username : getUsernameFromSocket(socket), x : x, y : y}, i);
////            var json = JSON.stringify({id : UDP_PLAYER_POSITIONS, username : getUsernameFromSocket(socket), x : x, y : y});
////            var rinfo = getUdpAddressFromSocket(this.match.players[i]);
////            if(rinfo == null) continue;
////            udpServer.send(json, 0, json.length, rinfo.port, rinfo.address);
//        }
//    }
    setPositions(socket, x, y, ip, port){
        if(this.state != "mazeMatchStarted") return;
        var index = this.match.getPlayerIndex(utils.getUsernameFromSocket(socket));
        var x0 = this.positions[index][0];
        var y0 = this.positions[index][1];
        if(this.isSolid(x, y)) return;
        if(utils.distance(x, y, x0, y0) <= 128){
            this.positions[index][0] = x;
            this.positions[index][1] = y;
            this.sendPositions(socket, x, y);
        }else{
            json = JSON.stringify({id : server.UDP_SELF_POSITIONS, x : x, y : y});
            server.udpServer.send(json, 0, json.length, port, ip);
        }
    }
    sendPositions(socket, x, y){
        for(var i = 0; i < this.match.players.length; i++){
            if(this.match.players[i] == socket) continue;
            var json = JSON.stringify({id : server.UDP_PLAYER_POSITIONS, username : utils.getUsernameFromSocket(socket), x : x, y : y});
            var rinfo = utils.getUdpAddressFromSocket(this.match.players[i]);
            server.udpServer.send(json, 0, json.length, rinfo.port, rinfo.address);
        }
    }
    trophyReached(socket){
        var i = this.match.getPlayerIndex(utils.getUsernameFromSocket(socket));
        var x = Math.floor(this.positions[i][0] / MAZE_TILE_WIDTH);
        var y = Math.floor(this.positions[i][1] / MAZE_TILE_HEIGHT);
        if(this.tiles[x][y] == MAZE_GOBLET){
            this.tiles[x][y] = MAZE_SPACE;
            var username = utils.getUsernameFromSocket(socket);
            this.points[this.match.getPlayerIndex(username)] += MAZE_TROPHY_POINTS;
            this.assignNewTrophy(x, y, username);
        }
    }
    assignNewTrophy(username){
        var x = Math.floor(Math.random() * MAZE_WIDTH), y = Math.floor(Math.random() * MAZE_HEIGHT);
        while(this.tiles[x][y] != MAZE_SPACE){
            x = Math.floor(Math.random() * MAZE_WIDTH);
            y = Math.floor(Math.random() * MAZE_HEIGHT);
        }
        this.tiles[x][y] = MAZE_GOBLET;
        for(var k = 0; k < this.match.players.length; k++){
            this.match.emitWithAck('mazeTrophyReached', {x : x, y : y, username : username}, k);
        }
    }
    isSolid(x, y){
        var i = Math.floor(x / MAZE_TILE_WIDTH);
        var j = Math.floor(y / MAZE_TILE_HEIGHT);
        if(i < 0 || j < 0 || i >= MAZE_WIDTH || j >= MAZE_HEIGHT) return true;
        if(this.tiles[i][j] == MAZE_WALL) return true;

        return false;
    }
    calcPoints(){
        for(var i = 0; i < this.match.players.length; i++){
            this.match.emitWithAck('mazeSendFinalPoints', {points : this.points}, i);
        }
    }
    endMode(){
        for(var i = 0; i < match.maxPlayers; i++){
            var socket = this.match.players[i];
            if(socket == null) continue;
            utils.removeUdpAddressToSocketMap(utils.getUdpAddressFromSocket(socket));
        }
        this.match.endMode();
    }
}

module.exports.MazeMatch = MazeMatch;