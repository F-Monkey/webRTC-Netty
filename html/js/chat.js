let localVideo;

let free_stun_server = [
    "stun:stun1.l.google.com:19302",
    "stun:stun2.l.google.com:19302",
    "stun:stun3.l.google.com:19302"
]

let offerOptions = {
  offerToReceiveAudio: 1,
  offerToReceiveVideo: 1
};

var body = document.getElementById("body");
var chatUl = document.createElement("ul");
body.appendChild(chatUl);

let myPeerConnection = createPeerConnection();

function UserMap(){
    this.url = {}
}

UserMap.prototype.put = function(userId, val){
    this.url[userId] = val;
    return;
}

UserMap.prototype.remove = function(userId){
    var element = this.url[userId];
    if(typeof element == "document"){
        element.remove(element.selectedIndex);
    }
    delete this.url[userId];
}

let remoteConnectionMap = new UserMap();

loadVideoComponent();

function loadVideoComponent(){
    var localVideoContent = "<video autoplay width=\"100%\" height=\"100%\"></video>";
    //localVideo = eval(videoContent);
    //let ul = document.createElement("ul");
    let li = document.createElement("li");
    li.innerHTML = localVideoContent;
    //li.appendChild(localVideo);
    localVideo = li.childNodes[0];
    chatUl.appendChild(li);
    //body.appendChild(ul);
}

function createRemoteConnection(roomData){
    if(roomData.characters){
        for(var i in roomData.characters){
            var character = roomData.characters[i];
            var remotePc = createPeerConnection(character.remoteAddress);
            addVideoElement(pc,chatUl);
            remoteConnectionMap.put(character.id, addVideoElement(pc,chatUl));
        }
    }
}

function createRemoteConnection_0(){
    var remotePc = createPeerConnection();
    remotePc.createOffer(offerOptions).then(desc => onCreateOfferSuccess(remotePc, desc))
                                        .catch(err =>{console.error(err)});
    var video = addVideoElement(remotePc,chatUl);
    remoteConnectionMap.put("111", video);
}

function createScreenShare(){
    navigator.mediaDevices.getDisplayMedia(constraints).then(stream =>{
        localVideo.srcObject = stream;
        shareStream(stream);
    }).catch(err =>{
        console.error(err);
    });
}

function createRemoteChat(){
    navigator.mediaDevices.getUserMedia(constraints).then(stream =>{
        localVideo.srcObject = stream;
        shareStream(stream);
    }).catch(err => {
        console.error(err);
    });
}

/**
    聊天
*/
var constraints = {video: true, audio:{ echoCancellation : true, noiseSuppression : true }};



function shareStream(stream){
    for (const track of stream.getTracks()) {
        myPeerConnection.addTrack(track, stream);
    }
}

/**
 * create a video element if the candidate add success, and add the element to the index of the parentUl's li
 */
function addVideoElement(pc, parentUl, index){

    var localVideoContent = "<video autoplay width=\"100%\" height=\"100%\"></video>";
    var li = document.createElement("li");
    li.innerHTML = localVideoContent;

    var video = li.childNodes[0];

    pc.ontrack = function(event){
        if (video.srcObject !== event.streams[0]) {
            video.srcObject = event.streams[0];
        }
        console.log('send local stream', event);
    }

    var childNodes = parentUl.childNodes;
    if(!index || childNodes.length < index){
        console.log("index is more than childNodes length, add element to the last");
        let li = document.createElement("li");
        li.appendChild(video);
        parentUl.appendChild(li);
        return video;
    }


    childNodes[index].innerHTML = "";
    childNodes[index].appendChild(video);
}

/**
 *  create peerConnection
 *  谁请求连接我，我才可以把candidate添加进去，谁才可以看到我的视频资源。
 *  所以我的peerConnection 就只有一个，
 *  但是如果我想看到其他人的视频资源的话，需要给别人发送connect
 */
function createPeerConnection(){
   var pcConfig = {
                  	'iceServers':[{
                  		'urls' : free_stun_server
                  	}]
                  };

    var pc = new RTCPeerConnection(pcConfig);
    pc.onIceCandidate = function(event){
        var candidate = event.candidate;
        console.info("candidate :", candidate);

        myPeerConnection.addIceCandidate(event.candidate)
                        .then(() => onAddIceCandidateSuccess(candidate), err =>onAddIceCandidateError(candidate,error));
        myPeerConnection.createAnswer().then(desc =>createAnswerSuc(myPeerConnection,desc));
    }
    return pc;
}

function createAnswerSuc(pc,desc){
	pc.setLocalDescription(desc);
}

function onCreateOfferSuccess(pc,desc){
  console.log(`Offer from pc1${desc.sdp}`);
  console.log('pc1 setLocalDescription start');
  pc.setLocalDescription(desc, () => onSetLocalSuccess(pc), onSetSessionDescriptionError);
}

function onSetLocalSuccess(pc) {
  console.log("setLocalDescription complete");
}

function onSetRemoteSuccess(pc) {
  console.log("setRemoteDescription complete");
}

function onSetSessionDescriptionError(error) {
  console.log("Failed to set session description");
}


function onCreateSessionDescriptionError(error) {
  console.log(`Failed to create session description: ${error.toString()}`);
}


function addCandidate(candidate){
    myPeerConnection.addIceCandidate(event.candidate)
                .then(() => onAddIceCandidateSuccess(candidate), err =>onAddIceCandidateError(candidate,error));
    myPeerConnection.createAnswer().then(desc =>createAnswerSuc(myPeerConnection,desc));
}

function onIceCandidate(event) {
    var candidate = event.candidate;
    console.info("candidate :", candidate);
    addCandidate(candidate);
}

function onAddIceCandidateSuccess(candidate) {
  console.log(candidate +` addIceCandidate success`);
}

function onAddIceCandidateError(candidate, error) {
  console.log(candidate+` failed to add ICE Candidate: ${error.toString()}`);
}
