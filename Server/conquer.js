var utils = require('./utils.js');
var match = require('./match.js');

var CONQUER_POINTS_WIN = 7;
var CONQUER_POINTS_DRAW = 3;
var CONQUER_MAX_ROUNDS = 3;
var CONQUER_TOWER_SIZE_BIG = 0;
var CONQUER_TOWER_SIZE_SMALL = 1;
var CONQUER_TROOP_REGEN_TIME = 2000;
var CONQUER_TROOPS_REGEN_BIG = 2;
var CONQUER_TROOPS_REGEN_SMALL = 1;
var CONQUER_TOWER_TROOPS_BIG = 20;
var CONQUER_TOWER_TROOPS_SMALL = 10;
var CONQUER_MATCH_TIME = 60 * 1000;

class ConquerMatch{
    constructor(matchVar){
        this.match = matchVar;
        this.rounds = 0;
    }
    startState(){
        this.state = "conquerStartState";
        this.points = new Array();
        for(var i = 0; i < match.maxPlayers; i++){
            this.points[i] = 0;
        }

        this.matchesEnded = 0;

        for(var i = 0; i < this.match.players.length; i++){
            this.match.emitWithAck("conquerInstantiateState", {}, i, this.match.startStateCallback);
        }
        this.matches = new Array();
        var self = this;
        var startStateTimeout = setInterval(function(){
            clearInterval(startStateTimeout);
            if(self.state != "conquerStartState") return;
            self.startMode();
        }, 15 * 1000);
        this.match.timers.push(startStateTimeout);
    }
    startMode(){
        this.state = "conquerAssigningMatches";
        this.match.state = this.state;
        this.assignMatches();
    }
    assignMatches(){
        this.matches = new Array();
        for(var i = 0; i < this.match.players.length; i += 2){
            var miniMatch = new ConquerMiniMatch(this, utils.getUsernameFromSocket(this.match.players[i]), utils.getUsernameFromSocket(this.match.players[i + 1]));
            miniMatch.startMatch();
            this.matches.push(miniMatch);
        }
        if(this.match.length % 2 != 0){
            // Handle this shite.
        }
    }
    sendTroops(socket, src, troops, dst){
        var username = utils.getUsernameFromSocket(socket);
        for(var i = 0; i < this.matches.length; i++){
            if(this.matches[i].hasPlayer(username)){
                this.matches[i].sendTroops(username, src, troops, dst);
                return;
            }
        }
    }
    miniMatchEnded(winner, p1, p2){
        this.matchesEnded++;
        if(winner != ""){
            var index = this.match.getPlayerIndex(winner);
            this.points[index] += CONQUER_POINTS_WIN;
        }else{
            var index1 = this.match.getPlayerIndex(p1);
            var index2 = this.match.getPlayerIndex(p2);
            this.points[index1] += CONQUER_POINTS_DRAW;
            this.points[index2] += CONQUER_POINTS_DRAW;
        }
        if(this.matchesEnded >= this.matches.length){
            this.rounds++;
            this.matches = new Array();
            if(this.rounds == CONQUER_MAX_ROUNDS){
                this.state = "conquerMatchEnd";
                this.calcPoints();
                this.endMode();
            }else{
                var self = this;
                this.sendRoundsEnded();
                var conquerMatchEndTime = setInterval(function(){
                    clearInterval(conquerMatchEndTime);
                    self.matchesEnded = 0;
                    self.assignMatches();
                }, 5000);
                this.match.timers.push(conquerMatchEndTime);
            }
        }
    }
    sendRoundsEnded(){
        for(var i = 0; i < this.match.players.length; i++){
            this.match.emitWithAck('conquerRoundEnded', {points : this.points}, i);
        }
    }
    calcPoints(){
        for(var i = 0; i < this.match.players.length; i++){
            this.match.emitWithAck('conquerSendFinalPoints', {points : this.points}, i);
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

class ConquerMiniMatch{
    constructor(conquerMatchVar, p1, p2){
        this.conquerMatch = conquerMatchVar;
        this.players = new Array();
        this.players.push(p1);
        this.players.push(p2);
        this.towers = new Array();
        var p1Index = this.conquerMatch.match.getPlayerIndex(p1);
        var p2Index = this.conquerMatch.match.getPlayerIndex(p2);
        this.towers.push(new ConquerTower(0, CONQUER_TOWER_SIZE_BIG, p1Index));
        this.towers.push(new ConquerTower(1, CONQUER_TOWER_SIZE_SMALL, -1));
        this.towers.push(new ConquerTower(2, CONQUER_TOWER_SIZE_BIG, p2Index));
        this.towers.push(new ConquerTower(3, CONQUER_TOWER_SIZE_SMALL, -1));
        this.towers.push(new ConquerTower(4, CONQUER_TOWER_SIZE_SMALL, -1));
        this.towers.push(new ConquerTower(5, CONQUER_TOWER_SIZE_SMALL, -1));
        this.towers.push(new ConquerTower(6, CONQUER_TOWER_SIZE_SMALL, p2Index));
        this.towers.push(new ConquerTower(7, CONQUER_TOWER_SIZE_SMALL, p1Index));
        this.towers.push(new ConquerTower(8, CONQUER_TOWER_SIZE_SMALL, p2Index));
        this.towers.push(new ConquerTower(9, CONQUER_TOWER_SIZE_SMALL, p1Index));
        this.towers.push(new ConquerTower(10, CONQUER_TOWER_SIZE_BIG, -1));
        this.towers.push(new ConquerTower(11, CONQUER_TOWER_SIZE_BIG, -1));
        this.state = "minimatchStarted";
    }
    hasPlayer(username){
        if(utils.checkIfAlreadyInArray(username, this.players)) return true;
        return false;
    }
    startMatch(){
        var self = this;
        var conquerMinimatchTimer = setInterval(function(){
            clearInterval(conquerMinimatchTimer);
            if(self.state != "minimatchEnded") return;
            self.state = "minimatchEnded";
            self.endMatch();
        }, CONQUER_MATCH_TIME);
        var troopDraftingTimer = setInterval(function(){
            if(self.state != "minimatchStarted"){
                clearInterval(troopDraftingTimer);
                return;
            }
            var troopsArray = new Array();
            for(var i = 0; i < self.towers.length; i++){
                var tw = self.towers[i];
                if(tw.getTeam() != -1){
                    if(tw.getSize() == CONQUER_TOWER_SIZE_BIG){
                        tw.addTroops(CONQUER_TROOPS_REGEN_BIG);
                    }else{
                        tw.addTroops(CONQUER_TROOPS_REGEN_SMALL);
                    }
                    if(tw.getTroops() >= 100) tw.setTroops(100);
                }
                troopsArray[i] = tw.getTroops();
            }
            self.conquerMatch.match.emitWithAck('conquerUpdateTroops', {array : troopsArray}, self.conquerMatch.match.getPlayerIndex(self.players[1]));
            self.conquerMatch.match.emitWithAck('conquerUpdateTroops', {array : troopsArray}, self.conquerMatch.match.getPlayerIndex(self.players[0]));
        }, CONQUER_TROOP_REGEN_TIME);
        this.conquerMatch.match.timers.push(troopDraftingTimer);
        this.conquerMatch.match.timers.push(conquerMinimatchTimer);
        this.conquerMatch.match.emitWithAck('conquerStartMatch', {username : this.players[0], player2 : true}, this.conquerMatch.match.getPlayerIndex(this.players[1]));
        this.conquerMatch.match.emitWithAck('conquerStartMatch', {username : this.players[1], player2 : false}, this.conquerMatch.match.getPlayerIndex(this.players[0]));
    }
    sendTroops(username, src, troops, dst){
        if(this.state != "minimatchStarted") return;
        var index = this.conquerMatch.match.getPlayerIndex(username);
        var srcTw = this.towers[src];
        if(index != srcTw.getTeam()) return;
        if(srcTw.getTroops < troops) return;
        srcTw.addTroops(-troops);
        var self = this;
        var time = this.getTravelTime(src, dst);
        var troopSender = setInterval(function(){
            clearInterval(troopSender);
            if(self.state != "minimatchStarted") return;
            self.troopsArrived(srcTw, dst, troops);
        }, time);
        this.conquerMatch.match.timers.push(troopSender);
        this.conquerMatch.match.emitWithAck('conquerSendTroops', {time : time, username : username, src : src, dst : dst, troops : troops}, this.conquerMatch.match.getPlayerIndex(this.players[1]));
        this.conquerMatch.match.emitWithAck('conquerSendTroops', {time : time, username : username, src : src, dst : dst, troops : troops}, this.conquerMatch.match.getPlayerIndex(this.players[0]));
    }
    troopsArrived(srcTw, dst, troops){
        var dstTw = this.towers[dst];
        if(srcTw.getTeam() == dstTw.getTeam()){
            dstTw.addTroops(troops);
        }else{
            if(dstTw.getTroops() < troops){
                dstTw.setTeam(srcTw.getTeam());
                dstTw.setTroops(troops - dstTw.getTroops());
            }else{
                dstTw.addTroops(-troops);
            }
        }
        var username = "";
        if(dstTw.getTeam() == this.conquerMatch.match.getPlayerIndex(this.players[0])){
            this.conquerMatch.match.emitWithAck('conquerTroopsArrived', {username : this.players[0], dst : dst, dstTroops : dstTw.getTroops()}, this.conquerMatch.match.getPlayerIndex(this.players[1]));
            this.conquerMatch.match.emitWithAck('conquerTroopsArrived', {username : this.players[0], dst : dst, dstTroops : dstTw.getTroops()}, this.conquerMatch.match.getPlayerIndex(this.players[0]));
        }else if(dstTw.getTeam() != -1){
             this.conquerMatch.match.emitWithAck('conquerTroopsArrived', {username : this.players[1], dst : dst, dstTroops : dstTw.getTroops()}, this.conquerMatch.match.getPlayerIndex(this.players[1]));
             this.conquerMatch.match.emitWithAck('conquerTroopsArrived', {username : this.players[1], dst : dst, dstTroops : dstTw.getTroops()}, this.conquerMatch.match.getPlayerIndex(this.players[0]));
        }else{
             this.conquerMatch.match.emitWithAck('conquerTroopsArrived', {username : "n", dst : dst, dstTroops : dstTw.getTroops()}, this.conquerMatch.match.getPlayerIndex(this.players[1]));
             this.conquerMatch.match.emitWithAck('conquerTroopsArrived', {username : "n", dst : dst, dstTroops : dstTw.getTroops()}, this.conquerMatch.match.getPlayerIndex(this.players[0]));
        }
        this.checkIfSomeoneIsOut();
    }
    checkIfSomeoneIsOut(){
        var team1 = 0;
        var team2 = 0;
        for(var i = 0; i < this.towers.length; i++){
            var tw = this.towers[i];
            if(tw.getTeam() == this.conquerMatch.match.getPlayerIndex(this.players[0])){
                team1++;
            }else if(tw.getTeam() != -1){
                team2++;
            }
        }
        if(team1 == 0 || team2 == 0){
            this.endMatch();
        }
    }
    getTravelTime(src, dst){
        switch(src){
            case 0 :
                switch(dst){
                    case 1 :
                    case 7 :
                    case 9 :
                        return 1000;

                    case 5 :
                    case 10 :
                        return 2000;

                    case 11 :
                        return 3500;

                    case 3 :
                    case 4 :
                        return 4000;

                    case 2 :
                    case 6 :
                    case 8 :
                        return 5000;
                }

            case 1 :
                switch(dst){
                    case 0 :
                    case 7 :
                    case 9 :
                    case 10 :
                        return 1000;

                    case 5 :
                        return 1500;

                    case 11 :
                        return 2000;

                    case 3 :
                    case 4 :
                        return 3000;

                    case 2 :
                    case 6 :
                    case 8 :
                        return 4000;
                }

            case 2 :
                switch(dst){
                    case 3 :
                    case 6 :
                    case 8 :
                        return 1000;

                    case 4 :
                    case 11 :
                        return 2000;

                    case 10 :
                        return 3500;

                    case 1 :
                    case 5 :
                        return 4000;

                    case 0 :
                    case 7 :
                    case 9 :
                        return 5000;
                }

            case 3 :
                switch(dst){
                    case 2 :
                    case 6 :
                    case 8 :
                    case 11 :
                        return 1000;

                    case 4 :
                        return 1500;

                    case 10 :
                        return 2000;

                    case 1 :
                    case 5 :
                        return 3000;

                    case 0 :
                    case 7 :
                    case 9 :
                        return 4000;
                }

            case 4 :
                switch(dst){
                    case 6 :
                        return 1300;

                    case 3 :
                        return 1500;

                    case 10 :
                    case 11 :
                        return 2000;

                    case 2 :
                        return 2000;

                    case 8 :
                        return 2500;

                    case 1 :
                    case 9 :
                        return 3000;

                    case 0 :
                    case 7 :
                    case 5 :
                        return 4000;
                }

            case 5 :
                switch(dst){
                    case 7 :
                        return 1300;

                    case 1 :
                        return 1500;

                    case 10 :
                    case 11 :
                        return 2000;

                    case 0 :
                        return 2000;

                    case 9 :
                        return 2500;

                    case 3 :
                    case 8 :
                        return 3000;

                    case 2 :
                    case 4 :
                    case 6 :
                        return 4000;
                }

            case 6 :
                switch(dst){
                    case 2 :
                    case 3 :
                        return 1000;

                    case 4 :
                        return 1300;

                    case 8 :
                        return 1700;

                    case 11 :
                        return 2000;

                    case 10 :
                        return 3000;

                    case 1 :
                        return 3700;

                    case 5 :
                        return 4000;

                    case 9 :
                        return 4000;

                    case 0 :
                    case 7 :
                        return 4500;
                }

            case 7 :
                switch(dst){
                    case 0 :
                    case 1 :
                        return 1000;

                    case 5 :
                        return 1300;

                    case 9 :
                        return 1700;

                    case 10 :
                        return 2000;

                    case 11 :
                        return 3000;

                    case 3 :
                        return 3700;

                    case 4 :
                        return 4000;

                    case 8 :
                        return 4000;

                    case 2 :
                    case 6 :
                        return 4500;
                }

            case 8 :
                switch(dst){
                    case 2 :
                    case 3 :
                        return 1000;

                    case 11 :
                        return 1500;

                    case 6 :
                        return 1700;

                    case 4 :
                        return 2500;

                    case 5 :
                    case 10 :
                        return 3000;

                    case 1 :
                    case 7 :
                        return 4000;

                    case 9 :
                    case 0 :
                        return 4500;
                }

            case 9 :
                switch(dst){
                    case 0 :
                    case 1 :
                        return 1000;

                    case 10 :
                        return 1500;

                    case 7 :
                        return 1700;

                    case 5 :
                        return 2500;

                    case 4 :
                    case 11 :
                        return 3000;

                    case 3 :
                    case 6 :
                        return 4000;

                    case 8 :
                    case 2 :
                        return 4500;
            }

            case 10 :
                switch(dst){
                    case 0 :
                        return 2000;

                    case 1 :
                        return 1000;

                    case 2 :
                        return 3500;

                    case 3 :
                        return 2000;

                    case 4 :
                        return 2000;

                    case 5 :
                        return 2000;

                    case 6 :
                        return 3000;

                    case 7 :
                        return 2000;

                    case 8 :
                        return 3000;

                    case 9 :
                        return 1500;

                    case 11 :
                        return 1600;
                }

            case 11 :
                switch(dst){
                    case 2 :
                        return 2000;

                    case 3 :
                        return 1000;

                    case 0 :
                        return 3500;

                    case 1 :
                        return 2000;

                    case 5 :
                        return 2000;

                    case 4 :
                        return 2000;

                    case 7 :
                        return 3000;

                    case 6 :
                        return 2000;

                    case 9 :
                        return 3000;

                    case 8 :
                        return 1500;

                    case 10 :
                        return 1600;
                }
        }
        return 0;
    }
    calcTroops(){
        var team1 = 0;
        var team2 = 0;
        for(var i = 0; i < this.towers.length; i++){
            var tw = this.towers[i];
            if(tw.getTeam() == this.conquerMatch.match.getPlayerIndex(this.players[0])){
                team1 += tw.getTroops();
            }else if(tw.getTeam() != -1){
                team2 += tw.getTroops();
            }
        }
        this.winner = "";
        if(team1 > team2){
            this.winner = this.players[0];
        }else if(team2 > team1){
            this.winner = this.players[1];
        }
    }
    endMatch(){
        if(this.state != "minimatchStarted") return;
        this.calcTroops();
        this.state = "minimatchEnded";
        this.conquerMatch.miniMatchEnded(this.winner, this.players[0], this.players[1]);
    }
}

class ConquerTower{
    constructor(arg1, arg2, arg3){
        this.index = arg1;
        if(arg2 == CONQUER_TOWER_SIZE_BIG){
            this.troops = CONQUER_TOWER_TROOPS_BIG;
        }else{
            this.troops = CONQUER_TOWER_TROOPS_SMALL;
        }
        this.size = arg2;
        this.team = arg3;
    }
    setTroops(num){
        this.troops = num;
    }
    addTroops(num){
        this.troops += num;
    }
    setTeam(num){
        this.team = num;
    }
    getTeam(){
        return this.team;
    }
    getTroops(){
        return this.troops;
    }
    getSize(){
        return this.size;
    }
}

module.exports.ConquerMatch = ConquerMatch;