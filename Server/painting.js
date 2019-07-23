var match = require('./match.js');
var utils = require('./utils.js');

var subjects = new Array();
var paintingMaxSubjects = 2;
var PAINTING_TIME_TO_DRAW = 45000;
var PAINTING_TIME_TO_VOTE = 10000;
var PAINTING_TIME_TO_SEE_RESULTS = 10000;
//var PAINTING_TIME_TO_DRAW = 5000;
//var PAINTING_TIME_TO_VOTE = 5000;
//var PAINTING_TIME_TO_SEE_RESULTS = 5000;

subjects.push("کتاب");
subjects.push("پیاز");
subjects.push("هویج");
subjects.push("خمیازه");
subjects.push("پری دریایی");
subjects.push("نهنگ");
subjects.push("برج ایفل");
subjects.push("تفنگ");
subjects.push("کولر");
subjects.push("گل");
subjects.push("قورباغه");
subjects.push("جغد");
subjects.push("هندوانه");
subjects.push("آسیاب");
subjects.push("کلاه ایمنی");
subjects.push("کوه آتشفشان");

class PaintingMatch{
    constructor(matchVar){
        this.match = matchVar;
    }
    startState(){
        this.subjects = new Array();
        this.clientSentInfoArray = new Array();
        this.subjectsCallbackArray = new Array();
        this.paintingReceived = new Array();
        this.votesReceived = new Array();
        this.state = "paintingSendingSubjects";
        this.round = 0;
        var self = this;
        this.points = new Array();
        for(var i = 0; i < match.maxPlayers; i++){
            this.points[i] = 0;
        }
        this.drawTimer = setInterval(function(){clearInterval(self.drawTimer)}, 10);
        this.voteTimer = setInterval(function(){clearInterval(self.voteTimer)}, 10);
        this.resultTimer = setInterval(function(){clearInterval(self.resultTimer)}, 10);
        this.match.timers.push(this.drawTimer);
        this.match.timers.push(this.voteTimer);
        this.match.timers.push(this.resultTimer);

        for(var i = 0; i < this.match.players.length; i++){
            this.match.emitWithAck("paintingInstantiateState", {}, i, this.match.startStateCallback);
        }

        var self = this;
        var startStateTimeout = setInterval(function(){
            clearInterval(startStateTimeout);
            if(self.state != "paintingStartState") return;
            self.startMode();
        }, 15 * 1000);
        this.match.timers.push(startStateTimeout);
    }
    startMode(){
        this.state = "paintingSendingSubjects";
        this.match.state = this.state;
        this.distributeSubjects();
    }
    distributeSubjects(){
        var self = this;
        this.subjects = getRandomSubjects(paintingMaxSubjects);
        for(var i = 0; i < this.match.players.length; i++){
            this.match.emitWithAck('paintingReceiveSubjects', {subs : this.subjects}, i, this.subjectsDistributed);
        }
        var distributeSubjectsTimeout = setInterval(function(){
            clearInterval(distributeSubjectsTimeout);
            if(self.state != "paintingSendingSubjects") return;
            var disconnectedUsersSocket = utils.getElementsNotInSecondArray(self.match.players, self.subjectsCallbackArray);
            while(disconnectedUsersSocket.length > 0){
                var player = disconnectedUsersSocket.pop();
                self.match.addReconnectionCommand(utils.getUsernameFromSocket(player), "reconnectionPaintingSendSubjects", {subjects : self.subjects}, self);
            }
            self.state = "paintingReceivingPaintings";
            self.handleSubjects();
        }, 15 * 1000);
        this.match.timers.push(distributeSubjectsTimeout);
    }
    subjectsDistributed(socket, match){
        if(match.mode.state != "paintingSendingSubjects") return;
        if(utils.checkIfAlreadyInArray(socket, match.mode.subjectsCallbackArray)) return;
        match.mode.subjectsCallbackArray.push(socket);
        if(utils.haveSameMembers(match.mode.subjectsCallbackArray, match.mode.match.players)){
            match.mode.handleSubjects();
        }
    }
    handleSubjects(){
        var self = this;
        for(var i = 0; i < this.match.players.length; i++){
            this.match.emitWithAck('paintingHandleSubjects', {}, i);
        }
        this.startNewRound();
    }
    startNewRound(){
        var self = this;
        this.state = "paintingReceivingPaintings";
        this.drawTimer = setInterval(function(){
            clearInterval(self.drawTimer);
            if(self.state != "paintingReceivingPaintings") return;
            self.state = "paintingReceivingVotes";
            for(var i = 0; i < self.match.players.length; i++){
                self.match.emitWithAck('paintingHandlePaintings', {}, i);
            }
            self.paintingReceived = new Array();
            self.clientSentInfoArray = new Array();
            self.voteTimer = setInterval(function(){
                clearInterval(self.voteTimer);
                if(self.state != "paintingReceivingVotes") return;
                self.state = "paintingShowingResults";
                for(var i = 0; i < self.match.players.length; i++){
                    self.match.emitWithAck('paintingHandleVotes', {}, i);
                }
                self.votesReceived = new Array();
                self.clientSentInfoArray = new Array();
                self.resultTimer = setInterval(function(){
                    clearInterval(self.resultTimer);
                    if(self.state != "paintingShowingResults") return;
                    self.roundComplete();
                }, PAINTING_TIME_TO_SEE_RESULTS);
            }, PAINTING_TIME_TO_VOTE + 20 * 1000);
        }, PAINTING_TIME_TO_DRAW + 20 * 1000);
    }
    sendPainting(nodes, ratio, soc){
        if(this.state != "paintingReceivingPaintings") return;
        if(utils.checkIfAlreadyInArray(soc, this.clientSentInfoArray)) return;
        this.clientSentInfoArray.push(soc);
        for(var i = 0; i < this.match.players.length; i++){
            if(this.match.players[i] == soc) continue;
            this.match.emitWithAck('paintingReceivePainting', {nodes : nodes, ratio : ratio, player : utils.getUsernameFromSocket(soc)}, i);
        }
    }
    addPaintingsReceived(val){
        if(this.state != "paintingReceivingPaintings") return;
        if(utils.checkIfAlreadyInArray(val, this.paintingReceived)) return;
        this.paintingReceived.push(val);
        var self = this;
        if(this.paintingReceived.length == this.match.players.length){
            if(this.state != "paintingReceivingPaintings") return;
            clearInterval(this.drawTimer);
            this.state = "paintingReceivingVotes";
            for(var i = 0; i < this.match.players.length; i++){
                this.match.emitWithAck('paintingHandlePaintings', {}, i);
            }
            this.paintingReceived = new Array();
            this.clientSentInfoArray = new Array();
            this.voteTimer = setInterval(function(){
                clearInterval(self.voteTimer);
                if(self.state != "paintingReceivingVotes") return;
                self.state = "paintingShowingResults";
                for(var i = 0; i < self.match.players.length; i++){
                    self.match.emitWithAck('paintingHandleVotes', {}, i);
                }
                self.votesReceived = new Array();
                self.clientSentInfoArray = new Array()
                if(self.round == paintingMaxSubjects - 1){
                    var paintingEndTimer = setInterval(function(){
                        clearInterval(paintingEndTimer);
                        if(self.state == "paintingEnd") return;
                        self.calcPoints();
                        self.endMode();
                    }, PAINTING_TIME_TO_SEE_RESULTS);
                    self.match.timers.push(paintingEndTimer);
                }else{
                    self.resultTimer = setInterval(function(){
                        clearInterval(self.resultTimer);
                        if(self.state != "paintingShowingResults") return;
                        self.roundComplete();
                    }, PAINTING_TIME_TO_SEE_RESULTS);
                }
            }, PAINTING_TIME_TO_VOTE + 20 * 1000);
        }
    }
    sendVotes(votes, socket){
        if(this.state != "paintingReceivingVotes") return;
        if(utils.checkIfAlreadyInArray(socket, this.clientSentInfoArray)) return;
        this.clientSentInfoArray.push(socket);
        var playerIndex = this.match.getPlayerIndex(utils.getUsernameFromSocket(socket));
        if(playerIndex == -1) return;
        if(invalidVotes(votes, playerIndex)){
            votes = 0;
        }
        this.calcVotes(votes);
        votes *= 10;
        votes += playerIndex;
        for(var i = 0; i < this.match.players.length; i++){
            if(socket == this.match.players[i]) continue;
            this.match.emitWithAck('paintingReceiveVotes', {votes : votes}, i);
        }
    }
    calcVotes(votes, socket){
        for(var i = 0; i < match.maxPlayers; i++){
            var p = votes % 10;
            if(p != 0){
                this.points[i] += 6 - p;
            }
            votes = Math.floor(votes / 10);
        }
    }
    addVotesReceived(val){
        if(this.state != "paintingReceivingVotes") return;
        if(utils.checkIfAlreadyInArray(val, this.votesReceived)) return;
        this.votesReceived.push(val);
        var self = this;
        if(this.votesReceived.length == this.match.players.length){
            if(this.state != "paintingReceivingVotes") return;
            clearInterval(this.voteTimer);
            this.state = "paintingShowingResults";
            for(var i = 0; i < this.match.players.length; i++){
                this.match.emitWithAck('paintingHandleVotes', {}, i);
            }
            this.votesReceived = new Array();
            this.clientSentInfoArray = new Array();
            if(self.round == paintingMaxSubjects - 1){
                var paintingEndTimer = setInterval(function(){
                    clearInterval(paintingEndTimer);
                    if(self.state == "paintingEnd") return;
                    self.calcPoints();
                    self.endMode();
                }, PAINTING_TIME_TO_SEE_RESULTS);
                self.match.timers.push(paintingEndTimer);
            }else{
                this.resultTimer = setInterval(function(){
                    clearInterval(self.resultTimer);
                    if(self.state != "paintingShowingResults") return;
                    self.roundComplete();
                }, PAINTING_TIME_TO_SEE_RESULTS);
            }
        }
    }
    roundComplete(){
        if(this.state == "paintingEnd") return;
        this.round++;
        if(this.round == paintingMaxSubjects){
            this.state = "paintingEnd";
            this.calcPoints();
            this.endMode();
        }else{
            this.startNewRound();
        }
    }
    setSubjects(val){
        this.subjects = val;
    }
    calcPoints(){
        for(var i = 0; i < this.match.players.length; i++){
            this.match.emitWithAck('paintingSendFinalPoints', {points : this.points}, i);
        }
    }
    endMode(){
        this.state = "paintingEnd";
        this.match.endMode();
    }
}

function invalidVotes(votes, index){
    if(votes >= Math.pow(10, match.maxPlayers) || votes < 0) return true;
    var digits = utils.getDigits(votes);
    var bools = new Array();
    for(var i = 0; i < match.maxPlayers; i++){
        bools[i] = false;
    }
    var ind = digits.length - index - 1;
    if(digits[ind] != null){
        if(digits[ind] != 0){
            return true;
        }
    }
    for(var i = 0; i < digits.length; i++){
        if(digits[i] > match.maxPlayers || digits[i] < 0) return true;
        if(digits[i] != 0) if(bools[digits[i]] == true) return true;
        bools[digits[i]] = true;
    }

    return false;
}

function getRandomSubjects(num){
	var subs = new Array();
	var length = subjects.length;
	for(var i = 0; i < num; i++){
		var ol = subjects[Math.floor(Math.random() * length)];
		while(utils.checkIfAlreadyInArray(ol, subs)){
			ol = subjects[Math.floor(Math.random() * length)];
		}
		subs.push(ol);
	}

	return subs;
}

module.exports.PaintingMatch = PaintingMatch;